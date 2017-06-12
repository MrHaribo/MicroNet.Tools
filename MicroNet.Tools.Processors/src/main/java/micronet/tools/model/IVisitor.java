package micronet.tools.model;

import micronet.tools.model.nodes.EntityTemplateNode;
import micronet.tools.model.nodes.EntityTemplateRootNode;
import micronet.tools.model.nodes.EnumNode;
import micronet.tools.model.nodes.EnumRootNode;
import micronet.tools.model.nodes.PrefabNode;
import micronet.tools.model.nodes.PrefabRootNode;

public interface IVisitor {
	void visit(EntityTemplateNode node);

	void visit(EntityTemplateRootNode rootNode);
	
	void visit(EnumNode enumNode);

	void visit(EnumRootNode enumRootNode);
	
	void visit(PrefabNode prefabNode);

	void visit(PrefabRootNode prefabRootNode);
	
}
