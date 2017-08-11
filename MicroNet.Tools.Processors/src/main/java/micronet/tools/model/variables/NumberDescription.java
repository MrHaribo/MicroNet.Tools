package micronet.tools.model.variables;

public class NumberDescription extends VariableDescription  {

	private NumberType numberType;

	public NumberDescription(NumberType numberType) {
		super(VariableType.NUMBER);
		this.numberType = numberType;
	}

	public NumberType getNumberType() {
		return numberType;
	}

	public void setNumberType(NumberType numberType) {
		this.numberType = numberType;
	}
	
	@Override
	public String toString() {
		return numberType.toString();
	}
	
	@Override
	public boolean equals(Object other) {
		if (!super.equals(other))
			return false;
	    if (!(other instanceof NumberDescription))
	        return false;
	    NumberDescription castOther = (NumberDescription) other;
		return getNumberType().equals(castOther.getNumberType());
	}
}
