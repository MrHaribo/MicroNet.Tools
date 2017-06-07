package micronet.tools.ui.modelview.nodes;

import micronet.tools.core.ModelProvider;
import micronet.tools.ui.modelview.INode;
import micronet.tools.ui.modelview.IVisitor;
import micronet.tools.ui.modelview.SyncTemplateTree;

public class PrefabNode extends ModelNode {

	private String templateType;
	
	public PrefabNode(String name, String templateType) {
		super(name);
		this.templateType = templateType;
		
		String sharedDir = ModelProvider.INSTANCE.getSharedDir();
		EntityTemplateNode templateNodeMirror = SyncTemplateTree.loadTemplateType(templateType, sharedDir);
		createVariables(templateNodeMirror);
	}
	
	private void createVariables(EntityTemplateNode templateNodeMirror) {
		
		if (templateNodeMirror.getParent() != null) {
			createVariables((EntityTemplateNode)templateNodeMirror.getParent());
		}
		
		for (INode child : templateNodeMirror.getChildren()) {
			if (child instanceof EntityVariableNode) {
				EntityVariableNode variableNodeMirror = (EntityVariableNode) child;
				PrefabVariableNode prefabVariable = new PrefabVariableNode(variableNodeMirror, templateNodeMirror.getName());
				addChild(prefabVariable);
			}
		}
	}

	@Override
	public void accept(IVisitor visitor) {
		visitor.visit(this);
	}

	public String getTemplateType() {
		return templateType;
	}
}
