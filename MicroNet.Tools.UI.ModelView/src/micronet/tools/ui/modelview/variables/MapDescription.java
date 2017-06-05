package micronet.tools.ui.modelview.variables;

public class MapDescription extends CollectionDescription {

	String keyType;

	public MapDescription(String keyType, String entryType) {
		super(VariableType.MAP, entryType);
		this.keyType = keyType;
	}

	public String getKeyType() {
		return keyType;
	}
}
