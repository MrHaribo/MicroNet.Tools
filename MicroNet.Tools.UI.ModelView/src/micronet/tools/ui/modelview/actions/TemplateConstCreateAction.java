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
import micronet.tools.model.nodes.EntityVariableConstNode;
import micronet.tools.model.nodes.PrefabVariableNode;

public class TemplateConstCreateAction extends ModelAction {

	private Shell shell;
	private EntityTemplateNode entityTemplateNode;

	public TemplateConstCreateAction(Shell shell, EntityTemplateNode entityTemplateNode) {
		this.shell = shell;
		this.entityTemplateNode = entityTemplateNode;
		
		setText("Add Constant");
		setToolTipText("Adds a Constant to the selected Template.");
		setImageDescriptor(Icons.IMG_CONST);
	}

	public void run() {

		InputDialog dlg = new InputDialog(shell, "Add Constant",
				"Add a new Constant to " + entityTemplateNode.getName(), "newConstant", null);
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

			EntityVariableConstNode variableNode = new EntityVariableConstNode(name);
			PrefabVariableNode prefabNode = new PrefabVariableNode(name, variableNode.getVariabelDescription());
			variableNode.addChild(prefabNode);
			
			entityTemplateNode.addChild(variableNode);

			String sharedDir = ModelProvider.INSTANCE.getSharedDir();
			SyncTemplateTree.saveTemplateTree(entityTemplateNode, sharedDir);

			refreshViewer();
		}
	}
}