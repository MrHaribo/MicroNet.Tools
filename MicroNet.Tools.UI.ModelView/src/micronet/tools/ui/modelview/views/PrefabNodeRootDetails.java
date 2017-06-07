package micronet.tools.ui.modelview.views;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class PrefabNodeRootDetails extends Composite {

	private Action onAddPrefab;
	private Action onSavePrefabTreeAction;

	public PrefabNodeRootDetails(Composite parent, int style) {
		super(parent, style);

		setLayout(new FillLayout(SWT.VERTICAL));

		Button button = new Button(this, SWT.PUSH);
		button.setText("Create Prefab");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				onAddPrefab.run();
			}
		});
		
		button = new Button(this, SWT.PUSH);
		button.setText("Save Prefab Tree");
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
