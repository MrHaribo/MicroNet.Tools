package micronet.tools.model.variables;

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
	
	@Override
	public boolean equals(Object other) {
		if (!super.equals(other))
			return false;
	    if (!(other instanceof EnumDescription))
	        return false;
	    EnumDescription castOther = (EnumDescription) other;
	    return getEnumType().equals(castOther.getEnumType());
	}
}
