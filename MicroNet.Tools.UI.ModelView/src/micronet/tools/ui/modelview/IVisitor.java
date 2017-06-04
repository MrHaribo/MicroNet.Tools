package micronet.tools.ui.modelview;

public interface IVisitor {
	void visit(EntityTemplateNode node);

	void visit(EnumNode enumNode);

	void visit(EnumRootNode enumRootNode);
}
