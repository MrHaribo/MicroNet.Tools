package micronet.tools.ui.modelview;

import java.io.File;

public class ModelConstants {
	public static final String ENUM_DEFINITIONS_KEY = "Enum Definitions";
	public static final String ENTITY_TEMPLATES_KEY = "Entity Templates";
	public static final String VARIABLES_PROP_KEY = "variables";
	public static final String NAME_PROP_KEY = "name";
	public static final String TYPE_PROP_KEY = "type";
	public static final String NUMBER_TYPE_PROP_KEY = "number_type";
	public static final String ENTRY_TYPE_PROP_KEY = "entry_type";
	public static final String KEY_TYPE_PROP_KEY = "key_type";
	public static final String PARENT_PROP_KEY = "parent";
	
	public static File getModelDir(String sharedDir) {
		File modelDir = new File(sharedDir + "model/");
		if (!modelDir.exists())
			modelDir.mkdir();
		return modelDir;
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
