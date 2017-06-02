package micronet.tools.ui.modelview.views;

import java.util.ArrayList;

class EntityTemplateNode extends EntityNode {
	private ArrayList<EntityTemplateNode> children;

	public EntityTemplateNode(String name) {
		super(name);
		children = new ArrayList<EntityTemplateNode>();
	}

	public void addChild(EntityTemplateNode child) {
		children.add(child);
		child.setParent(this);
	}

	public void removeChild(EntityTemplateNode child) {
		children.remove(child);
		child.setParent(null);
	}

	public EntityTemplateNode[] getChildren() {
		return children.toArray(new EntityTemplateNode[children.size()]);
	}

	public boolean hasChildren() {
		return children.size() > 0;
	}
}