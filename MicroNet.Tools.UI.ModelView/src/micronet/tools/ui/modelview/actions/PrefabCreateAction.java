package micronet.tools.ui.modelview.actions;

import java.util.List;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import micronet.tools.core.ModelProvider;
import micronet.tools.filesync.SyncPrefabTree;
import micronet.tools.filesync.SyncTemplateTree;
import micronet.tools.model.INode;
import micronet.tools.model.ModelConstants;
import micronet.tools.model.nodes.ModelNode;
import micronet.tools.model.nodes.PrefabNode;

public class PrefabCreateAction extends ModelAction {
	
	private Shell shell;
	private ModelNode modelNode;
	
	public PrefabCreateAction(Shell shell, ModelNode modelNode) {
		this.shell = shell;
		this.modelNode = modelNode;
	}

	@Override
	public void run() {

		InputDialog dlg = new InputDialog(shell, "Add new Prefab Node", "Enter Name for the new Prefab.", "NewPrefab", null);
		if (dlg.open() == Window.OK) {
			String name = dlg.getValue();
			if (name == null)
				return;
			
			if (!ModelConstants.isValidJavaIdentifier(name)) {
				MessageDialog.openInformation(shell, "Invalid Name", "\"" + name + "\" is an invalid name.");
				return;
			}
			
			for (INode childNode : modelNode.getChildren()) {
				if (childNode.getName().toLowerCase().equals(name.toLowerCase())) {
					MessageDialog.openInformation(shell, "Duplicate Prefab", 
							"Prefab with the same name (" + name + ") already exists under " + modelNode.getName() + ". Choose a unique name.");
					return;
				}
			}
			
			String sharedDir = ModelProvider.INSTANCE.getSharedDir();
			List<String> allTemplateNames = SyncTemplateTree.getAllTemplateNames(sharedDir);
			
			ElementListSelectionDialog dialog = new ElementListSelectionDialog(shell, new LabelProvider());
			dialog.setElements(allTemplateNames.toArray());
			dialog.setTitle("Select A type for the Prefab");
			if (dialog.open() != Window.OK)
				return;
			
			Object[] selectedType = dialog.getResult();
			if (selectedType.length == 0 || selectedType.length > 1)
				return;
			
			PrefabNode prefabNode = new PrefabNode(name, selectedType[0].toString(), sharedDir);
			modelNode.addChild(prefabNode);
			SyncPrefabTree.savePrefab(modelNode, sharedDir);
			
			refreshViewer();
		}
	}
}
