package micronet.tools.filesync;

import static micronet.tools.model.ModelConstants.ENTITY_TEMPLATE_ROOT_KEY;
import static micronet.tools.model.ModelConstants.NAME_PROP_KEY;
import static micronet.tools.model.ModelConstants.PARENT_PROP_KEY;
import static micronet.tools.model.ModelConstants.VARIABLES_PROP_KEY;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import micronet.args.XOBJ;
import micronet.args.XOUT;
import micronet.tools.model.INode;
import micronet.tools.model.IVisitor;
import micronet.tools.model.ModelConstants;
import micronet.tools.model.nodes.EntityTemplateNode;
import micronet.tools.model.nodes.EntityTemplateRootNode;
import micronet.tools.model.nodes.EntityVariableConstNode;
import micronet.tools.model.nodes.EntityVariableDynamicNode;
import micronet.tools.model.nodes.EntityVariableNode;
import micronet.tools.model.nodes.EnumNode;
import micronet.tools.model.nodes.EnumRootNode;
import micronet.tools.model.nodes.PrefabNode;
import micronet.tools.model.nodes.PrefabRootNode;
import micronet.tools.model.nodes.PrefabVariableNode;
import micronet.tools.model.variables.CollectionDescription;
import micronet.tools.model.variables.ComponentDescription;
import micronet.tools.model.variables.EnumDescription;
import micronet.tools.model.variables.GeometryDescription;
import micronet.tools.model.variables.MapDescription;
import micronet.tools.model.variables.NumberDescription;
import micronet.tools.model.variables.ScriptDescription;
import micronet.tools.model.variables.VariableDescription;
import micronet.tools.model.variables.VariableType;

public class SyncTemplateTree {

	private static Semaphore semaphore = new Semaphore(1);
	
	public static Map<String, Set<String>> getEnumUsage(String sharedDir) {
		EntityTemplateRootNode rootNode = loadTemplateTree(sharedDir);
		return getEnumUsage(rootNode);
	}
	
	private static Map<String, Set<String>> getEnumUsage(EntityTemplateNode templateNode) {
		Map<String, Set<String>> enumUsage = new HashMap<>();
		
		for (INode childNode : templateNode.getChildren()) {
			if (childNode instanceof EntityTemplateNode) {
				Map<String, Set<String>> childUsage = getEnumUsage((EntityTemplateNode) childNode);
				for (Map.Entry<String, Set<String>> usageEntry : childUsage.entrySet()) {
					if (!enumUsage.containsKey(usageEntry.getKey())) 
						enumUsage.put(usageEntry.getKey(), new HashSet<>());
					enumUsage.get(usageEntry.getKey()).addAll(usageEntry.getValue());
				}
			} else if (childNode instanceof EntityVariableNode) {

				EntityVariableNode variableNode = (EntityVariableNode) childNode;
				VariableDescription variableDescription = variableNode.getVariabelDescription();
				
				if (variableDescription.getType() == VariableType.ENUM) {
					EnumDescription enumDesc = (EnumDescription) variableDescription;
					
					if (!enumUsage.containsKey(enumDesc.getEnumType()))
						enumUsage.put(enumDesc.getEnumType(), new HashSet<>());
					enumUsage.get(enumDesc.getEnumType()).add(templateNode.getName());
				}
			}
		}
		return enumUsage;
	}

	public static Map<String, Set<String>> getTemplateUsage(String sharedDir) {
		EntityTemplateRootNode rootNode = loadTemplateTree(sharedDir);
		return getTemplateUsage(rootNode);
	}
	
