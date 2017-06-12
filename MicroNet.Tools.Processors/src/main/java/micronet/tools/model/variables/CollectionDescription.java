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
}
