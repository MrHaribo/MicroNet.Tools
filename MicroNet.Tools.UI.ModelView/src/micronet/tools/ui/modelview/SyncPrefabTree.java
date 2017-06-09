package micronet.tools.ui.modelview;

import static micronet.tools.ui.modelview.ModelConstants.TYPE_PROP_KEY;
import static micronet.tools.ui.modelview.ModelConstants.getModelDir;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.concurrent.Semaphore;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import micronet.tools.ui.modelview.nodes.EntityTemplateNode;
import micronet.tools.ui.modelview.nodes.EntityTemplateRootNode;
import micronet.tools.ui.modelview.nodes.EntityVariableNode;
import micronet.tools.ui.modelview.nodes.EnumNode;
import micronet.tools.ui.modelview.nodes.EnumRootNode;
import micronet.tools.ui.modelview.nodes.ModelNode;
import micronet.tools.ui.modelview.nodes.PrefabNode;
import micronet.tools.ui.modelview.nodes.PrefabRootNode;
import micronet.tools.ui.modelview.nodes.PrefabVariableNode;
import micronet.tools.ui.modelview.variables.ComponentDescription;
import micronet.tools.ui.modelview.variables.NumberDescription;

public class SyncPrefabTree {
	
	private static Semaphore semaphore = new Semaphore(1);
	
	public static void loadPrefab(PrefabNode prefabNode, String sharedDir) {
	
	}
	
	public static void removePrefab(PrefabNode node, String sharedDir) {
		
		for (INode childNode : node.getChildren()) {
			if (childNode instanceof PrefabNode) {
				removePrefab((PrefabNode)node, sharedDir);
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
				return comparison;
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
			System.out.println("h23");
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
		
		PrefabNode prefabNode = new PrefabNode(prefabName, prefabType);
		
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
	
	private static void deserializePrefabVariable(PrefabVariableNode variableNode, JsonElement element, String sharedDir) {

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
		case ENUM:
			variableNode.setVariableValue(element.getAsString());
			break;
		case LIST:
			break;
		case MAP:
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
		case SET:
			break;
		case STRING:
			variableNode.setVariableValue(element.getAsString());
			break;
		}
	}

	private static JsonElement serializePrefabVariable(PrefabVariableNode variableNode) {
		
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
		case SET:
			break;
		case LIST:
			break;
		case MAP:
			break;
		default:
			return null;
		
		}
		
		return null;
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
			
			Gson gson = new GsonBuilder().setPrettyPrinting().create();

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
			try (PrintWriter printer = new PrintWriter(file)) {
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

	private static String loadFile(File file) {
		String data = null;
		try {
			semaphore.acquire();
			try (Scanner scanner = new Scanner(file)) {
				scanner.useDelimiter("\\A");
				data = scanner.next();
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
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
