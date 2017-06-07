package micronet.tools.ui.modelview;

import micronet.tools.ui.modelview.nodes.EntityTemplateNode;
import micronet.tools.ui.modelview.nodes.EntityTemplateRootNode;
import micronet.tools.ui.modelview.nodes.EnumNode;
import micronet.tools.ui.modelview.nodes.EnumRootNode;
import micronet.tools.ui.modelview.nodes.PrefabNode;
import micronet.tools.ui.modelview.nodes.PrefabRootNode;

public interface IVisitor {
	void visit(EntityTemplateNode node);

	void visit(EntityTemplateRootNode rootNode);
	
	void visit(EnumNode enumNode);

	void visit(EnumRootNode enumRootNode);
	
	void visit(PrefabNode prefabNode);

	void visit(PrefabRootNode prefabRootNode);
	
}
