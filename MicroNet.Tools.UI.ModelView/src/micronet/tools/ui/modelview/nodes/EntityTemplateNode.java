package micronet.tools.ui.modelview.nodes;

import micronet.tools.ui.modelview.IVisitor;

public class EntityTemplateNode extends ModelNode {

	public EntityTemplateNode(String name) {
		super(name);
	}
	
	@Override
	public void accept(IVisitor visitor) {
		visitor.visit(this);
	}
}