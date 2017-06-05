package micronet.tools.ui.modelview;

import static micronet.tools.ui.modelview.ModelConstants.ENTITY_TEMPLATES_KEY;
import static micronet.tools.ui.modelview.ModelConstants.NAME_PROP_KEY;
import static micronet.tools.ui.modelview.ModelConstants.PARENT_PROP_KEY;
import static micronet.tools.ui.modelview.ModelConstants.VARIABLES_PROP_KEY;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;
import java.util.concurrent.Semaphore;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import micronet.tools.ui.modelview.nodes.EntityTemplateNode;
import micronet.tools.ui.modelview.nodes.EntityVariableNode;
import micronet.tools.ui.modelview.nodes.EnumNode;
import micronet.tools.ui.modelview.nodes.EnumRootNode;
import micronet.tools.ui.modelview.variables.CollectionDescription;
import micronet.tools.ui.modelview.variables.MapDescription;
import micronet.tools.ui.modelview.variables.NumberDescription;
import micronet.tools.ui.modelview.variables.NumberType;
import micronet.tools.ui.modelview.variables.VariableDescription;
import micronet.tools.ui.modelview.variables.VariableType;

public class SyncTemplateTree {

	private static Semaphore semaphore = new Semaphore(1);

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
					templateNames.add(templateObject.getAsJsonObject().getAsJsonPrimitive(NAME_PROP_KEY).toString());
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

	public static EntityTemplateNode loadTemplateTree(String sharedDir) {

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

	private static EntityTemplateNode constructTemplateTree(List<JsonElement> templateFileObjects) {

		Map<String, List<JsonElement>> parentMapping = new HashMap<>();
		Stack<JsonElement> parentObjects = new Stack<>();
		Map<String, EntityTemplateNode> processedNodes = new HashMap<>();

		EntityTemplateNode templateTreeRoot = new EntityTemplateNode(ENTITY_TEMPLATES_KEY);

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
	
	public static void saveTemplateTree(EntityTemplateNode rootNode, String sharedDir) {
		SaveTemplateTreeVisitor visitor = new SaveTemplateTreeVisitor(sharedDir);
		visitor.visit(rootNode);
	}

	private static class SaveTemplateTreeVisitor implements IVisitor {
		private File templateDir;

		public SaveTemplateTreeVisitor(String sharedDir) {
			this.templateDir = getTemplateDir(sharedDir);
		}

		@Override
		public void visit(EntityTemplateNode node) {

			JsonObject template = new JsonObject();

			String parentName = node.getParent() != null ? node.getParent().getName() : null;
			parentName = parentName != ENTITY_TEMPLATES_KEY ? parentName : null;
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

			if (node.getName().equals(ENTITY_TEMPLATES_KEY))
				return;

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
	}
	
	private static EntityVariableNode deserializeVariable(JsonObject variableObject) {
		String variableName = variableObject.getAsJsonPrimitive(ModelConstants.NAME_PROP_KEY).getAsString();
		VariableType variableType = Enum.valueOf(VariableType.class, variableObject.getAsJsonPrimitive(ModelConstants.TYPE_PROP_KEY).getAsString());
		
		EntityVariableNode variableNode = new EntityVariableNode(variableName);
		VariableDescription variabelDescription = null;
		
		switch (variableType) {
		case LIST:
			String listEntryType = variableObject.getAsJsonPrimitive(ModelConstants.ENTRY_TYPE_PROP_KEY).getAsString();
			variabelDescription = new CollectionDescription(VariableType.LIST, listEntryType);
			break;
		case SET:
			String setEntryType = variableObject.getAsJsonPrimitive(ModelConstants.ENTRY_TYPE_PROP_KEY).getAsString();
			variabelDescription = new CollectionDescription(VariableType.SET, setEntryType);
			break;
		case NUMBER:
			String numberType = variableObject.getAsJsonPrimitive(ModelConstants.NUMBER_TYPE_PROP_KEY).getAsString();
			variabelDescription = new NumberDescription(Enum.valueOf(NumberType.class, numberType));
			break;
		case MAP:
			String keyType = variableObject.getAsJsonPrimitive(ModelConstants.KEY_TYPE_PROP_KEY).getAsString();
			String mapEntryType = variableObject.getAsJsonPrimitive(ModelConstants.ENTRY_TYPE_PROP_KEY).getAsString();
			variabelDescription = new MapDescription(keyType, mapEntryType);
			break;
		case BOOLEAN:
		case CHAR:
		case COMPONENT:
		case ENUM:
		case REF:
		case STRING:
			variabelDescription = new VariableDescription(variableType);
			break;
		}
		
		variableNode.setVariabelDescription(variabelDescription);
		return variableNode;
	}
	
	private static JsonObject serializeVariableDescription(EntityVariableNode variableNode) {
		
		JsonObject variableDesc = new JsonObject();
		variableDesc.addProperty(ModelConstants.NAME_PROP_KEY, variableNode.getName());
		variableDesc.addProperty(ModelConstants.TYPE_PROP_KEY, variableNode.getVariabelDescription().getType().toString());
		
		switch (variableNode.getVariabelDescription().getType()) {
		case BOOLEAN:
		case CHAR:
		case COMPONENT:
		case ENUM:
		case REF:
		case STRING:
			return variableDesc;
		case LIST:
		case SET:
			CollectionDescription collectionDesc = (CollectionDescription) variableNode.getVariabelDescription();
			variableDesc.addProperty(ModelConstants.ENTRY_TYPE_PROP_KEY, collectionDesc.getEntryType());
			break;
		case NUMBER:
			NumberDescription numDesc = (NumberDescription) variableNode.getVariabelDescription();
			variableDesc.addProperty(ModelConstants.NUMBER_TYPE_PROP_KEY, numDesc.getNumberType().toString());
			break;
		case MAP:
			MapDescription mapDesc = (MapDescription) variableNode.getVariabelDescription();
			variableDesc.addProperty(ModelConstants.KEY_TYPE_PROP_KEY, mapDesc.getKeyType());
			variableDesc.addProperty(ModelConstants.ENTRY_TYPE_PROP_KEY, mapDesc.getEntryType());
			break;
		}
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