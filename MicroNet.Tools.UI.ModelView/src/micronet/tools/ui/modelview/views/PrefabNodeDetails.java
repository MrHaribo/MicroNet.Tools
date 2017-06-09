package micronet.tools.ui.modelview.views;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import micronet.tools.core.ModelProvider;
import micronet.tools.ui.modelview.SyncPrefabTree;
import micronet.tools.ui.modelview.actions.PrefabCreateAction;
import micronet.tools.ui.modelview.actions.PrefabRemoveAction;
import micronet.tools.ui.modelview.nodes.PrefabNode;

public class PrefabNodeDetails extends NodeDetails {
	
	private PrefabRemoveAction removePrefabAction;
	private PrefabCreateAction createChildPrefabAction;
	
	public PrefabNodeDetails(PrefabNode prefabNode, Composite parent, int style) {
		super(prefabNode, parent, style, true);
		
		createChildPrefabAction = new PrefabCreateAction(getShell(), prefabNode);
		createChildPrefabAction.setText("Add Prefab");
		createChildPrefabAction.setToolTipText("Adds a new Child Prefab.");
		
		removePrefabAction = new PrefabRemoveAction(getShell(), prefabNode);
		removePrefabAction.setText("Add Prefab");
		removePrefabAction.setToolTipText("Adds a new Child Prefab.");
		
		setLayout(new GridLayout(1, false));
		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Label nameLabel = new Label(this, SWT.NONE);
		nameLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		nameLabel.setText(prefabNode.getTemplateType());
		
		Button button = new Button(this, SWT.PUSH);
		button.setText("Create Child Prefab");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				createChildPrefabAction.run();
			}
		});
		
		button = new Button(this, SWT.PUSH);
		button.setText("Save Prefab");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				String sharedDir = ModelProvider.INSTANCE.getSharedDir();
				SyncPrefabTree.savePrefab(prefabNode, sharedDir);
			}
		});
	}

	@Override
	public void setRefreshViewerAction(Action refreshViewerAction) {
		removePrefabAction.setRefreshViewerAction(refreshViewerAction, false);
		createChildPrefabAction.setRefreshViewerAction(refreshViewerAction, false);
	}
	
	@Override
	protected void removeNode() {
		removePrefabAction.run();
	}
}
