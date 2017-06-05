package micronet.tools.ui.modelview.nodes;

import java.util.ArrayList;
import java.util.List;

import micronet.tools.ui.modelview.INode;
import micronet.tools.ui.modelview.IVisitor;

public class EnumNode implements INode {

	private String name;
	private INode parent;
	
	private List<String> enumConstants = new ArrayList<>();
	
	public EnumNode(String name) {
		this.name = name;
	}
	@Override
	public String getName() {
		return name;
	}
	@Override
	public void setParent(INode parent) {
		this.parent = parent;
	}
	@Override
	public INode getParent() {
		return parent;
	}
	public String toString() {
		return getName();
	}

	@Override
	public void accept(IVisitor visitor) {
		visitor.visit(this);
	}
	public List<String> getEnumConstants() {
		return enumConstants;
	}
	public void setEnumConstants(List<String> enumConstants) {
		this.enumConstants = enumConstants;
	}
}
