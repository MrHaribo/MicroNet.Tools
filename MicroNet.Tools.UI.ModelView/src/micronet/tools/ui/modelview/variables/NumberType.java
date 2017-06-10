package micronet.tools.ui.modelview.variables;

public enum NumberType {
	BYTE,
	SHORT,
	INT,
	LONG,
	FLOAT,
	DOUBLE;
	
	public static boolean isNumberTypeName(String typeName) {
		try {
			Enum.valueOf(NumberType.class, typeName.toUpperCase());
		} catch (IllegalArgumentException e) {
			return false;
		}
		return true;
	}
}
