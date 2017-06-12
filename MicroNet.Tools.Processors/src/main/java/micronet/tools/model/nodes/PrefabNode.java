package micronet.tools.model.nodes;

import micronet.tools.filesync.SyncTemplateTree;
import micronet.tools.model.INode;
import micronet.tools.model.IVisitor;

public class PrefabNode extends ModelNode {

	private String templateType;
	
	public PrefabNode(String name, String templateType, String sharedDir) {
		super(name);
		this.templateType = templateType;
		
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
