package micronet.tools.ui.modelview.views;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import micronet.tools.ui.modelview.actions.TemplateCreateAction;
import micronet.tools.ui.modelview.nodes.EntityTemplateRootNode;

public class TemplateNodeRootDetails extends Composite implements IDetails {

	private TemplateCreateAction createTemplateAction;
	
	public TemplateNodeRootDetails(EntityTemplateRootNode templateRoot, Composite parent, int style) {
		super(parent, style);
		
		createTemplateAction = new TemplateCreateAction(getShell(), templateRoot); 
		createTemplateAction.setText("Create Template");
		createTemplateAction.setToolTipText("Create a new Template.");
		
		setLayout(new FillLayout(SWT.VERTICAL));
		
		Button button = new Button(this, SWT.PUSH);
		button.setText("Create Template");
		button.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				createTemplateAction.run();
			}
		});
	}

	@Override
	public void setRefreshViewerAction(Action refreshViewerAction) {
		createTemplateAction.setRefreshViewerAction(refreshViewerAction);
	}
}
