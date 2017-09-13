package micronet.tools.filesync;

import static micronet.tools.model.ModelConstants.TYPE_PROP_KEY;
import static micronet.tools.model.ModelConstants.getModelDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Semaphore;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import micronet.tools.model.INode;
import micronet.tools.model.IVisitor;
import micronet.tools.model.nodes.EntityTemplateNode;
import micronet.tools.model.nodes.EntityTemplateRootNode;
import micronet.tools.model.nodes.EntityVariableNode;
import micronet.tools.model.nodes.EnumNode;
import micronet.tools.model.nodes.EnumRootNode;
import micronet.tools.model.nodes.PrefabNode;
import micronet.tools.model.nodes.PrefabRootNode;
import micronet.tools.model.nodes.PrefabVariableEntryNode;
import micronet.tools.model.nodes.PrefabVariableKeyNode;
import micronet.tools.model.nodes.PrefabVariableNode;
import micronet.tools.model.variables.CollectionDescription;
import micronet.tools.model.variables.ComponentDescription;
import micronet.tools.model.variables.MapDescription;
import micronet.tools.model.variables.NumberDescription;
import micronet.tools.model.variables.VariableDescription;
import micronet.tools.model.variables.VariableType;

public class SyncPrefabTree {
	
	private static Semaphore semaphore = new Semaphore(1);
	
	public static void loadPrefab(PrefabNode prefabNode, String sharedDir) {
	
	}
	
	public static void removePrefab(PrefabNode node, String sharedDir) {
		
		for (INode childNode : node.getChildren()) {
			if (childNode instanceof PrefabNode) {
				removePrefab((PrefabNode)childNode, sharedDir);
			}
		}
		
		String prefabID = PrefabNode.serializeID(node);
		File metaFile = new File(getPrefabMetaDir(sharedDir) + "/" + prefabID);
		File dataFile = new File(getPrefabDataDir(sharedDir) + "/" + prefabID);
		metaFile.delete();
		dataFile.delete();
	}
	
	public static PrefabRootNode loadPrefabTree(String sharedDir) {
		
		File prefabMetaDir = getPrefabMetaDir(sharedDir);
		File[] directoryListing = prefabMetaDir.listFiles();
		if (directoryListing == null)
			return null;
		
		Map<String[], PrefabNode> prefabMap = new TreeMap<>(new Comparator<String[]>() {
			@Override
			public int compare(String[] o1, String[] o2) {
				int comparison = o1.length - o2.length;
				if (comparison != 0)
					return comparison;
				
				String name1 = String.join(".", o1);
				String name2 = String.join(".", o2);
				return name1.compareTo(name2);
			}
		});

		for (File metaFile : directoryListing) {
			
			String metaData = loadFile(metaFile);
			if (metaData == null)
				return null;
			
			String[] prefabID = deserializePrefabID(metaFile.getName());
			String prefabName = prefabID[prefabID.length-1];
			
			JsonObject metaObject = new JsonParser().parse(metaData).getAsJsonObject();
			String prefabType = metaObject.getAsJsonPrimitive(TYPE_PROP_KEY).getAsString();
			
			File prefabDataDir = getPrefabDataDir(sharedDir);
			File dataFile = new File(prefabDataDir + "/" + metaFile.getName());
			
			PrefabNode prefabNode = parsePrefab(prefabName, prefabType, dataFile, sharedDir);
			prefabMap.put(prefabID, prefabNode);
		}
		PrefabRootNode rootNode = new PrefabRootNode();
		
		for (Map.Entry<String[], PrefabNode> prefab : prefabMap.entrySet()) {
			
			if (prefab.getKey().length == 1) {
				rootNode.addChild(prefab.getValue());
			} else {
				String[] parentName = Arrays.copyOf(prefab.getKey(), prefab.getKey().length-1);
				prefabMap.get(parentName).addChild(prefab.getValue());
			}
		}
		return rootNode;
	}
	
