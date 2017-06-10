package micronet.tools.ui.modelview.variables;

public enum VariableType {
	STRING,
	NUMBER,
	BOOLEAN,
	CHAR,
	ENUM,
	LIST,
	SET,
	MAP,
	COMPONENT;
	
	public static boolean isVariableTypeName(String typeName) {
		try {
			Enum.valueOf(VariableType.class, typeName.toUpperCase());
		} catch (IllegalArgumentException e) {
			return false;
		}
		return true;
	}
	
	public static boolean isPrimitiveTypeName(String typeName) {
		boolean numberTypeName = NumberType.isNumberTypeName(typeName);
		boolean variableTypeName = isVariableTypeName(typeName);
		return numberTypeName || variableTypeName;
	}
}
