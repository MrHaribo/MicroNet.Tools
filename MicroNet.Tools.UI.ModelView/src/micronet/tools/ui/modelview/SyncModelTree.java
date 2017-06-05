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

public class SyncModelTree {

	public static final String ENUM_DEFINITIONS_KEY = "Enum Definitions";
	public static final String ENTITY_TEMPLATES_KEY = "Entity Templates";
	private static final String VARIABLES_PROP_KEY = "variables";
	private static final String NAME_PROP_KEY = "name";
	private static final String PARENT_PROP_KEY = "parent";

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
	
	public static EnumRootNode loadEnumTree(String sharedDir) {
		File enumDir = getEnumDir(sharedDir);
		File[] directoryListing = enumDir.listFiles();
		if (directoryListing == null)
			return null;

		JsonParser parser = new JsonParser();
		EnumRootNode rootNode = new EnumRootNode(ENUM_DEFINITIONS_KEY);

		for (File enumFile : directoryListing) {
			String data = null;
			try {
				semaphore.acquire();
				try (Scanner scanner = new Scanner(enumFile)) {
					scanner.useDelimiter("\\A");
					data = scanner.next();
				} catch (IOException e) {
					e.printStackTrace();
					continue;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
				continue;
			} finally {
				semaphore.release();
			}

			JsonObject enumObject = parser.parse(data).getAsJsonObject();

			String enumName = enumObject.getAsJsonPrimitive(NAME_PROP_KEY).getAsString();
			JsonArray enumConstants = enumObject.getAsJsonArray(VARIABLES_PROP_KEY);

			EnumNode enumNode = new EnumNode(enumName);

			for (JsonElement enumConstant : enumConstants) {
				enumNode.getEnumConstants().add(enumConstant.getAsString());
			}
			rootNode.addChild(enumNode);
		}

		return rootNode;
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
				parentNode.addChild(new EntityVariableNode(variable.getAsString()));
			}
		}

		return templateTreeRoot;
	}

	public static boolean templateExists(String name, String sharedDir) {
		File templateDir = getTemplateDir(sharedDir);
		File templateFile = new File(templateDir + "/" + name);
		return templateFile.exists();
	}

	public static boolean enumExists(String name, String sharedDir) {

		File templateDir = getEnumDir(sharedDir);
		File templateFile = new File(templateDir + "/" + name);
		return templateFile.exists();
	}

	public static void saveTemplateTree(EntityTemplateNode rootNode, String sharedDir) {
		SaveModelTreeVisitor visitor = new SaveModelTreeVisitor(sharedDir);
		visitor.visit(rootNode);
	}

	public static void saveEnumTree(EnumRootNode rootNode, String sharedDir) {
		SaveModelTreeVisitor visitor = new SaveModelTreeVisitor(sharedDir);
		visitor.visit(rootNode);
	}

	public static void saveEnumNode(EnumNode node, String sharedDir) {
		SaveModelTreeVisitor visitor = new SaveModelTreeVisitor(sharedDir);
		visitor.visit(node);
	}

	private static class SaveModelTreeVisitor implements IVisitor {
		private File templateDir;
		private File enumDir;

		public SaveModelTreeVisitor(String sharedDir) {
			this.templateDir = getTemplateDir(sharedDir);
			this.enumDir = getEnumDir(sharedDir);
		}

		@Override
		public void visit(EnumRootNode enumRootNode) {
			for (INode node : enumRootNode.getChildren()) {
				node.accept(this);
			}
		}

		@Override
		public void visit(EnumNode enumNode) {

			JsonArray enumConstants = new JsonArray();
			for (String enumConstant : enumNode.getEnumConstants()) {
				enumConstants.add(enumConstant);
			}

			JsonObject enumDefinition = new JsonObject();

			enumDefinition.addProperty(NAME_PROP_KEY, enumNode.getName());
			enumDefinition.add(VARIABLES_PROP_KEY, enumConstants);

			File enumFile = new File(enumDir + "/" + enumNode.getName());
			String data = new Gson().toJson(enumDefinition);

			try {
				semaphore.acquire();
				try (PrintWriter printer = new PrintWriter(enumFile)) {
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

			File templateFile = new File(templateDir + "/" + node.getName());
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

	private static File getModelDir(String sharedDir) {
		File modelDir = new File(sharedDir + "model/");
		if (!modelDir.exists())
			modelDir.mkdir();
		return modelDir;
	}

	private static File getTemplateDir(String sharedDir) {
		File modelDir = getModelDir(sharedDir);
		File templateDir = new File(modelDir + "/templates/");
		if (!templateDir.exists())
			templateDir.mkdir();
		return templateDir;
	}

	private static File getEnumDir(String sharedDir) {
		File modelDir = getModelDir(sharedDir);
		File templateDir = new File(modelDir + "/enums/");
		if (!templateDir.exists())
			templateDir.mkdir();
		return templateDir;
	}

	public static boolean isValidJavaIdentifier(String s) {
		// an empty or null string cannot be a valid identifier
		if (s == null || s.length() == 0) {
			return false;
		}

		char[] c = s.toCharArray();
		if (!Character.isJavaIdentifierStart(c[0])) {
			return false;
		}

		for (int i = 1; i < c.length; i++) {
			if (!Character.isJavaIdentifierPart(c[i])) {
				return false;
			}
		}

		return true;
	}
}
