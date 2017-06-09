package micronet.tools.ui.modelview.nodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IElementComparer;

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
	
	public int getChildIndex(INode node) {
		return children.indexOf(node);
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
	
	@Override
    public boolean equals(Object o) {
		return o.hashCode() == this.hashCode();
    }

    @Override
    public int hashCode() {
        return getID().hashCode();
    }
    
	public String getID() {
		return serializeID(this);
	}
    
	public static String serializeID(ModelNode modelNode) {
		List<String> prefabNameArray = new ArrayList<>(); 
		prefabNameArray.add(modelNode.getName());
		
		INode parent = modelNode.getParent();
		while (parent != null && !(parent instanceof PrefabRootNode)) {
			prefabNameArray.add(parent.getName());
			parent = parent.getParent();
		}
		Collections.reverse(prefabNameArray);
		return String.join(".", prefabNameArray);
	}
}