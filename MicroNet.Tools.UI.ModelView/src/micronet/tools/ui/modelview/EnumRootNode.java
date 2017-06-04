package micronet.tools.ui.modelview;

import java.util.ArrayList;
import java.util.List;

public class EnumRootNode implements INode {
	
	private String name;
	
	private List<EnumNode> enumDefinitions = new ArrayList<>();
	
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
	public List<EnumNode> getEnumDefinitions() {
		return enumDefinitions;
	}
	public void setEnumDefinitions(List<EnumNode> enumDefinitions) {
		this.enumDefinitions = enumDefinitions;
	}

}
