package micronet.tools.ui.modelview.nodes;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;

import micronet.tools.ui.modelview.INode;

public abstract class ModelNode implements INode, IAdaptable  {
	private String name;
	private INode parent;
	private List<INode> children;
	
	public ModelNode(String name) {
		this.name = name;
		this.children = new ArrayList<>();
	}
	public String getName() {
		return name;
	}
	public void setParent(INode parent) {
		this.parent = parent;
	}
	public INode getParent() {
		return parent;
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
		return children.toArray(new ModelNode[children.size()]);
	}
	
	public void clearChildren() {
		children.clear();
	}

	public boolean hasChildren() {
		return children.size() > 0;
	}
	public String toString() {
		return getName();
	}
	public <T> T getAdapter(Class<T> key) {
		return null;
	}
}