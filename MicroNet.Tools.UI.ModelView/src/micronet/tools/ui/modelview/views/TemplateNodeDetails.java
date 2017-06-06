package micronet.tools.ui.modelview.views;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import micronet.tools.ui.modelview.nodes.EntityTemplateNode;

public class TemplateNodeDetails extends NodeDetails {

	private Action onAddChildTemplate;
	private Action onAddChildVariable;
	
	public TemplateNodeDetails(EntityTemplateNode templateNode, Composite parent, int style) {
		super(templateNode, parent, style);
		
		Composite detailsContainer = new Composite(this, SWT.NONE);
		detailsContainer.setLayout(new GridLayout(2, false));
		
		Button button = new Button(detailsContainer, SWT.PUSH);
		button.setText("Add Child Template");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				onAddChildTemplate.run();
			}
		});
		
		button = new Button(detailsContainer, SWT.PUSH);
		button.setText("Add Variable");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				onAddChildVariable.run();
			}
		});
	}

	public void setOnAddChildTemplate(Action onAddChildTemplate) {
		this.onAddChildTemplate = onAddChildTemplate;
	}

	public void setOnAddChildVariable(Action onAddChildVariable) {
		this.onAddChildVariable = onAddChildVariable;
	}


	
	
}
