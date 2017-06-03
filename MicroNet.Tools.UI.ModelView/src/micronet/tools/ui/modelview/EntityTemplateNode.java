package micronet.tools.ui.modelview;

import java.util.ArrayList;

public class EntityTemplateNode extends EntityNode {
	private ArrayList<EntityNode> children;

	public EntityTemplateNode(String name) {
		super(name);
		children = new ArrayList<EntityNode>();
	}

	public void addChild(EntityNode child) {
		children.add(child);
		child.setParent(this);
	}

	public void removeChild(EntityNode child) {
		children.remove(child);
		child.setParent(null);
	}

	public EntityNode[] getChildren() {
		return children.toArray(new EntityNode[children.size()]);
	}

	public boolean hasChildren() {
		return children.size() > 0;
	}
	
	@Override
	public void accept(IVisitor visitor) {
		visitor.visit(this);
	}
}