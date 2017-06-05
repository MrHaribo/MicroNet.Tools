package micronet.tools.ui.modelview.nodes;

import micronet.tools.ui.modelview.IVisitor;
import micronet.tools.ui.modelview.variables.VariableDescription;
import micronet.tools.ui.modelview.variables.VariableType;

public class EntityVariableNode extends EntityNode {

	private VariableDescription variabelDescription = new VariableDescription(VariableType.STRING);
	
	public EntityVariableNode(String name) {
		super(name);
	}
	@Override
	public void accept(IVisitor visitor) {
	}
	
	public VariableDescription getVariabelDescription() {
		return variabelDescription;
	}
	public void setVariabelDescription(VariableDescription variabelDescription) {
		this.variabelDescription = variabelDescription;
	}
}
