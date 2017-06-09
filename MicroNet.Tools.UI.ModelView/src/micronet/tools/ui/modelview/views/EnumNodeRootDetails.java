package micronet.tools.ui.modelview.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import micronet.tools.core.ModelProvider;
import micronet.tools.ui.modelview.ModelConstants;
import micronet.tools.ui.modelview.SyncEnumTree;
import micronet.tools.ui.modelview.nodes.EnumNode;
import micronet.tools.ui.modelview.nodes.EnumRootNode;

public class EnumNodeRootDetails extends Composite {

	protected Action refreshViewerAction;
	
	private Action addEnumAction;
	
	private EnumRootNode enumRootNode;
	
	public EnumNodeRootDetails(EnumRootNode enumRootNode, Composite parent, int style) {
		super(parent, style);
		
		this.enumRootNode = enumRootNode;
		
		addEnumAction = new AddEnumAction();
		addEnumAction.setText("Add Enum");
		addEnumAction.setToolTipText("Adds a new Enum.");
		addEnumAction.setImageDescriptor(ModelView.IMG_ADD);

		setLayout(new FillLayout(SWT.VERTICAL));
		
		Button button = new Button(this, SWT.PUSH);
		button.setText("Create Enum");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				addEnumAction.run();
			}
		});
	}
	
	public void setRefreshViewerAction(Action refreshViewerAction) {
		this.refreshViewerAction = refreshViewerAction;
	}

	private class AddEnumAction extends Action {
		
		@Override
		public void run() {
			
			InputDialog dlg = new InputDialog(EnumNodeRootDetails.this.getShell(), "Add new Enum", "Enter Name for the new Enum", "NewEnum", null);
			if (dlg.open() == Window.OK) {
				String name = dlg.getValue();
				if (name == null)
					return;
				
				if (!ModelConstants.isValidJavaIdentifier(name)) {
					MessageDialog.openInformation(EnumNodeRootDetails.this.getShell(), "Invalid Enum Name", "\"" + name + "\" is an invalid name.");
					return;
				}
				String sharedDir = ModelProvider.INSTANCE.getSharedDir();
				if (SyncEnumTree.enumExists(name, sharedDir)) {
					MessageDialog.openInformation(EnumNodeRootDetails.this.getShell(), "Duplicate Enum", "Enum with the same name (" + name + ") already exists. Choose a unique name.");
					return;
				}

				enumRootNode.addChild(new EnumNode(name));
				SyncEnumTree.saveEnumTree(enumRootNode, sharedDir);
				
				if (refreshViewerAction != null)
					refreshViewerAction.run();
			}
		}
	}
}
