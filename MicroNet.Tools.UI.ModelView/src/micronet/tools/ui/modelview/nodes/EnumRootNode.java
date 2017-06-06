package micronet.tools.ui.modelview.nodes;

import java.util.ArrayList;
import java.util.List;

import micronet.tools.ui.modelview.INode;
import micronet.tools.ui.modelview.IVisitor;
import micronet.tools.ui.modelview.ModelConstants;

public class EnumRootNode implements INode {
	
	private List<INode> enumDefinitions = new ArrayList<>();
	@Override
	public String getName() {
		return ModelConstants.ENUM_DEFINITIONS_KEY;
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
