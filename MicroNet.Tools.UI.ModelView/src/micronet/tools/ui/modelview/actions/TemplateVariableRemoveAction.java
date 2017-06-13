package micronet.tools.ui.modelview.actions;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import micronet.tools.core.Icons;
import micronet.tools.core.ModelProvider;
import micronet.tools.filesync.SyncTemplateTree;
import micronet.tools.model.nodes.EntityTemplateNode;
import micronet.tools.model.nodes.EntityVariableNode;

public class TemplateVariableRemoveAction extends ModelAction {
	
	private Shell shell;
	private EntityVariableNode variableNode;
	
	public TemplateVariableRemoveAction(Shell shell, EntityVariableNode variableNode) {
		this.shell = shell;
		this.variableNode = variableNode;
		
		setText("Remove Template");
		setToolTipText("Remove the Template.");
		setImageDescriptor(Icons.IMG_REMOVE);
	}

	@Override
	public void run() {
		if (!MessageDialog.openQuestion(shell, "Remove Node", "Do you really want to remove: " + variableNode.getName()))
			return;

		EntityTemplateNode parentTemplate = (EntityTemplateNode)variableNode.getParent();
		
		parentTemplate.removeChild(variableNode);
		
		String sharedDir = ModelProvider.INSTANCE.getSharedDir();
		SyncTemplateTree.saveTemplateTree(parentTemplate, sharedDir);
		
		refreshViewer();
	}
}
