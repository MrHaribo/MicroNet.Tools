package micronet.tools.ui.modelview;

import micronet.tools.ui.modelview.nodes.EntityTemplateNode;
import micronet.tools.ui.modelview.nodes.EnumNode;
import micronet.tools.ui.modelview.nodes.EnumRootNode;

public interface IVisitor {
	void visit(EntityTemplateNode node);

	void visit(EnumNode enumNode);

	void visit(EnumRootNode enumRootNode);
}
