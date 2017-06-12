package micronet.tools.model.nodes;

import micronet.tools.model.variables.VariableDescription;

public class PrefabVariableEntryNode extends PrefabVariableNode {

	private String variableName = "EnTrY";
	
	public PrefabVariableEntryNode(String name, VariableDescription variableDescription) {
		super(name, variableDescription);
	}

	@Override
	public String getName() {
		return variableName;
	}
	
	public void setName(String name) {
		variableName = name;
	}
}
