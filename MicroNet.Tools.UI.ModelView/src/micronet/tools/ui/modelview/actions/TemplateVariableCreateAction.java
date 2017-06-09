package micronet.tools.ui.modelview.actions;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

import micronet.tools.core.ModelProvider;
import micronet.tools.ui.modelview.INode;
import micronet.tools.ui.modelview.ModelConstants;
import micronet.tools.ui.modelview.SyncTemplateTree;
import micronet.tools.ui.modelview.nodes.EntityTemplateNode;
import micronet.tools.ui.modelview.nodes.EntityVariableNode;
import micronet.tools.ui.modelview.variables.VariableDescription;
import micronet.tools.ui.modelview.variables.VariableType;

public class TemplateVariableCreateAction extends ModelAction {

	private Shell shell;
	private EntityTemplateNode entityTemplateNode;

	public TemplateVariableCreateAction(Shell shell, EntityTemplateNode entityTemplateNode) {
		this.shell = shell;
		this.entityTemplateNode = entityTemplateNode;
	}

	public void run() {

		InputDialog dlg = new InputDialog(shell, "Add Variable",
				"Add a new Variable to " + entityTemplateNode.getName(), "newVariable", null);
		if (dlg.open() == Window.OK) {
			String name = dlg.getValue();
			if (name == null)
				return;

			if (!ModelConstants.isValidJavaIdentifier(name)) {
				MessageDialog.openInformation(shell, "Invalid Name", "\"" + name + "\" is an invalid name.");
				return;
			}

			for (INode child : entityTemplateNode.getChildren()) {
				if (child.getName().equals(name)) {
					MessageDialog.openInformation(shell, "Dublicate Variable",
							"Variable with the same (" + name + ") name already exists. Choose a unique name.");
					return;
				}
			}

			EntityVariableNode variableNode = new EntityVariableNode(name);
			variableNode.setVariabelDescription(new VariableDescription(VariableType.STRING));
			entityTemplateNode.addChild(variableNode);

			String sharedDir = ModelProvider.INSTANCE.getSharedDir();
			SyncTemplateTree.saveTemplateTree(entityTemplateNode, sharedDir);

			refreshViewer();
		}
	}
}