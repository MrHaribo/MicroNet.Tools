package micronet.tools.model.variables;

public class VariableDescription {
	private VariableType type;

	public VariableDescription(VariableType type) {
		this.type = type;
	}

	public VariableType getType() {
		return type;
	}
	
	@Override
	public String toString() {
		return type.toString();
	}
	
	@Override
	public boolean equals(Object other) {
		if ((this == other))
	        return true;
	    if ((other == null))
	        return false;
	    if (!(other instanceof VariableDescription))
	        return false;
	    VariableDescription castOther = (VariableDescription) other;
		return getType().equals(castOther.getType());
	}
}
