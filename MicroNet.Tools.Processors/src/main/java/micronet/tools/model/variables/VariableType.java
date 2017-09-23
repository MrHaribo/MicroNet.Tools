package micronet.tools.model.variables;

public enum VariableType {
	STRING,
	NUMBER,
	BOOLEAN,
	CHAR,
	ENUM,
	LIST,
	SET,
	MAP,
	COMPONENT,
	SCRIPT,
	GEOMETRY;
	
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
