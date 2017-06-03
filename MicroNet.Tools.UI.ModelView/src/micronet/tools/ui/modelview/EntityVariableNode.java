package micronet.tools.ui.modelview;

public class EntityVariableNode extends EntityNode {
	public BaseType type;
	public EntityVariableNode(String name) {
		super(name);
	}
	@Override
	public void accept(IVisitor visitor) {
	}
}
