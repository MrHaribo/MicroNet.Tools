package micronet.tools.ui.modelview;

import static micronet.tools.ui.modelview.ModelConstants.TYPE_PROP_KEY;
import static micronet.tools.ui.modelview.ModelConstants.getModelDir;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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
import micronet.tools.ui.modelview.nodes.EnumNode;
import micronet.tools.ui.modelview.nodes.EnumRootNode;
import micronet.tools.ui.modelview.nodes.PrefabNode;
import micronet.tools.ui.modelview.nodes.PrefabRootNode;
import micronet.tools.ui.modelview.nodes.PrefabVariableNode;
import micronet.tools.ui.modelview.variables.NumberDescription;

public class SyncPrefabTree {
	
	private static Semaphore semaphore = new Semaphore(1);
	
	public static void loadPrefab(PrefabNode prefabNode, String sharedDir) {
	
	}
	
	public static PrefabRootNode loadPrefabTree(String sharedDir) {
		
		File enumDir = getPrefabDir(sharedDir);
		File[] directoryListing = enumDir.listFiles();
		if (directoryListing == null)
			return null;
		
		Map<String[], PrefabNode> prefabMap = new TreeMap<>(new Comparator<String[]>() {
			@Override
			public int compare(String[] o1, String[] o2) {
				int comparison = o1.length - o2.length;
				return comparison;
			}
		});

		for (File prefabFile : directoryListing) {
			
			String data = loadPrefabFile(prefabFile);
			if (data == null)
				return null;
			
			String[] prefabID = deserializePrefabID(prefabFile.getName());
			PrefabNode prefabNode = parsePrefab(prefabID, data, sharedDir);
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
	

	
	public static void savePrefab(INode node, String sharedDir) {
		SavePrefabTreeVisitor visitor = new SavePrefabTreeVisitor(sharedDir);
		node.accept(visitor);
		
	}
	
	private static PrefabNode parsePrefab(String[] prefabID, String data, String sharedDir) {
		
		JsonParser parser = new JsonParser();
		JsonObject prefabObject = parser.parse(data).getAsJsonObject();

		//TODO: Get Type from other file
		String prefabType = prefabObject.getAsJsonPrimitive(TYPE_PROP_KEY).getAsString();
		String prefabName = prefabID[prefabID.length-1];
		
		PrefabNode prefabNode = new PrefabNode(prefabName, prefabType);
		
		deserializePrefabVariables(prefabObject, prefabNode);
		
		return prefabNode;
	}
	
	private static void deserializePrefabVariables(JsonObject prefabObject, PrefabNode prefabNode) {
		
		for (INode childNode : prefabNode.getChildren()) {
			
			if (childNode instanceof PrefabVariableNode) {
				
				PrefabVariableNode variableNode = (PrefabVariableNode)childNode;
				
				JsonElement element = prefabObject.get(variableNode.getName());
				if (element == null)
					continue;
				
				deserializePrefabVariable(variableNode, element);
			}
		}
	}
	
	private static void deserializePrefabVariable(PrefabVariableNode variableNode, JsonElement element) {
		
		switch (variableNode.getVariableType()) {
		case BOOLEAN:
			variableNode.setVariableValue(element.getAsBoolean());
			break;
		case CHAR:
			variableNode.setVariableValue(element.getAsCharacter());
			break;
		case COMPONENT:
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
		case REF:
			break;
		case SET:
			break;
		case STRING:
			break;
		}
	}

	private static JsonElement serializePrefabVariable(PrefabVariableNode variableNode) {
		
		switch (variableNode.getVariableType()) {
		case BOOLEAN:
			return new JsonPrimitive((boolean)variableNode.getVariableValue());
		case CHAR:
			return new JsonPrimitive((char)variableNode.getVariableValue());
		case COMPONENT:
			break;
		case ENUM:
			return new JsonPrimitive(variableNode.getVariableValue().toString());
		case LIST:
			break;
		case MAP:
			break;
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
		case REF:
			break;
		case SET:
			break;
		case STRING:
			break;
		default:
			return null;
		
		}
		
		return null;
	}
	
	private static class SavePrefabTreeVisitor implements IVisitor {
		private File prefabDir;

		public SavePrefabTreeVisitor(String sharedDir) {
			this.prefabDir = getPrefabDir(sharedDir);
		}

		@Override
		public void visit(PrefabRootNode prefabRootNode) {
			for (INode node : prefabRootNode.getChildren()) {
				node.accept(this);
			}
		}
		
		@Override
		public void visit(PrefabNode prefabNode) {
			
			String prefabID = serializePrefabID(prefabNode);
			JsonObject prefabObject = new JsonObject();
			
			prefabObject.addProperty(TYPE_PROP_KEY, prefabNode.getTemplateType());
			
			for (INode node : prefabNode.getChildren()) {
				if (node instanceof PrefabNode) {
					node.accept(this);
				} else if (node instanceof PrefabVariableNode) {
					PrefabVariableNode variableNode = (PrefabVariableNode) node;
					
					if (variableNode.getVariableValue() == null) 
						continue;
					
					JsonElement variableElement = serializePrefabVariable(variableNode);
					prefabObject.add(variableNode.getName(), variableElement);
				}
			}
			
			File prefabFile = new File(prefabDir + "/" + prefabID);
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			String data = gson.toJson(prefabObject);

			try {
				semaphore.acquire();
				try (PrintWriter printer = new PrintWriter(prefabFile)) {
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
		public void visit(EntityTemplateNode node) {
		}
		@Override
		public void visit(EntityTemplateRootNode rootNode) {
		}
	}

	private static String serializePrefabID(PrefabNode prefabNode) {
		List<String> prefabNameArray = new ArrayList<>(); 
		prefabNameArray.add(prefabNode.getName());
		
		INode parent = prefabNode.getParent();
		while (parent != null && !(parent instanceof PrefabRootNode)) {
			prefabNameArray.add(parent.getName());
			parent = parent.getParent();
		}
		Collections.reverse(prefabNameArray);
		return String.join(".", prefabNameArray);
	}
	
	private static String[] deserializePrefabID(String prefabName) {
		String[] id = prefabName.split("\\.");
		if (id.length == 0)
			return new String[]{prefabName};
		return id;
	}

	private static String loadPrefabFile(File file) {
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
	
	private static File getPrefabDir(String sharedDir) {
		File modelDir = getModelDir(sharedDir);
		File prefabDir = new File(modelDir + "/prefabs/");
		if (!prefabDir.exists())
			prefabDir.mkdir();
		return prefabDir;
	}
}
