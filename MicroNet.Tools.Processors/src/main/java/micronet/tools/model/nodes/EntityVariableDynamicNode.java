package micronet.tools.model.nodes;

import micronet.tools.model.IVisitor;

public class EntityVariableDynamicNode extends EntityVariableNode {

	private boolean ctorArg = false;
	
	public EntityVariableDynamicNode(String name) {
		super(name);
	}
	@Override
	public void accept(IVisitor visitor) {
	}

	public boolean isCtorArg() {
		return ctorArg;
	}
	public void setCtorArg(boolean ctorArg) {
		this.ctorArg = ctorArg;
	}
}
