package micronet.tools.model.nodes;

import micronet.tools.model.IVisitor;

public class EntityTemplateNode extends ModelNode {

	private boolean hasDefaultCtor = false;
	
	public EntityTemplateNode(String name) {
		super(name);
	}
	
	@Override
	public void accept(IVisitor visitor) {
		visitor.visit(this);
	}

	public boolean hasDefaultCtor() {
		return hasDefaultCtor;
	}

	public void setHasDefaultCtor(boolean defaultCtor) {
		this.hasDefaultCtor = defaultCtor;
	}
}