	private static PrefabNode parsePrefab(String prefabName, String prefabType, File file, String sharedDir) {
		
		String data = loadFile(file);
		if (data == null)
			return null;
		
		JsonParser parser = new JsonParser();
		JsonObject prefabObject = parser.parse(data).getAsJsonObject();
		
		PrefabNode prefabNode = new PrefabNode(prefabName, prefabType, sharedDir);
		
		deserializePrefabVariables(prefabObject, prefabNode, sharedDir);
		
		return prefabNode;
	}
	
	private static void deserializePrefabVariables(JsonObject prefabObject, PrefabNode prefabNode, String sharedDir) {
		
		for (INode childNode : prefabNode.getChildren()) {
			
			if (childNode instanceof PrefabVariableNode) {
				
				PrefabVariableNode variableNode = (PrefabVariableNode)childNode;
				
				JsonElement element = prefabObject.get(variableNode.getName());
				if (element == null)
					continue;
				
				try {
					deserializePrefabVariable(variableNode, element, sharedDir);
				} catch (Exception e) {
					variableNode.setVariableValue(null);
				}
			}
		}
	}
	
	public static void deserializePrefabVariable(PrefabVariableNode variableNode, JsonElement element, String sharedDir) {

		switch (variableNode.getVariableType()) {
		case BOOLEAN:
			variableNode.setVariableValue(element.getAsBoolean());
			break;
		case CHAR:
			variableNode.setVariableValue(element.getAsCharacter());
			break;
		case COMPONENT:
			JsonObject componentObject = element.getAsJsonObject();
			if (componentObject == null)
				break;
			variableNode.setVariableValue(new Object());
			
			ComponentDescription componentDesc = (ComponentDescription) variableNode.getVariableDescription();
			EntityTemplateNode templateType = SyncTemplateTree.loadTemplateType(componentDesc.getComponentType(), sharedDir);
			
			for (INode child : templateType.getChildren()) {
				if (child instanceof EntityVariableNode) {
					PrefabVariableNode childVariable = new PrefabVariableNode((EntityVariableNode)child, templateType.getName());
					variableNode.addChild(childVariable);
					JsonElement childVariableObject = componentObject.get(child.getName());
					try {
						deserializePrefabVariable(childVariable, childVariableObject, sharedDir);
					} catch (Exception e) {
						childVariable.setVariableValue(null);
					}
				}
			}
			break;
		case SET:
		case LIST:
			JsonArray listArray = element.getAsJsonArray();
			if (listArray == null)
				break;
			variableNode.setVariableValue(new Object());
			
			CollectionDescription listDescription = (CollectionDescription) variableNode.getVariableDescription();
			VariableDescription entryDesc = listDescription.getEntryType();
			
			int index = 0;
			for (JsonElement listEntry : listArray) {
				PrefabVariableEntryNode childVariable = new PrefabVariableEntryNode(listDescription.getEntryType().getType().toString(), entryDesc);
				childVariable.setName(entryDesc.getType().toString() + index++);
				variableNode.addChild(childVariable);
				
				if (variableNode.getVariableType() == VariableType.SET)
					childVariable.setEditable(false);
				
				try {
					deserializePrefabVariable(childVariable, listEntry, sharedDir);
				} catch (Exception e) {
					childVariable.setVariableValue(null);
				}
			}
			break;
		case ENUM:
			variableNode.setVariableValue(element.getAsString());
			break;
		case MAP:
			
			JsonObject mapObject = element.getAsJsonObject();
			if (mapObject == null)
				break;
			variableNode.setVariableValue(new Object());
			
			MapDescription mapDescription = (MapDescription) variableNode.getVariableDescription();
			VariableDescription mapKeyDesc = mapDescription.getKeyType();
			VariableDescription mapEntryDesc = mapDescription.getEntryType();
			
			for (Map.Entry<String,JsonElement> entry : mapObject.entrySet()) {
				
				PrefabVariableKeyNode keyNode = new PrefabVariableKeyNode(entry.getKey(), mapKeyDesc);
				keyNode.setName(entry.getKey());
				keyNode.setEditable(false);
				variableNode.addChild(keyNode);
				
				try {
					JsonElement keyElement = new JsonPrimitive(entry.getKey());// = parseKeyString(mapKeyDesc, entry.getKey());
					
					deserializePrefabVariable(keyNode, keyElement, sharedDir);
					
					PrefabVariableNode valueNode = new PrefabVariableNode("value", mapEntryDesc);
					keyNode.addChild(valueNode);
					
					try {
						deserializePrefabVariable(valueNode, entry.getValue(), sharedDir);
					} catch (Exception e) {
					}
				} catch (Exception e) {
				}
			}
			break;
		case NUMBER:
			NumberDescription numberDescription = (NumberDescription) variableNode.getVariableDescription();
			switch (numberDescription.getNumberType()) {
			case BYTE:
				variableNode.setVariableValue(element.getAsByte());
				break;
			case DOUBLE:
				variableNode.setVariableValue(element.getAsDouble());
				break;
			case FLOAT:
				variableNode.setVariableValue(element.getAsFloat());
				break;
			case INT:
				variableNode.setVariableValue(element.getAsInt());
				break;
			case LONG:
				variableNode.setVariableValue(element.getAsLong());
				break;
			case SHORT:
				variableNode.setVariableValue(element.getAsShort());
				break;
			}
			break;
		case STRING:
			variableNode.setVariableValue(element.getAsString());
			break;
		}
	}

