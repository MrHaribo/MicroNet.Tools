package micronet.tools.filesync;

import static micronet.tools.model.ModelConstants.ENTITY_TEMPLATE_ROOT_KEY;
import static micronet.tools.model.ModelConstants.NAME_PROP_KEY;
import static micronet.tools.model.ModelConstants.PARENT_PROP_KEY;
import static micronet.tools.model.ModelConstants.VARIABLES_PROP_KEY;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;
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
import micronet.tools.model.nodes.EntityVariableNode;
import micronet.tools.model.nodes.EnumNode;
import micronet.tools.model.nodes.EnumRootNode;
import micronet.tools.model.nodes.PrefabNode;
import micronet.tools.model.nodes.PrefabRootNode;
import micronet.tools.model.variables.CollectionDescription;
import micronet.tools.model.variables.ComponentDescription;
import micronet.tools.model.variables.EnumDescription;
import micronet.tools.model.variables.MapDescription;
import micronet.tools.model.variables.NumberDescription;
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
				case ENUM: case SET: case BOOLEAN: case CHAR: case STRING: case NUMBER: default:
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
				try (Scanner scanner = new Scanner(templateFile)) {
					scanner.useDelimiter("\\A");
					String data = scanner.next();

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
		
		JsonElement templateObject = null;

		try {
			semaphore.acquire();

			try (Scanner scanner = new Scanner(templateFile)) {
				scanner.useDelimiter("\\A");
				String data = scanner.next();

				JsonParser parser = new JsonParser();
				templateObject = parser.parse(data);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			semaphore.release();
		}
		
		if (templateObject == null)
			return null;
		
		String templateName = templateObject.getAsJsonObject().get(NAME_PROP_KEY).getAsString();
		EntityTemplateNode templateNode = new EntityTemplateNode(templateName);
		
		JsonArray variables = templateObject.getAsJsonObject().getAsJsonArray(VARIABLES_PROP_KEY);
		for (JsonElement variable : variables) {
			EntityVariableNode variableNode =  deserializeVariable(variable.getAsJsonObject());
			templateNode.addChild(variableNode);
		}
		
		JsonPrimitive parentNameObject = templateObject.getAsJsonObject().getAsJsonPrimitive(PARENT_PROP_KEY);
		parentName.Obj.Value = parentNameObject == null ? null : parentNameObject.getAsString();

		return templateNode;
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
				try (Scanner scanner = new Scanner(templateFile)) {
					scanner.useDelimiter("\\A");
					String data = scanner.next();

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

		return constructTemplateTree(templateFileObjects);
	}

	private static EntityTemplateRootNode constructTemplateTree(List<JsonElement> templateFileObjects) {

		Map<String, List<JsonElement>> parentMapping = new HashMap<>();
		Stack<JsonElement> parentObjects = new Stack<>();
		Map<String, EntityTemplateNode> processedNodes = new HashMap<>();

		EntityTemplateRootNode templateTreeRoot = new EntityTemplateRootNode(ENTITY_TEMPLATE_ROOT_KEY);

		for (JsonElement templateObject : templateFileObjects) {

			JsonPrimitive parentNameObject = templateObject.getAsJsonObject().getAsJsonPrimitive(PARENT_PROP_KEY);
			String parentName = parentNameObject == null ? null : parentNameObject.getAsString();

			if (parentName == null) {
				parentObjects.add(templateObject);
			} else {
				if (!parentMapping.containsKey(parentName))
					parentMapping.put(parentName, new ArrayList<>());
				parentMapping.get(parentName).add(templateObject);
			}
		}

		while (!parentObjects.isEmpty()) {

			JsonElement parentTemplate = parentObjects.pop();
			String potentialParentName = parentTemplate.getAsJsonObject().get(NAME_PROP_KEY).getAsString();

			EntityTemplateNode parentNode = null;
			if (processedNodes.containsKey(potentialParentName)) {
				// Template was added by a parent before (cannot be a root
				// element)
				parentNode = processedNodes.get(potentialParentName);
			} else {
				parentNode = new EntityTemplateNode(potentialParentName);
				processedNodes.put(potentialParentName, parentNode);
				templateTreeRoot.addChild(parentNode);
			}

			if (parentMapping.containsKey(potentialParentName)) {
				List<JsonElement> childTemplateObjects = parentMapping.get(potentialParentName);

				for (JsonElement childTemplate : childTemplateObjects) {
					String childName = childTemplate.getAsJsonObject().get(NAME_PROP_KEY).getAsString();
					EntityTemplateNode childNode = new EntityTemplateNode(childName);
					processedNodes.put(childName, childNode);
					parentObjects.push(childTemplate);
					parentNode.addChild(childNode);
				}
			}

			JsonArray variables = parentTemplate.getAsJsonObject().getAsJsonArray(VARIABLES_PROP_KEY);
			for (JsonElement variable : variables) {
				EntityVariableNode variableNode =  deserializeVariable(variable.getAsJsonObject());
				parentNode.addChild(variableNode);
			}
		}

		return templateTreeRoot;
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
	
	public static void saveTemplateTree(EntityTemplateNode node, String sharedDir) {
		SaveTemplateTreeVisitor visitor = new SaveTemplateTreeVisitor(sharedDir);
		node.accept(visitor);
	}

	private static class SaveTemplateTreeVisitor implements IVisitor {
		private File templateDir;

		public SaveTemplateTreeVisitor(String sharedDir) {
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

			JsonObject template = new JsonObject();

			String parentName = node.getParent() != null ? node.getParent().getName() : null;
			parentName = parentName != ENTITY_TEMPLATE_ROOT_KEY ? parentName : null;
			template.addProperty(NAME_PROP_KEY, node.getName());
			template.addProperty(PARENT_PROP_KEY, parentName);

			JsonArray variables = new JsonArray();

			for (INode childNode : node.getChildren()) {

				if (childNode instanceof EntityTemplateNode) {
					childNode.accept(this);
				} else if (childNode instanceof EntityVariableNode) {
					EntityVariableNode variableNode = (EntityVariableNode)childNode;
					JsonObject variableObject = serializeVariableDescription(variableNode);
					variables.add(variableObject);
				}
			}

			template.add(VARIABLES_PROP_KEY, variables);

			File templateFile = new File(templateDir + "/" + node.getName());
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			String data = gson.toJson(template);

			try {
				semaphore.acquire();

				try (PrintWriter printer = new PrintWriter(templateFile)) {
					printer.print(data);
				} catch (FileNotFoundException e) {
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
	
	private static EntityVariableNode deserializeVariable(JsonObject variableObject) {
		String variableName = variableObject.getAsJsonPrimitive(ModelConstants.NAME_PROP_KEY).getAsString();
		JsonObject variableDetails = variableObject.getAsJsonObject(ModelConstants.TYPE_PROP_KEY);

		EntityVariableNode variableNode = new EntityVariableNode(variableName);
		VariableDescription variabelDescription = deserializeVariableDescription(variableDetails);

		variableNode.setVariabelDescription(variabelDescription);
		return variableNode;
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
		case BOOLEAN:
		case CHAR:
		case STRING:
		default:
			return new Gson().fromJson(variableDetails, VariableDescription.class);
		}
		
	}
	
	private static JsonObject serializeVariableDescription(EntityVariableNode variableNode) {
		
		JsonObject variableDesc = new JsonObject();
		variableDesc.addProperty(ModelConstants.NAME_PROP_KEY, variableNode.getName());
		
		JsonElement variableDetails = new Gson().toJsonTree(variableNode.getVariabelDescription());
		variableDesc.add(ModelConstants.TYPE_PROP_KEY, variableDetails);
		
		return variableDesc;
	}

	private static File getTemplateDir(String sharedDir) {
		File modelDir = ModelConstants.getModelDir(sharedDir);
		File templateDir = new File(modelDir + "/templates/");
		if (!templateDir.exists())
			templateDir.mkdir();
		return templateDir;
	}
}
