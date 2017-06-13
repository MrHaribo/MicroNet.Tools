package micronet.tools.ui.modelview.actions;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

import micronet.tools.core.Icons;
import micronet.tools.core.ModelProvider;
import micronet.tools.filesync.SyncTemplateTree;
import micronet.tools.model.ModelConstants;
import micronet.tools.model.nodes.EntityTemplateNode;
import micronet.tools.model.variables.VariableType;

public class TemplateCreateAction extends ModelAction {
	
	Shell shell;
	EntityTemplateNode entityTemplateNode;
	
	public TemplateCreateAction(Shell shell, EntityTemplateNode entityTemplateNode) {
		this.shell = shell;
		this.entityTemplateNode = entityTemplateNode;
		
		setText("Create Template");
		setToolTipText("Creates a new Template.");
		setImageDescriptor(Icons.IMG_TEMPLATE);
	}

	public void run() {
		
		InputDialog dlg = new InputDialog(shell, "Add EntityTemplate", "Enter Name for new EntityTemplate.", "NewType", null);
		if (dlg.open() == Window.OK) {
			String name = dlg.getValue();
			if (name == null)
				return;
			
			String sharedDir = ModelProvider.INSTANCE.getSharedDir();
			
			if (!ModelConstants.isValidJavaIdentifier(name)) {
				MessageDialog.openInformation(shell, "Invalid Template Name", "\"" + name + "\" is an invalid name.");
				return;
			}
			
			if (VariableType.isPrimitiveTypeName(name)) {
				MessageDialog.openInformation(shell, "Forbidden Template Name", "Primitive Typenames are reserved.");
				return;
			}
			
			if (SyncTemplateTree.templateExists(name, sharedDir)) {
				MessageDialog.openInformation(shell, "Duplicate Template Name","Template with the name \"" + name + "\" already exists. Choose a unique name.");
				return;
			}

			entityTemplateNode.addChild(new EntityTemplateNode(name));
			SyncTemplateTree.saveTemplateTree(entityTemplateNode, sharedDir);
			
			refreshViewer();
		}
	}
}
