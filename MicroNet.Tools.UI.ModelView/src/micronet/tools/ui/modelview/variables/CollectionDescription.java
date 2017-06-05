package micronet.tools.ui.modelview.variables;

public class CollectionDescription extends VariableDescription {

	String entryType;
	
	public CollectionDescription(VariableType type, String entryType) {
		super(type);
		this.entryType = entryType;
	}

	public String getEntryType() {
		return entryType;
	}
}
