package micronet.tools.model;

public interface INode {
	String getName();
	void accept(IVisitor visitor);
	INode getParent();
	void setParent(INode parent);
}
