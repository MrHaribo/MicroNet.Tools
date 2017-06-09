package micronet.tools.ui.modelview;

import java.io.File;

import micronet.tools.ui.modelview.variables.CollectionDescription;
import micronet.tools.ui.modelview.variables.ComponentDescription;
import micronet.tools.ui.modelview.variables.NumberDescription;
import micronet.tools.ui.modelview.variables.NumberType;
import micronet.tools.ui.modelview.variables.VariableDescription;
import micronet.tools.ui.modelview.variables.VariableType;

public class ModelConstants {
	public static final String ENUM_DEFINITION_ROOT_KEY = "Enum Definitions";
	public static final String ENTITY_TEMPLATE_ROOT_KEY = "Entity Templates";
	public static final String PREFAB_ROOT_KEY = "Entity Prefabs";
	public static final String VARIABLES_PROP_KEY = "variables";
	public static final String NAME_PROP_KEY = "name";
	public static final String ID_PROP_KEY = "id";
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
	
	public static boolean isPrimitiveTypeName(String typeName) {
		boolean numberTypeName = isNumberTypeName(typeName);
		boolean variableTypeName = isVariableTypeName(typeName);
		return numberTypeName || variableTypeName;
	}
	
	public static boolean isNumberTypeName(String typeName) {
		try {
			Enum.valueOf(NumberType.class, typeName.toUpperCase());
		} catch (IllegalArgumentException e) {
			return false;
		}
		return true;
	}
	
	public static boolean isVariableTypeName(String typeName) {
		try {
			Enum.valueOf(VariableType.class, typeName.toUpperCase());
		} catch (IllegalArgumentException e) {
			return false;
		}
		return true;
	}
	
	public static boolean isTemplateCollection(CollectionDescription desc) {
		VariableType variableType = ModelConstants.getVariableEntryTypeOfCollection(desc);
		NumberType numberType = ModelConstants.getNumberEntryTypeOfCollection(desc);
		return variableType == null && numberType == null;
	}

	public static VariableType getVariableEntryTypeOfCollection(CollectionDescription desc) {
		return getVariableEntryTypeFromName(desc.getEntryType());
	}

	public static VariableType getVariableEntryTypeFromName(String name) {
		VariableType variableType = null;
		try {
			variableType = Enum.valueOf(VariableType.class, name);
		} catch (IllegalArgumentException e) {
		}
		return variableType;
	}
	
	public static NumberType getNumberEntryTypeOfCollection(CollectionDescription desc) {
		return getNumberEntryTypeFromName(desc.getEntryType());
	}
		
	public static NumberType getNumberEntryTypeFromName(String name) {
		NumberType numberType = null;
		try {
			numberType = Enum.valueOf(NumberType.class, name);
		} catch (IllegalArgumentException e) {
		}
		return numberType;
	}
	
	public static VariableDescription getEntryDescription(CollectionDescription listDescription) {
		VariableDescription variableDesc = null;
		if (ModelConstants.isTemplateCollection(listDescription)) {
			variableDesc = new ComponentDescription(listDescription.getEntryType());
		} else {
			NumberType numberType = ModelConstants.getNumberEntryTypeOfCollection(listDescription);
			VariableType variableType = ModelConstants.getVariableEntryTypeOfCollection(listDescription);
			
			if (numberType != null) {
				variableDesc = new NumberDescription(Enum.valueOf(NumberType.class, listDescription.getEntryType()));
			} else if (variableType != null) {
				variableDesc = new VariableDescription(Enum.valueOf(VariableType.class, listDescription.getEntryType()));
			}
		}
		
		return variableDesc;
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
