package micronet.tools.ui.modelview.views;

import org.eclipse.core.runtime.IAdaptable;

class EntityNode implements IAdaptable {
	private String name;
	private EntityNode parent;
	
	public EntityNode(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void setParent(EntityTemplateNode parent) {
		this.parent = parent;
	}
	public EntityNode getParent() {
		return parent;
	}
	public String toString() {
		return getName();
	}
	public <T> T getAdapter(Class<T> key) {
		return null;
	}
}