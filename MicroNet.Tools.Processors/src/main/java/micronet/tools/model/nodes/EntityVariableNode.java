package micronet.tools.model.nodes;

import micronet.tools.model.IVisitor;
import micronet.tools.model.variables.VariableDescription;
import micronet.tools.model.variables.VariableType;

public class EntityVariableNode extends ModelNode {

	private VariableDescription variabelDescription = new VariableDescription(VariableType.STRING);
	
	private boolean ctorArg = false;
	
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
	public boolean isCtorArg() {
		return ctorArg;
	}
	public void setCtorArg(boolean ctorArg) {
		this.ctorArg = ctorArg;
	}
}
