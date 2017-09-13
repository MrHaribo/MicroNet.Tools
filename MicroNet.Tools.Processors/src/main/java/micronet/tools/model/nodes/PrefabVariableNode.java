package micronet.tools.model.nodes;

import micronet.tools.model.IVisitor;
import micronet.tools.model.variables.VariableDescription;
import micronet.tools.model.variables.VariableType;

public class PrefabVariableNode extends ModelNode {

	private String contributingTemplate = "";
	private VariableDescription variableDescription;
	
	private boolean editable = true;
	
	private Object variableValue = null;
	
	public PrefabVariableNode(String name, VariableDescription variableDescription) {
		super(name);
		this.variableDescription = variableDescription;
	}
	
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

	public void setVariableDescription(VariableDescription variableDescription) {
		this.variableDescription = variableDescription;
	}

	public VariableDescription getVariableDescription() {
		return variableDescription;
	}

	public String getContributingTemplate() {
		return contributingTemplate;
	}

	public void setVariableValue(Object variableValue) {
		this.variableValue = variableValue;
	}

	public Object getVariableValue() {
		return variableValue;
	}
	
	public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}
}
