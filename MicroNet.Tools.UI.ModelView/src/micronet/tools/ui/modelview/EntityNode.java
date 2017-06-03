package micronet.tools.ui.modelview;

import org.eclipse.core.runtime.IAdaptable;

public abstract class EntityNode implements INode, IAdaptable  {
	private String name;
	private INode parent;
	
	public EntityNode(String name) {
		this.name = name;
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
	public String toString() {
		return getName();
	}
	public <T> T getAdapter(Class<T> key) {
		return null;
	}
}