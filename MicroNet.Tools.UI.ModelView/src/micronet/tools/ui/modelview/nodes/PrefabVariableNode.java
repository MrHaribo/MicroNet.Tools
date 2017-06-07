package micronet.tools.ui.modelview.nodes;

import micronet.tools.ui.modelview.IVisitor;
import micronet.tools.ui.modelview.variables.VariableDescription;
import micronet.tools.ui.modelview.variables.VariableType;

public class PrefabVariableNode extends ModelNode {

	private String contributingTemplate;
	private VariableDescription variableDescription;
	
	public PrefabVariableNode(EntityVariableNode variableNodeMirror, String contributingTemplate) {
		super(variableNodeMirror.getName());
		this.contributingTemplate = contributingTemplate;
		this.variableDescription = variableNodeMirror.getVariabelDescription();
	}

	@Override
	public void accept(IVisitor visitor) {
	}

	public VariableType getVariableType() {
		return variableDescription.getType();
	}

	public VariableDescription getVariableDescription() {
		return variableDescription;
	}

	public String getContributingTemplate() {
		return contributingTemplate;
	}
}
