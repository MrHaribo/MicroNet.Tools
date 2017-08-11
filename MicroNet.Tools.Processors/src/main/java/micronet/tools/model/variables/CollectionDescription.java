package micronet.tools.model.variables;

public class CollectionDescription extends VariableDescription {

	VariableDescription entryType;
	
	public CollectionDescription(VariableType type, VariableDescription entryType) {
		super(type);
		this.entryType = entryType;
	}

	public VariableDescription getEntryType() {
		return entryType;
	}

	public void setEntryType(VariableDescription entryType) {
		this.entryType = entryType;
	}
	
	@Override
	public boolean equals(Object other) {
		if (!super.equals(other))
			return false;
	    if (!(other instanceof CollectionDescription))
	        return false;
	    CollectionDescription castOther = (CollectionDescription) other;
	    return getEntryType().equals(castOther.getEntryType());
	}
	
	@Override
	public String toString() {
		return super.toString() + "<" + entryType + ">";
	}
}
