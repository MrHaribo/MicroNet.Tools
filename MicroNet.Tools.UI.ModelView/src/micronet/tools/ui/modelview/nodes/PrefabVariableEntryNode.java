package micronet.tools.ui.modelview.nodes;

import micronet.tools.ui.modelview.variables.VariableDescription;

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