	public static Map<String, Set<String>> getTemplateUsage(EntityTemplateNode templateNode) {
		Map<String, Set<String>> templateUsage = new HashMap<>();
		
		for (INode childNode : templateNode.getChildren()) {
			if (childNode instanceof EntityTemplateNode) {
				
				Map<String, Set<String>> childUsage = getTemplateUsage((EntityTemplateNode) childNode);
				for (Map.Entry<String, Set<String>> usageEntry : childUsage.entrySet()) {
					if (!templateUsage.containsKey(usageEntry.getKey())) 
						templateUsage.put(usageEntry.getKey(), new HashSet<>());
					templateUsage.get(usageEntry.getKey()).addAll(usageEntry.getValue());
				}
				
			} else if (childNode instanceof EntityVariableNode) {

				EntityVariableNode variableNode = (EntityVariableNode) childNode;
				VariableDescription variableDescription = variableNode.getVariabelDescription();
				
				String usedKey = null;
				
				switch (variableDescription.getType()) {
				case COMPONENT:
					ComponentDescription componentDesc = (ComponentDescription) variableDescription;
					usedKey = componentDesc.getComponentType();
					break;
				case LIST:
				case MAP:
					CollectionDescription collectionDesc = (CollectionDescription) variableDescription;
					if (collectionDesc.getEntryType().getType() == VariableType.COMPONENT) {
						ComponentDescription entryComponentDesc = (ComponentDescription)collectionDesc.getEntryType();
						usedKey = entryComponentDesc.getComponentType();
					}
					break;
				default:
					break;
				}
				
				if (usedKey != null) {
					if (!templateUsage.containsKey(usedKey))
						templateUsage.put(usedKey, new HashSet<>());
					templateUsage.get(usedKey).add(templateNode.getName());
				}
			}
		}
		return templateUsage;
	}
	
