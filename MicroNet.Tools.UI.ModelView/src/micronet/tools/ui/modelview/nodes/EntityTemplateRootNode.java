package micronet.tools.ui.modelview.nodes;

import micronet.tools.ui.modelview.IVisitor;

public class EntityTemplateRootNode extends EntityTemplateNode {
	public EntityTemplateRootNode(String name) {
		super(name);
	}
	
	@Override
	public void accept(IVisitor visitor) {
		// TODO Auto-generated method stub
		visitor.visit(this);
	}
}