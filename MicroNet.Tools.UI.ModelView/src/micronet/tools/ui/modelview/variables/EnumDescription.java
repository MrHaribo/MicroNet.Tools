package micronet.tools.ui.modelview.variables;

public class EnumDescription extends VariableDescription {

	private String enumType;
	
	public EnumDescription(String enumType) {
		super(VariableType.ENUM);
		this.enumType = enumType;
	}

	public String getEnumType() {
		return enumType;
	}

	public void setEnumType(String enumType) {
		this.enumType = enumType;
	}
	
	@Override
	public String toString() {
		return enumType;
	}
}
