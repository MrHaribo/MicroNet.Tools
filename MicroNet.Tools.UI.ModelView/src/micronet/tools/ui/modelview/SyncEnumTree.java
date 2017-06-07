package micronet.tools.ui.modelview;

import static micronet.tools.ui.modelview.ModelConstants.NAME_PROP_KEY;
import static micronet.tools.ui.modelview.ModelConstants.VARIABLES_PROP_KEY;
import static micronet.tools.ui.modelview.ModelConstants.getModelDir;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import micronet.tools.ui.modelview.nodes.EntityTemplateNode;
import micronet.tools.ui.modelview.nodes.EntityTemplateRootNode;
import micronet.tools.ui.modelview.nodes.EnumNode;
import micronet.tools.ui.modelview.nodes.EnumRootNode;
import micronet.tools.ui.modelview.nodes.PrefabNode;
import micronet.tools.ui.modelview.nodes.PrefabRootNode;

public class SyncEnumTree {

	private static Semaphore semaphore = new Semaphore(1);

	public static EnumRootNode loadEnumTree(String sharedDir) {
		File enumDir = getEnumDir(sharedDir);
		File[] directoryListing = enumDir.listFiles();
		if (directoryListing == null)
			return null;

		EnumRootNode rootNode = new EnumRootNode();

		for (File enumFile : directoryListing) {
			
			String data = loadEnumFile(enumFile);
			if (data == null)
				return null;
			
			EnumNode enumNode = parseEnumFile(data);
			rootNode.addChild(enumNode);
		}

		return rootNode;
	}
	
	public static EnumNode loadEnum(String enumType, String sharedDir) {
		File enumDir = getEnumDir(sharedDir);
		File enumFile = new File(enumDir + "/" + enumType);
		if (!enumFile.exists())
			return null;

		String data = loadEnumFile(enumFile);
		if (data == null)
			return null;
		
		return parseEnumFile(data);
	}

	private static EnumNode parseEnumFile(String data) {
		JsonParser parser = new JsonParser();
		JsonObject enumObject = parser.parse(data).getAsJsonObject();

		String enumName = enumObject.getAsJsonPrimitive(NAME_PROP_KEY).getAsString();
		JsonArray enumConstants = enumObject.getAsJsonArray(VARIABLES_PROP_KEY);

		EnumNode enumNode = new EnumNode(enumName);
		for (JsonElement enumConstant : enumConstants) {
			enumNode.getEnumConstants().add(enumConstant.getAsString());
		}
		return enumNode;
	}

	private static String loadEnumFile(File enumFile) {
		String data = null;
		try {
			semaphore.acquire();
			try (Scanner scanner = new Scanner(enumFile)) {
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

	public static boolean enumExists(String name, String sharedDir) {

		File enumDir = getEnumDir(sharedDir);
		File enumFile = new File(enumDir + "/" + name);
		return enumFile.exists();
	}
	
	public static void removeEnum(INode node, String sharedDir) {
		File enumDir = getEnumDir(sharedDir);
		File enumFile = new File(enumDir + "/" + node.getName());
		enumFile.delete();
	}

	public static void saveEnumTree(EnumRootNode rootNode, String sharedDir) {
		SaveModelTreeVisitor visitor = new SaveModelTreeVisitor(sharedDir);
		visitor.visit(rootNode);
	}

	public static void saveEnumNode(EnumNode node, String sharedDir) {
		SaveModelTreeVisitor visitor = new SaveModelTreeVisitor(sharedDir);
		node.accept(visitor);
	}

	private static class SaveModelTreeVisitor implements IVisitor {
		private File enumDir;

		public SaveModelTreeVisitor(String sharedDir) {
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
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			String data = gson.toJson(enumDefinition);

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
		}
		@Override
		public void visit(EntityTemplateRootNode rootNode) {
		}
		@Override
		public void visit(PrefabNode prefabNode) {
		}
		@Override
		public void visit(PrefabRootNode prefabRootNode) {
		}
	}

	private static File getEnumDir(String sharedDir) {
		File modelDir = getModelDir(sharedDir);
		File templateDir = new File(modelDir + "/enums/");
		if (!templateDir.exists())
			templateDir.mkdir();
		return templateDir;
	}



}
