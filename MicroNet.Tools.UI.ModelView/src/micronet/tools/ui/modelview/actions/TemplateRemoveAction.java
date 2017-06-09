package micronet.tools.ui.modelview.actions;

import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import micronet.tools.core.ModelProvider;
import micronet.tools.ui.modelview.INode;
import micronet.tools.ui.modelview.SyncTemplateTree;
import micronet.tools.ui.modelview.nodes.EntityTemplateNode;

public class TemplateRemoveAction extends ModelAction {
	
	private Shell shell;
	private EntityTemplateNode entityTemplateNode;
	
	public TemplateRemoveAction(Shell shell, EntityTemplateNode entityTemplateNode) {
		this.shell = shell;
		this.entityTemplateNode = entityTemplateNode;
	}

	@Override
	public void run() {
			
		for (INode node : entityTemplateNode.getChildren()) {
			if (node instanceof EntityTemplateNode) {
				MessageDialog.openInformation(shell, "Template has Children", "Remove Child Templates first");
				return;
			}
		}
		
		String sharedDir = ModelProvider.INSTANCE.getSharedDir();
		Map<String, Set<String>> templateUsage = SyncTemplateTree.getTemplateUsage(sharedDir);
		if (templateUsage.containsKey(entityTemplateNode.getName())) {
			MessageDialog.openInformation(shell, "Template is in use", "Template " + entityTemplateNode.getName() + 
					" cant be removed because it is in use by: " +
					String.join(",", templateUsage.get(entityTemplateNode.getName())));
			return;
		}
		
		if (!MessageDialog.openQuestion(shell, "Remove Node", "Do you really want to remove: " + entityTemplateNode.getName()))
			return;

		SyncTemplateTree.removeTemplate(entityTemplateNode, sharedDir);
		
		((EntityTemplateNode) entityTemplateNode.getParent()).removeChild(entityTemplateNode);
		
		refreshViewer();
	}
}
