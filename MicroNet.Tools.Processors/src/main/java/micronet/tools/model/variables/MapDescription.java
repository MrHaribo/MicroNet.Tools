package micronet.tools.model.variables;

public class MapDescription extends CollectionDescription {

	VariableDescription keyType;

	public MapDescription(VariableDescription keyType, VariableDescription entryType) {
		super(VariableType.MAP, entryType);
		this.keyType = keyType;
	}

	public VariableDescription getKeyType() {
		return keyType;
	}

	public void setKeyType(VariableDescription keyType) {
		this.keyType = keyType;
	}
	
	@Override
	public boolean equals(Object other) {
		if (!super.equals(other))
			return false;
	    if (!(other instanceof MapDescription))
	        return false;
	    MapDescription castOther = (MapDescription) other;
	    return getKeyType().equals(castOther.getKeyType());
	}
	
	@Override
	public String toString() {
		return "MAP<" + keyType + "," + entryType + ">";
	}
}
