package micronet.tools.ui.modelview;

import static micronet.tools.ui.modelview.ModelConstants.getModelDir;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
	
	public static void savePrefab(INode node, String sharedDir) {
		SavePrefabTreeVisitor visitor = new SavePrefabTreeVisitor(sharedDir);
		node.accept(visitor);
		
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
			
			String prefabName = getPrefabName(prefabNode);
			JsonObject prefabObject = new JsonObject();
			
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
			
			File prefabFile = new File(prefabDir + "/" + prefabName);
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

	private static String getPrefabName(PrefabNode prefabNode) {
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
	
	private static File getPrefabDir(String sharedDir) {
		File modelDir = getModelDir(sharedDir);
		File prefabDir = new File(modelDir + "/prefabs/");
		if (!prefabDir.exists())
			prefabDir.mkdir();
		return prefabDir;
	}
}
