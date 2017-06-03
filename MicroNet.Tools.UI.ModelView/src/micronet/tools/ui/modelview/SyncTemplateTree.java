package micronet.tools.ui.modelview;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;
import java.util.TreeSet;
import java.util.concurrent.Semaphore;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import micronet.serialization.Serialization;
import micronet.tools.annotation.api.ServiceAPI;
import micronet.tools.annotation.codegen.CodegenConstants;

public class SyncTemplateTree {

	public static final String ENTITY_TEMPLATES_KEY = "Entity Templates";
	private static final String VARIABLES_PROP_KEY = "variables";
	private static final String NAME_PROP_KEY = "name";
	private static final String PARENT_PROP_KEY = "parent";

	private static Semaphore semaphore = new Semaphore(1);

	public static EntityTemplateNode loadTemplateTree(String sharedDir) {

		File modelDir = new File(sharedDir + "model/");
		if (!modelDir.exists())
			return null;
		File[] directoryListing = modelDir.listFiles();
		if (directoryListing == null)
			return null;

		JsonParser parser = new JsonParser();
		List<JsonElement> templateFileObjects = new ArrayList<>();

		try {
			semaphore.acquire();

			for (File parameterCodeFile : directoryListing) {
				try (Scanner scanner = new Scanner(parameterCodeFile)) {
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
				// Template was added by a parent before (cannot be a root element)
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
				parentNode.addChild(new EntityVariableNode(variable.getAsString()));
			}
		}
		
		return templateTreeRoot;
	}

	public static void saveTemplateTree(EntityTemplateNode rootNode, String sharedDir) {

		File modelDir = new File(sharedDir + "model/");
		if (!modelDir.exists())
			modelDir.mkdir();

		SaveTemplateTreeVisitor visitor = new SaveTemplateTreeVisitor(modelDir + "/");
		visitor.visit(rootNode);
	}

	private static class SaveTemplateTreeVisitor implements IVisitor {
		private String modelDir;

		public SaveTemplateTreeVisitor(String modelDir) {
			this.modelDir = modelDir;
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
					variables.add(childNode.getName());
				}
			}

			if (node.getName().equals(ENTITY_TEMPLATES_KEY))
				return;

			template.add(VARIABLES_PROP_KEY, variables);

			File templateFile = new File(modelDir + node.getName());
			Gson gson = new Gson();
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
	}
}