	public static JsonElement serializePrefabVariable(PrefabVariableNode variableNode) {
		
		if (variableNode.getVariableValue() == null)
			return null;
		
		//TODO: Barrier for Stack Overflow
		switch (variableNode.getVariableType()) {
		case BOOLEAN:
			return new JsonPrimitive((boolean)variableNode.getVariableValue());
		case CHAR:
			return new JsonPrimitive((char)variableNode.getVariableValue());
		case STRING:
			return new JsonPrimitive(variableNode.getVariableValue().toString());
		case ENUM:
			return new JsonPrimitive(variableNode.getVariableValue().toString());
		case NUMBER:
			NumberDescription numberDescription = (NumberDescription) variableNode.getVariableDescription();
			switch (numberDescription.getNumberType()) {
			case BYTE:
				return new JsonPrimitive((byte)variableNode.getVariableValue());
			case DOUBLE:
				return new JsonPrimitive((double)variableNode.getVariableValue());
			case FLOAT:
				return new JsonPrimitive((float)variableNode.getVariableValue());
			case INT:
				return new JsonPrimitive((int)variableNode.getVariableValue());
			case LONG:
				return new JsonPrimitive((long)variableNode.getVariableValue());
			case SHORT:
				return new JsonPrimitive((short)variableNode.getVariableValue());
			default:
				return null;
			}
		case COMPONENT:
			if (!variableNode.hasChildren())
				return null;
			JsonObject componentObject = new JsonObject();
			for (INode child : variableNode.getChildren()) {
				if (child instanceof PrefabVariableNode) {
					PrefabVariableNode childVariable = (PrefabVariableNode) child;
					try {
						JsonElement childVariableObject = serializePrefabVariable(childVariable);
						componentObject.add(child.getName(), childVariableObject);
					} catch (Exception e) {
					}
				}
			}
			return componentObject;
		case LIST:
		case SET:
			if (!variableNode.hasChildren())
				return null;
			JsonArray listArray = new JsonArray();
			for (INode child : variableNode.getChildren()) {
				if (child instanceof PrefabVariableNode) {
					PrefabVariableNode childVariable = (PrefabVariableNode) child;
					JsonElement childVariableObject = null;
					try {
						childVariableObject = serializePrefabVariable(childVariable);
					} catch (Exception e) {
					}
					listArray.add(childVariableObject);
				}
			}
			return listArray;
		case MAP:
			if (!variableNode.hasChildren())
				return null;
			JsonObject mapObject = new JsonObject();
			for (INode child : variableNode.getChildren()) {
				if (child instanceof PrefabVariableNode) {
					
					PrefabVariableNode mapKeyVariable = (PrefabVariableNode) child;
					PrefabVariableNode mapEntryVariable = (PrefabVariableNode) mapKeyVariable.getChildren().get(0);
					
					try {
						JsonElement childKeyVariableObject = serializePrefabVariable(mapKeyVariable);
						JsonElement mapEntryObject = null;
						try {
							mapEntryObject = serializePrefabVariable(mapEntryVariable);
						} catch (Exception e) {
						}
						mapObject.add(childKeyVariableObject.toString(), mapEntryObject);
					} catch (Exception e) {
					}
				}
			}
			return mapObject;
		default:
			return null;
		}
	}
	
