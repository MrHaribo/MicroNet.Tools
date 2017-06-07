package micronet.tools.ui.modelview.nodes;

import micronet.tools.ui.modelview.IVisitor;
import micronet.tools.ui.modelview.variables.VariableType;

public class PrefabVariableNode extends ModelNode {

	private String contributingTemplate;
	private VariableType variableType;
	
	public PrefabVariableNode(String name, String contributingTemplate, VariableType variableType) {
		super(name);
		this.contributingTemplate = contributingTemplate;
		this.variableType = variableType;
	}

	@Override
	public void accept(IVisitor visitor) {
	}

	public VariableType getVariableType() {
		return variableType;
	}

	public String getContributingTemplate() {
		return contributingTemplate;
	}
}
