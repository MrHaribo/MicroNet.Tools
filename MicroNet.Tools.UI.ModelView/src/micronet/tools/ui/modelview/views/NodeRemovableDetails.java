package micronet.tools.ui.modelview.views;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import micronet.tools.ui.modelview.INode;

public class NodeRemovableDetails extends NodeDetails {

	private Action removeNodeAction;
	
	public NodeRemovableDetails(INode node, Composite parent, int style) {
		super(node, parent, style);
	}

	@Override
	protected Composite createDetailsContainer(String name, int style) {
		Composite detailsContainer = super.createDetailsContainer(name, style);
		
		Button removeNodeButton = new Button(detailsContainer, SWT.PUSH);
		removeNodeButton.setText("Remove");
		removeNodeButton.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
		
		removeNodeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (removeNodeAction != null)
					removeNodeAction.run();
			}
		});
		return detailsContainer;
	}

	public void setRemoveNodeAction(Action removeNodeAction) {
		this.removeNodeAction = removeNodeAction;
	}
}
