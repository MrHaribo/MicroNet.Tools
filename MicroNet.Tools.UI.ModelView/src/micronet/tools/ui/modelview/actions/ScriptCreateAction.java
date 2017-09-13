package micronet.tools.ui.modelview.actions;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

import micronet.tools.core.Icons;
import micronet.tools.core.ModelProvider;
import micronet.tools.filesync.SyncScripts;
import micronet.tools.model.ModelConstants;
import micronet.tools.model.nodes.ScriptNode;
import micronet.tools.model.nodes.ScriptRootNode;

public class ScriptCreateAction extends ModelAction {
	
	private Shell shell;
	private ScriptRootNode scriptRootNode;
	
	public ScriptCreateAction(Shell shell, ScriptRootNode scriptRootNode) {
		this.shell = shell;
		this.scriptRootNode = scriptRootNode;
		
		setText("Add Script");
		setToolTipText("Creates a new Script.");
		setImageDescriptor(Icons.IMG_ENUM);
	}

	@Override
	public void run() {
		
		InputDialog dlg = new InputDialog(shell, "Add new Script", "Enter Name for the new Script", "NewScript", null);
		if (dlg.open() == Window.OK) {
			String name = dlg.getValue();
			if (name == null)
				return;
			
			if (!ModelConstants.isValidJavaIdentifier(name)) {
				MessageDialog.openInformation(shell, "Invalid Enum Name", "\"" + name + "\" is an invalid name.");
				return;
			}
			String sharedDir = ModelProvider.INSTANCE.getSharedDir();
			if (SyncScripts.scriptExists(name, sharedDir)) {
				MessageDialog.openInformation(shell, "Duplicate Script", "Script with the same name (" + name + ") already exists. Choose a unique name.");
				return;
			}
			
			SyncScripts.createScript(name, sharedDir);
			scriptRootNode.addChild(new ScriptNode(name));

			refreshViewer();
		}
	}
}
