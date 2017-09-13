package micronet.tools.ui.modelview.views;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import micronet.tools.model.nodes.ScriptRootNode;
import micronet.tools.ui.modelview.actions.ScriptCreateAction;

public class ScriptNodeRootDetails extends Composite implements IDetails {

	private ScriptCreateAction addScriptAction;
	
	public ScriptNodeRootDetails(ScriptRootNode scriptRootNode, Composite parent, int style) {
		super(parent, style);
		
		addScriptAction = new ScriptCreateAction(getShell(), scriptRootNode);
		
		setLayout(new FillLayout(SWT.VERTICAL));
		
		Button button = new Button(this, SWT.PUSH);
		button.setText("Create Script");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				addScriptAction.run();
			}
		});
	}
	
	@Override
	public void setRefreshViewerAction(Action refreshViewerAction) {
		addScriptAction.setRefreshViewerAction(refreshViewerAction, false);
	}
}
