package micronet.tools.model.nodes;

import micronet.tools.model.IVisitor;

public class EntityVariableConstNode extends EntityVariableNode {

	public EntityVariableConstNode(String name) {
		super(name);
	}
	@Override
	public void accept(IVisitor visitor) {
	}
}
