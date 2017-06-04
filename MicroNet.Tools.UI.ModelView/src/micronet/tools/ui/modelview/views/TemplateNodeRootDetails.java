package micronet.tools.ui.modelview.views;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class TemplateNodeRootDetails extends Composite {

	private Action onAddChildTemplate;
	
	public TemplateNodeRootDetails(Composite parent, int style) {
		super(parent, style);
		
		setLayout(new FillLayout(SWT.VERTICAL));
		
		Button button = new Button(this, SWT.PUSH);
		button.setText("Create Template");
		button.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				onAddChildTemplate.run();
			}
		});
	}

	public void setOnAddChildTemplate(Action onAddChildTemplate) {
		this.onAddChildTemplate = onAddChildTemplate;
	}
}