	public static List<String> getAllTemplateNames(String sharedDir) {
		File templateDir = getTemplateDir(sharedDir);
		File[] directoryListing = templateDir.listFiles();
		if (directoryListing == null)
			return null;

		JsonParser parser = new JsonParser();
		List<String> templateNames = new ArrayList<>();

		try {
			semaphore.acquire();

			for (File templateFile : directoryListing) {
				try {
					String data = new String(Files.readAllBytes(templateFile.toPath()), StandardCharsets.UTF_8);
					JsonElement templateObject = parser.parse(data);
					templateNames.add(templateObject.getAsJsonObject().getAsJsonPrimitive(NAME_PROP_KEY).getAsString());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			semaphore.release();
		}
		return templateNames;
	}
	
	public static List<EntityTemplateNode> loadAllTemplateTypes(String sharedDir) {
		File templateDir = getTemplateDir(sharedDir);
		File[] directoryListing = templateDir.listFiles();
		if (directoryListing == null)
			return null;

		List<EntityTemplateNode> templateObjects = new ArrayList<>();

		for (File templateFile : directoryListing) {
			EntityTemplateNode templateNode = loadTemplateType(templateFile.getName(), sharedDir);
			if (templateNode != null)
				templateObjects.add(templateNode);
		}
		return templateObjects;
	}
	
	public static EntityTemplateNode loadTemplateType(String templateType, String sharedDir) {
		
		XOBJ<String> parentNameOut = new XOBJ<>(null);    
	    
		EntityTemplateNode templateNode = loadTemplateType(templateType, parentNameOut.Out(), sharedDir); 
	    EntityTemplateNode originalNode = templateNode;

	    while (parentNameOut.Value != null) {
	    	String parentName = parentNameOut.Value;
	    	parentNameOut = new XOBJ<>(null);   
	    	EntityTemplateNode parentNode = loadTemplateType(parentName, parentNameOut.Out(), sharedDir);
	    	templateNode.setParent(parentNode);
	    	templateNode = parentNode;
	    }
	    
		return originalNode;
	}
	
	public static EntityTemplateNode loadTemplateType(String name, XOUT<String> parentName, String sharedDir) {

		File templateDir = getTemplateDir(sharedDir);
		
		File templateFile = new File(templateDir + "/" + name);
		if (!templateFile.exists())
			return null;
		
		String data = loadTemplateFile(templateFile);
		JsonElement templateObject = new JsonParser().parse(data);
		if (templateObject == null)
			return null;
		
		return deserializeTemplateNode(templateObject, parentName, sharedDir);
	}
	
	public static EntityTemplateRootNode loadTemplateTree(String sharedDir) {

		File templateDir = getTemplateDir(sharedDir);
		File[] directoryListing = templateDir.listFiles();
		if (directoryListing == null)
			return null;

		JsonParser parser = new JsonParser();
		List<JsonElement> templateFileObjects = new ArrayList<>();

		try {
			semaphore.acquire();

			for (File templateFile : directoryListing) {
				try {
					String data = new String(Files.readAllBytes(templateFile.toPath()), StandardCharsets.UTF_8);
					JsonElement templateObject = parser.parse(data);
					templateFileObjects.add(templateObject);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			semaphore.release();
		}

		return constructTemplateTree(templateFileObjects, sharedDir);
	}
	
	private static EntityTemplateRootNode constructTemplateTree(List<JsonElement> templateFileObjects, String sharedDir) {
		
		EntityTemplateRootNode templateTreeRoot = new EntityTemplateRootNode(ENTITY_TEMPLATE_ROOT_KEY);

		Map<String, EntityTemplateNode> nodeMapping = new HashMap<>();
		Map<String, List<String>> parentMapping = new HashMap<>();
		
		for (JsonElement templatElement : templateFileObjects) {
			String templateName = templatElement.getAsJsonObject().get(NAME_PROP_KEY).getAsString();
			
			XOUT<String> parentNameOut = new XOUT<>(null);    
			EntityTemplateNode template = deserializeTemplateNode(templatElement, parentNameOut, sharedDir);
			
			nodeMapping.put(templateName, template);
			
			if (parentNameOut.Obj.Value != null) {
				
				if (!parentMapping.containsKey(parentNameOut.Obj.Value))
					parentMapping.put(parentNameOut.Obj.Value, new ArrayList<>());
				parentMapping.get(parentNameOut.Obj.Value).add(templateName);
				
			} else {
				templateTreeRoot.addChild(template);
			}
		}
		
		while (parentMapping.size() > 0) {
			
			Map.Entry<String, List<String>> parentEntry = parentMapping.entrySet().iterator().next();
			String parentName = parentEntry.getKey();
			
			EntityTemplateNode parentNode = nodeMapping.get(parentName);
			
			for (String childName : parentEntry.getValue()) {
				EntityTemplateNode childNode = nodeMapping.get(childName);
				parentNode.addChild(childNode);
			}
			parentMapping.remove(parentName);
		}
		return templateTreeRoot;
	}
	
	public static void saveTemplateTree(EntityTemplateNode node, String sharedDir) {
		SaveTemplateTreeVisitor visitor = new SaveTemplateTreeVisitor(sharedDir);
		node.accept(visitor);
	}

	private static class SaveTemplateTreeVisitor implements IVisitor {
		private String sharedDir;
		private File templateDir;

		public SaveTemplateTreeVisitor(String sharedDir) {
			this.sharedDir = sharedDir;
			this.templateDir = getTemplateDir(sharedDir);
		}
		

		@Override
		public void visit(EntityTemplateRootNode rootNode) {
			for (INode childNode : rootNode.getChildren()) {
				if (childNode instanceof EntityTemplateNode) {
					childNode.accept(this);
				} 
			}
		}

		@Override
		public void visit(EntityTemplateNode node) {

			JsonObject template = serializeTemplateNode(node, sharedDir);
			
			for (INode childNode : node.getChildren()) {
				if (childNode instanceof EntityTemplateNode) {
					childNode.accept(this);
				}
			}

			File templateFile = new File(templateDir + "/" + node.getName());
			Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
			String data = gson.toJson(template);

			try {
				semaphore.acquire();
				try {
					Files.write(templateFile.toPath(), data.getBytes());
				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				semaphore.release();
			}
		}
		
		@Override
		public void visit(EnumRootNode enumRootNode) {
		}
		@Override
		public void visit(EnumNode enumNode) {
		}
		@Override
		public void visit(PrefabNode prefabNode) {
		}
		@Override
		public void visit(PrefabRootNode prefabRootNode) {
		}
	}
	
	private static JsonObject serializeTemplateNode(EntityTemplateNode node, String sharedDir) {
		JsonObject template = new JsonObject();

		String parentName = node.getParent() != null ? node.getParent().getName() : null;
		parentName = parentName != ENTITY_TEMPLATE_ROOT_KEY ? parentName : null;
		template.addProperty(NAME_PROP_KEY, node.getName());
		template.addProperty(PARENT_PROP_KEY, parentName);

		JsonArray variables = new JsonArray();

		for (INode childNode : node.getChildren()) {
			if (childNode instanceof EntityVariableNode) {
				EntityVariableNode variableNode = (EntityVariableNode)childNode;
				JsonObject variableObject = serializeVariable(variableNode, sharedDir);
				variables.add(variableObject);
			}
		}

		template.add(VARIABLES_PROP_KEY, variables);
		return template;
	}
	
	private static EntityTemplateNode deserializeTemplateNode(JsonElement templateObject, XOUT<String> parentName, String sharedDir) {
		String templateName = templateObject.getAsJsonObject().get(NAME_PROP_KEY).getAsString();
		EntityTemplateNode templateNode = new EntityTemplateNode(templateName);
		
		JsonArray variables = templateObject.getAsJsonObject().getAsJsonArray(VARIABLES_PROP_KEY);
		for (JsonElement variable : variables) {
			EntityVariableNode variableNode =  deserializeVariable(variable.getAsJsonObject(), sharedDir);
			templateNode.addChild(variableNode);
		}
		
		if (templateObject.getAsJsonObject().get(PARENT_PROP_KEY) != null && !templateObject.getAsJsonObject().get(PARENT_PROP_KEY).isJsonNull()) {
			JsonPrimitive parentNameObject = templateObject.getAsJsonObject().getAsJsonPrimitive(PARENT_PROP_KEY);
			parentName.Obj.Value = parentNameObject == null ? null : parentNameObject.getAsString();
		}
		return templateNode;
	}
	
	private static EntityVariableNode deserializeVariable(JsonObject variableObject, String sharedDir) {
		String variableName = variableObject.getAsJsonPrimitive(ModelConstants.NAME_PROP_KEY).getAsString();
		JsonObject variableDetails = variableObject.getAsJsonObject(ModelConstants.TYPE_PROP_KEY);
		
		EntityVariableNode variableNode = null; 
		VariableDescription variabelDescription = deserializeVariableDescription(variableDetails);
		
		
		if (variableObject.get(ModelConstants.CONST_VARIABLE_PROP_KEY) != null) {
			variableNode = new EntityVariableConstNode(variableName);
			PrefabVariableNode prefabNode = new PrefabVariableNode(variableName, variabelDescription);

			if (!variableObject.get(ModelConstants.CONST_VARIABLE_PROP_KEY).isJsonNull()) {
				JsonElement variableElement = variableObject.get(ModelConstants.CONST_VARIABLE_PROP_KEY);
				SyncPrefabTree.deserializePrefabVariable(prefabNode, variableElement, sharedDir);
			}
			variableNode.addChild(prefabNode);
		} else {
			EntityVariableDynamicNode dynamicVar = new EntityVariableDynamicNode(variableName);
			variableNode = dynamicVar;
			if (variableObject.getAsJsonPrimitive(ModelConstants.CTOR_ARGUMENT_PROP_KEY) != null) {
				boolean ctorArg = variableObject.getAsJsonPrimitive(ModelConstants.CTOR_ARGUMENT_PROP_KEY).getAsBoolean();
				dynamicVar.setCtorArg(ctorArg);
			}
		}
		
		variableNode.setVariabelDescription(variabelDescription);
		return variableNode;
	}
	
	private static JsonObject serializeVariable(EntityVariableNode variableNode, String sharedDir) {
		
		JsonObject variableObject = new JsonObject();
		variableObject.addProperty(ModelConstants.NAME_PROP_KEY, variableNode.getName());
		
		JsonElement variableDetails = new Gson().toJsonTree(variableNode.getVariabelDescription());
		variableObject.add(ModelConstants.TYPE_PROP_KEY, variableDetails);
		
		if (variableNode instanceof EntityVariableConstNode) {
			PrefabVariableNode prefabNode = (PrefabVariableNode)variableNode.getChildren().get(0);
			JsonElement prefabElement = SyncPrefabTree.serializePrefabVariable(prefabNode);
			variableObject.add(ModelConstants.CONST_VARIABLE_PROP_KEY, prefabElement);
			
		} else if (variableNode instanceof EntityVariableDynamicNode) {
			EntityVariableDynamicNode dynamicVariableNode = (EntityVariableDynamicNode)variableNode;
			variableObject.addProperty(ModelConstants.CTOR_ARGUMENT_PROP_KEY, dynamicVariableNode.isCtorArg());
		}
		
		return variableObject;
	}
	
	private static VariableDescription deserializeVariableDescription(JsonObject variableDetails) {
		VariableType variableType = Enum.valueOf(VariableType.class, variableDetails.getAsJsonPrimitive(ModelConstants.TYPE_PROP_KEY).getAsString());
		
		switch (variableType) {
		case SET:
		case LIST:
			CollectionDescription collectionDesc = new Gson().fromJson(variableDetails, CollectionDescription.class);
			JsonObject listEntryDetails = variableDetails.getAsJsonObject(ModelConstants.ENTRY_TYPE_PROP_KEY);
			VariableDescription entryDesc = deserializeVariableDescription(listEntryDetails);
			collectionDesc.setEntryType(entryDesc);
			return collectionDesc;
		case NUMBER:
			return new Gson().fromJson(variableDetails, NumberDescription.class);
		case MAP:
			MapDescription mapDesc = new Gson().fromJson(variableDetails, MapDescription.class);

			JsonObject mapKeyDetails = variableDetails.getAsJsonObject(ModelConstants.KEY_TYPE_PROP_KEY);
			VariableDescription mapKeyDesc = deserializeVariableDescription(mapKeyDetails);
			mapDesc.setKeyType(mapKeyDesc);

			JsonObject mapEntryDetails = variableDetails.getAsJsonObject(ModelConstants.ENTRY_TYPE_PROP_KEY);
			VariableDescription mapEntryDesc = deserializeVariableDescription(mapEntryDetails);
			mapDesc.setEntryType(mapEntryDesc);
			
			return mapDesc;
		case ENUM:
			return new Gson().fromJson(variableDetails, EnumDescription.class);
		case COMPONENT:
			return new Gson().fromJson(variableDetails, ComponentDescription.class);
		case SCRIPT:
			ScriptDescription scriptDesc = new Gson().fromJson(variableDetails, ScriptDescription.class);
			JsonObject externalArgsDescriptions = variableDetails.getAsJsonObject(ModelConstants.EXTERNAL_ARGS_PROP_KEY);
			Map<String, VariableDescription> externalArgs = new HashMap<>();
			for (Map.Entry<String, VariableDescription> arg : scriptDesc.getExternalArgs().entrySet()) {
				VariableDescription argDesc = deserializeVariableDescription(externalArgsDescriptions.getAsJsonObject(arg.getKey()));
				externalArgs.put(arg.getKey(), argDesc);
			}
			scriptDesc.setExternalArgs(externalArgs);
			return scriptDesc;
		case GEOMETRY:
			return new Gson().fromJson(variableDetails, GeometryDescription.class);
		case BOOLEAN:
		case CHAR:
		case STRING:
		default:
			return new Gson().fromJson(variableDetails, VariableDescription.class);
		}
	}

	private static File getTemplateDir(String sharedDir) {
		File modelDir = ModelConstants.getModelDir(sharedDir);
		File templateDir = new File(modelDir + "/templates/");
		if (!templateDir.exists())
			templateDir.mkdir();
		return templateDir;
	}
	
	private static String loadTemplateFile(File templateFile) {
		try {
			semaphore.acquire();

			try {
				return new String(Files.readAllBytes(templateFile.toPath()), StandardCharsets.UTF_8);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			semaphore.release();
		}
		return null;
	}
	
	public static boolean templateExists(String name, String sharedDir) {
		File templateDir = getTemplateDir(sharedDir);
		File templateFile = new File(templateDir + "/" + name);
		return templateFile.exists();
	}
	
	public static void removeTemplate(INode node, String sharedDir) {
		File templateDir = getTemplateDir(sharedDir);
		File templateFile = new File(templateDir + "/" + node.getName());
		templateFile.delete();
	}
}
