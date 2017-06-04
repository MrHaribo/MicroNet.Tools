package micronet.tools.ui.modelview;

public class EntityVariableNode extends EntityNode {
	public VariableType type;
	public EntityVariableNode(String name) {
		super(name);
	}
	@Override
	public void accept(IVisitor visitor) {
	}
}
