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
}
