package micronet.tools.model.nodes;

import java.util.ArrayList;
import java.util.List;

import micronet.tools.model.IVisitor;

public class EnumNode extends ModelNode {

	public EnumNode(String name) {
		super(name);
	}
	
	private List<String> enumConstants = new ArrayList<>();

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
