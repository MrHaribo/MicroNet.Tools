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

import micronet.tools.ui.modelview.nodes.PrefabNode;

public class PrefabNodeDetails extends NodeRemovableDetails {
	private Action onAddPrefab;
	private Action onSavePrefabTreeAction;
	
	public PrefabNodeDetails(PrefabNode prefabNode, Composite parent, int style) {
		super(prefabNode, parent, style);
		
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
				onAddPrefab.run();
			}
		});
		
		button = new Button(this, SWT.PUSH);
		button.setText("Save Prefab");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				onSavePrefabTreeAction.run();
			}
		});
	}
	
	public void setOnAddPrefab(Action onAddPrefab) {
		this.onAddPrefab = onAddPrefab;
	}
	
	public void setOnSavePrefab(Action savePrefabTreeAction) {
		this.onSavePrefabTreeAction = savePrefabTreeAction;
	}
}
