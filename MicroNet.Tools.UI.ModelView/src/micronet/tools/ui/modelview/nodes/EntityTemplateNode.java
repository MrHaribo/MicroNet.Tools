package micronet.tools.ui.modelview.nodes;

import java.util.ArrayList;

import micronet.tools.ui.modelview.INode;
import micronet.tools.ui.modelview.IVisitor;

public class EntityTemplateNode extends EntityNode {
	private ArrayList<INode> children;

	public EntityTemplateNode(String name) {
		super(name);
		children = new ArrayList<INode>();
	}

	public void addChild(INode child) {
		children.add(child);
		child.setParent(this);
	}

	public void removeChild(INode child) {
		children.remove(child);
		child.setParent(null);
	}

	public INode[] getChildren() {
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