	public static void savePrefab(INode node, String sharedDir) {
		SavePrefabTreeVisitor visitor = new SavePrefabTreeVisitor(sharedDir);
		node.accept(visitor);
		
	}
	
	private static class SavePrefabTreeVisitor implements IVisitor {
		private File prefabMetaDir;
		private File prefabDataDir;

		public SavePrefabTreeVisitor(String sharedDir) {
			this.prefabMetaDir = getPrefabMetaDir(sharedDir);
			this.prefabDataDir = getPrefabDataDir(sharedDir);
		}

		@Override
		public void visit(PrefabRootNode prefabRootNode) {
			for (INode node : prefabRootNode.getChildren()) {
				node.accept(this);
			}
		}
		
		@Override
		public void visit(PrefabNode prefabNode) {
			
			
			JsonObject prefabMeta = new JsonObject();
			prefabMeta.addProperty(TYPE_PROP_KEY, prefabNode.getTemplateType());
			
			JsonObject prefabObject = new JsonObject();
			for (INode node : prefabNode.getChildren()) {
				if (node instanceof PrefabNode) {
					node.accept(this);
				} else if (node instanceof PrefabVariableNode) {
					PrefabVariableNode variableNode = (PrefabVariableNode) node;
					
					if (variableNode.getVariableValue() == null) 
						continue;
					
					try {
						JsonElement variableElement = serializePrefabVariable(variableNode);
						prefabObject.add(variableNode.getName(), variableElement);
					} catch (Exception e) {
					}
				}
			}
			
			Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();

			File file = new File(prefabDataDir + "/" + prefabNode.getID());
			String data = gson.toJson(prefabObject);
			saveFile(file, data);
			
			file = new File(prefabMetaDir + "/" + prefabNode.getID());
			data = gson.toJson(prefabMeta);
			saveFile(file, data);
		}



		@Override
		public void visit(EnumRootNode enumRootNode) {
		}
		@Override
		public void visit(EnumNode enumNode) {
		}
		@Override
		public void visit(EntityTemplateNode node) {
		}
		@Override
		public void visit(EntityTemplateRootNode rootNode) {
		}
	}
	
	private static String[] deserializePrefabID(String prefabName) {
		String[] id = prefabName.split("\\.");
		if (id.length == 0)
			return new String[]{prefabName};
		return id;
	}
	
	private static void saveFile(File file, String data) {
		try {
			semaphore.acquire();
			try {
				Files.write(file.toPath(), data.getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			semaphore.release();
		}
	}

	private static String loadFile(File file) {
		String data = null;
		try {
			semaphore.acquire();
			try {
				data = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			semaphore.release();
		}
		return data;
	}
	
	private static File getPrefabMetaDir(String sharedDir) {
		File prefabDir = getPrefabDir(sharedDir);
		File prefabDataDir = new File(prefabDir + "/meta/");
		if (!prefabDataDir.exists())
			prefabDataDir.mkdir();
		return prefabDataDir;
	}
	
	private static File getPrefabDataDir(String sharedDir) {
		File prefabDir = getPrefabDir(sharedDir);
		File prefabDataDir = new File(prefabDir + "/data/");
		if (!prefabDataDir.exists())
			prefabDataDir.mkdir();
		return prefabDataDir;
	}
	
	private static File getPrefabDir(String sharedDir) {
		File modelDir = getModelDir(sharedDir);
		File prefabDir = new File(modelDir + "/prefabs/");
		if (!prefabDir.exists())
			prefabDir.mkdir();
		return prefabDir;
	}
}
