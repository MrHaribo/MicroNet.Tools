package micronet.tools.ui.modelview.actions;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

import micronet.tools.core.ModelProvider;
import micronet.tools.ui.modelview.ModelConstants;
import micronet.tools.ui.modelview.SyncEnumTree;
import micronet.tools.ui.modelview.nodes.EnumNode;
import micronet.tools.ui.modelview.nodes.EnumRootNode;

public class AddEnumAction extends ModelAction {
	
	private Shell shell;
	private EnumRootNode enumRootNode;
	
	public AddEnumAction(Shell shell, EnumRootNode enumRootNode) {
		this.shell = shell;
		this.enumRootNode = enumRootNode;
	}

	@Override
	public void run() {
		
		InputDialog dlg = new InputDialog(shell, "Add new Enum", "Enter Name for the new Enum", "NewEnum", null);
		if (dlg.open() == Window.OK) {
			String name = dlg.getValue();
			if (name == null)
				return;
			
			if (!ModelConstants.isValidJavaIdentifier(name)) {
				MessageDialog.openInformation(shell, "Invalid Enum Name", "\"" + name + "\" is an invalid name.");
				return;
			}
			String sharedDir = ModelProvider.INSTANCE.getSharedDir();
			if (SyncEnumTree.enumExists(name, sharedDir)) {
				MessageDialog.openInformation(shell, "Duplicate Enum", "Enum with the same name (" + name + ") already exists. Choose a unique name.");
				return;
			}

			enumRootNode.addChild(new EnumNode(name));
			SyncEnumTree.saveEnumTree(enumRootNode, sharedDir);
			
			refreshViewer();
		}
	}
}
