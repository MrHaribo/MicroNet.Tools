package micronet.tools.ui.modelview;

public interface INode {
	String getName();
	void accept(IVisitor visitor);
	INode getParent();
}
