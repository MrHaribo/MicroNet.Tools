package micronet.tools.ui.modelview.actions;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import micronet.tools.core.Icons;
import micronet.tools.core.ModelProvider;
import micronet.tools.filesync.SyncPrefabTree;
import micronet.tools.model.nodes.ModelNode;
import micronet.tools.model.nodes.PrefabNode;

public class PrefabRemoveAction extends ModelAction {

	private Shell shell;
	private PrefabNode prefabNode;

	public PrefabRemoveAction(Shell shell, PrefabNode prefabNode) {
		this.shell = shell;
		this.prefabNode = prefabNode;
		
		setText("Premove Prefab");
		setToolTipText("Removes the Prefab from the Model.");
		setImageDescriptor(Icons.IMG_REMOVE);
	}

	@Override
	public void run() {
		if (!MessageDialog.openQuestion(shell, "Remove Node", "Do you really want to remove: " + prefabNode.getName()))
			return;

		String sharedDir = ModelProvider.INSTANCE.getSharedDir();
		SyncPrefabTree.removePrefab((PrefabNode) prefabNode, sharedDir);
		((ModelNode) prefabNode.getParent()).removeChild(prefabNode);
		
		refreshViewer();
	}
}
