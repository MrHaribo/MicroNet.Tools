package micronet.tools.filesync;

import java.io.File;
import java.io.IOException;

import micronet.tools.model.nodes.ScriptNode;
import micronet.tools.model.nodes.ScriptRootNode;

public class SyncScripts {
	
	public static boolean scriptExists(String name, String sharedDir) {
		File scriptFile = new File (getScriptFileName(sharedDir, name));
		return scriptFile.exists();
	}
	
	public static String getScriptFileName(String name, String sharedDir) {
		return String.format("%s/%s.js", getScriptDirName(sharedDir), name);
	}
	
	public static String getScriptDirName(String sharedDir) {
		String dirName = String.format("%s/scripts/", sharedDir);
		File dir = new File(dirName);
		if (!dir.exists())
			dir.mkdirs();
		return dirName;
	}
	
	public static void createScript(String name, String sharedDir) {
		try {
			File scriptFile = new File (getScriptFileName(name, sharedDir));
			scriptFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void deleteScript(String name, String sharedDir) {
		File scriptFile = new File (getScriptFileName(name, sharedDir));
		scriptFile.delete();
	}
	
	public static ScriptNode loadScript(String name, String sharedDir) {
		if (!scriptExists(name, sharedDir))
			createScript(name, sharedDir);
			
		ScriptNode node = new ScriptNode(name);
		return node;
	}
	
	public static ScriptRootNode loadScripts(String sharedDir) {
		ScriptRootNode scriptRoot = new ScriptRootNode();
		File dir = new File(getScriptDirName(sharedDir));
		for (File scriptFile : dir.listFiles()) {
			String name = scriptFile.getName().replaceAll(".js", "");
			ScriptNode node = new ScriptNode(name);
			scriptRoot.addChild(node);
		}
		return scriptRoot;
	}
}
