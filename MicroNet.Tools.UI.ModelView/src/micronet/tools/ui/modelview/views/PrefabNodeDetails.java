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
import micronet.tools.filesync.SyncPrefabTree;
import micronet.tools.model.nodes.PrefabNode;
import micronet.tools.ui.modelview.actions.PrefabCreateAction;
import micronet.tools.ui.modelview.actions.PrefabRemoveAction;

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

		Composite detailsContainer = new Composite(this, SWT.NONE);
		detailsContainer.setLayout(new GridLayout(2, false));
		detailsContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Label label = new Label(detailsContainer, SWT.NONE);
		label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		label.setText("Template Type:");
		
		label = new Label(detailsContainer, SWT.NONE);
		label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		label.setText(prefabNode.getTemplateType());
		
		Button button = new Button(detailsContainer, SWT.PUSH);
		button.setText("Create Child Prefab");
		button.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 2, 1));
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				createChildPrefabAction.run();
			}
		});
		
		button = new Button(detailsContainer, SWT.PUSH);
		button.setText("Save Prefab");
		button.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 2, 1));
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
