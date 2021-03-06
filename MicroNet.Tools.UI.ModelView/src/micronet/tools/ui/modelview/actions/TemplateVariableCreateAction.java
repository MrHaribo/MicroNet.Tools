package micronet.tools.ui.modelview.actions;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

import micronet.tools.core.Icons;
import micronet.tools.core.ModelProvider;
import micronet.tools.filesync.SyncTemplateTree;
import micronet.tools.model.INode;
import micronet.tools.model.ModelConstants;
import micronet.tools.model.nodes.EntityTemplateNode;
import micronet.tools.model.nodes.EntityVariableDynamicNode;
import micronet.tools.model.nodes.EntityVariableNode;
import micronet.tools.model.variables.VariableDescription;
import micronet.tools.model.variables.VariableType;

public class TemplateVariableCreateAction extends ModelAction {

	private Shell shell;
	private EntityTemplateNode entityTemplateNode;

	public TemplateVariableCreateAction(Shell shell, EntityTemplateNode entityTemplateNode) {
		this.shell = shell;
		this.entityTemplateNode = entityTemplateNode;
		
		setText("Add Variable");
		setToolTipText("Adds a Variable to the selected Template.");
		setImageDescriptor(Icons.IMG_VARIABLE);
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

			EntityVariableNode variableNode = new EntityVariableDynamicNode(name);
			variableNode.setVariabelDescription(new VariableDescription(VariableType.STRING));
			entityTemplateNode.addChild(variableNode);

			String sharedDir = ModelProvider.INSTANCE.getSharedDir();
			SyncTemplateTree.saveTemplateTree(entityTemplateNode, sharedDir);

			refreshViewer();
		}
	}
}