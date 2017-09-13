package micronet.tools.ui.modelview.actions;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import micronet.tools.core.Icons;
import micronet.tools.core.ModelProvider;
import micronet.tools.filesync.SyncScripts;
import micronet.tools.model.nodes.ModelNode;
import micronet.tools.model.nodes.ScriptNode;

public class ScriptRemoveAction extends ModelAction {

	private Shell shell;
	private ScriptNode scriptNode;
	
	public ScriptRemoveAction(Shell shell, ScriptNode scriptNode) {
		this.shell = shell;
		this.scriptNode = scriptNode;
		
		setText("Remove Script");
		setToolTipText("Removes the selected Script");
		setImageDescriptor(Icons.IMG_REMOVE);
	}

	@Override
	public void run() {

		String sharedDir = ModelProvider.INSTANCE.getSharedDir();
		
		//TODO: Script Usage Forbid Removal
		
		if (!MessageDialog.openQuestion(shell, "Remove Node", "Do you really want to remove: " + scriptNode.getName()))
			return;

		((ModelNode) scriptNode.getParent()).removeChild(scriptNode);
		SyncScripts.deleteScript(scriptNode.getName(), sharedDir);

		refreshViewer();
	}
}
