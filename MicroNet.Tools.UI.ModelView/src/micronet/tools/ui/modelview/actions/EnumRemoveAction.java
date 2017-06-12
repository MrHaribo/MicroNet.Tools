package micronet.tools.ui.modelview.actions;

import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import micronet.tools.core.ModelProvider;
import micronet.tools.filesync.SyncEnumTree;
import micronet.tools.filesync.SyncTemplateTree;
import micronet.tools.model.nodes.EnumNode;
import micronet.tools.model.nodes.ModelNode;

public class EnumRemoveAction extends ModelAction {

	private Shell shell;
	private EnumNode enumNode;
	
	public EnumRemoveAction(Shell shell, EnumNode enumNode) {
		this.shell = shell;
		this.enumNode = enumNode;
	}

	@Override
	public void run() {

		String sharedDir = ModelProvider.INSTANCE.getSharedDir();
		Map<String, Set<String>> enumUsage = SyncTemplateTree.getEnumUsage(sharedDir);
		if (enumUsage.containsKey(enumNode.getName())) {
			MessageDialog.openInformation(shell.getShell(), "Enum in Use",
					"Enum " + enumNode.getName() + " cant be removed because it is in use by: "
							+ String.join(",", enumUsage.get(enumNode.getName())));
			return;
		}

		if (!MessageDialog.openQuestion(shell, "Remove Node", "Do you really want to remove: " + enumNode.getName()))
			return;

		((ModelNode) enumNode.getParent()).removeChild(enumNode);
		SyncEnumTree.removeEnum(enumNode, sharedDir);

		refreshViewer();
	}
}
