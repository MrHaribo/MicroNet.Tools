package micronet.tools.model;

import java.io.File;

import micronet.tools.model.variables.NumberType;
import micronet.tools.model.variables.VariableType;

public class ModelConstants {
	public static final String ENUM_DEFINITION_ROOT_KEY = "Enum Definitions";
	public static final String ENTITY_TEMPLATE_ROOT_KEY = "Entity Templates";
	public static final String PREFAB_ROOT_KEY = "Entity Prefabs";
	public static final String SCRIPTS_ROOT_KEY = "Scripts";
	public static final String VARIABLES_PROP_KEY = "variables";
	public static final String NAME_PROP_KEY = "name";
	public static final String ID_PROP_KEY = "id";
	public static final String TYPE_PROP_KEY = "type";
	public static final String NUMBER_TYPE_PROP_KEY = "numberType";
	public static final String ENTRY_TYPE_PROP_KEY = "entryType";
	public static final String KEY_TYPE_PROP_KEY = "keyType";
	public static final String PARENT_PROP_KEY = "parent";
	public static final String CTOR_ARGUMENT_PROP_KEY = "ctorArg";
	public static final String CONST_VARIABLE_PROP_KEY = "constVar";
	
	public static File getModelDir(String sharedDir) {
		File modelDir = new File(sharedDir + "model/");
		if (!modelDir.exists())
			modelDir.mkdir();
		return modelDir;
	}
	


	public static VariableType getVariableEntryTypeFromName(String name) {
		VariableType variableType = null;
		try {
			variableType = Enum.valueOf(VariableType.class, name);
		} catch (IllegalArgumentException e) {
		}
		return variableType;
	}
	
	
		
	public static NumberType getNumberEntryTypeFromName(String name) {
		NumberType numberType = null;
		try {
			numberType = Enum.valueOf(NumberType.class, name);
		} catch (IllegalArgumentException e) {
		}
		return numberType;
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
