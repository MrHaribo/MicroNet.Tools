package micronet.tools.ui.modelview.views;

import java.util.function.Consumer;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import micronet.tools.model.nodes.ScriptNode;
import micronet.tools.ui.modelview.actions.ScriptRemoveAction;

public class ScriptNodeDetails extends NodeDetails {

	private ScriptRemoveAction removeAction;
	
	public ScriptNodeDetails(ScriptNode scriptNode, Composite parent, int style, Consumer<ScriptNode> showScript) {
		super(scriptNode, parent, style, true);
		
		removeAction = new ScriptRemoveAction(getShell(), scriptNode);
		
		Button editButton = new Button(this, SWT.PUSH);
		editButton.setText("Edit Script");
		
		editButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				showScript.accept(scriptNode);
			}
		});
	}
	
	@Override
	protected void removeNode() {
		removeAction.run();
	}
	
	@Override
	public void setRefreshViewerAction(Action refreshViewerAction) {
		removeAction.setRefreshViewerAction(refreshViewerAction, true);
	}
}
