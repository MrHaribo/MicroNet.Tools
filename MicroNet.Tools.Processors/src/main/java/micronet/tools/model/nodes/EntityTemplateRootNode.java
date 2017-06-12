package micronet.tools.model.nodes;

import micronet.tools.model.IVisitor;

public class EntityTemplateRootNode extends EntityTemplateNode {
	public EntityTemplateRootNode(String name) {
		super(name);
	}
	
	@Override
	public void accept(IVisitor visitor) {
		visitor.visit(this);
	}
}