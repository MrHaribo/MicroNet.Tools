package micronet.tools.ui.modelview;

import java.util.ArrayList;
import java.util.List;

public class EnumRootNode implements INode {
	
	private String name;
	
	private List<INode> enumDefinitions = new ArrayList<>();
	
	public EnumRootNode(String name) {
		this.name = name;
	}
	@Override
	public String getName() {
		return name;
	}
	@Override
	public void setParent(INode parent) {
	}
	@Override
	public INode getParent() {
		return null;
	}
	public String toString() {
		return getName();
	}

	@Override
	public void accept(IVisitor visitor) {
		visitor.visit(this);
	}
	
	public void addChild(INode child) {
		enumDefinitions.add(child);
		child.setParent(this);
	}

	public void removeChild(INode child) {
		enumDefinitions.remove(child);
		child.setParent(null);
	}

	public INode[] getChildren() {
		return enumDefinitions.toArray(new EnumNode[enumDefinitions.size()]);
	}
}
