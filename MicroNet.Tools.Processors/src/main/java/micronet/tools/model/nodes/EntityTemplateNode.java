package micronet.tools.model.nodes;

import micronet.tools.model.IVisitor;

public class EntityTemplateNode extends ModelNode {

	public EntityTemplateNode(String name) {
		super(name);
	}
	
	@Override
	public void accept(IVisitor visitor) {
		visitor.visit(this);
	}
}