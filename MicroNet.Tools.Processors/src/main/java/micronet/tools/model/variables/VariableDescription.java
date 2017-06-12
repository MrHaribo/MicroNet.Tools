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
}
