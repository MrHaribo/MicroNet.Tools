package micronet.tools.ui.modelview.views;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import micronet.tools.core.ModelProvider;
import micronet.tools.ui.modelview.SyncPrefabTree;
import micronet.tools.ui.modelview.actions.PrefabCreateAction;
import micronet.tools.ui.modelview.nodes.PrefabRootNode;

public class PrefabNodeRootDetails extends Composite implements IDetails {

	private PrefabCreateAction addPrefabAction;
	
	public PrefabNodeRootDetails(PrefabRootNode prefabRoot, Composite parent, int style) {
		super(parent, style);

		addPrefabAction = new PrefabCreateAction(getShell(), prefabRoot);
		addPrefabAction.setText("Add Prefab");
		addPrefabAction.setToolTipText("Adds a new Prefab.");
		
		setLayout(new FillLayout(SWT.VERTICAL));

		Button button = new Button(this, SWT.PUSH);
		button.setText("Create Prefab");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				addPrefabAction.run();
			}
		});
		
		button = new Button(this, SWT.PUSH);
		button.setText("Save Prefab Tree");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				String sharedDir = ModelProvider.INSTANCE.getSharedDir();
				SyncPrefabTree.savePrefab(prefabRoot, sharedDir);
			}
		});
	}
	
	@Override
	public void setRefreshViewerAction(Action refreshViewerAction) {
		addPrefabAction.setRefreshViewerAction(refreshViewerAction, false);
	}
}
