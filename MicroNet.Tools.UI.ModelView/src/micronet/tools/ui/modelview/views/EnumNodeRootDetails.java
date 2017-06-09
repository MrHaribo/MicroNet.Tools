package micronet.tools.ui.modelview.views;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import micronet.tools.ui.modelview.actions.AddEnumAction;
import micronet.tools.ui.modelview.nodes.EnumRootNode;

public class EnumNodeRootDetails extends Composite {

	private AddEnumAction addEnumAction;
	
	public EnumNodeRootDetails(EnumRootNode enumRootNode, Composite parent, int style) {
		super(parent, style);
		
		addEnumAction = new AddEnumAction(getShell(), enumRootNode);
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
		addEnumAction.setRefreshViewerAction(refreshViewerAction);
	}


}
