package micronet.tools.ui.modelview.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import micronet.tools.ui.modelview.INode;

public class NodeDetails extends Composite {
	
	private Action onRemove;
	
	public NodeDetails(INode node, Composite parent, int style) {
		super(parent, style);
		
		setLayout(new GridLayout(1, false));
		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Composite detailsContainer = new Composite(this, SWT.NONE);
		detailsContainer.setLayout(new GridLayout(2, false));
		detailsContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
				
		Label nameLabel = new Label(detailsContainer, SWT.NONE);
		nameLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		nameLabel.setText(node.getName());
		
		FontDescriptor descriptor = FontDescriptor.createFrom(nameLabel.getFont());
		descriptor = descriptor.setStyle(SWT.BOLD);
		nameLabel.setFont(descriptor.createFont(nameLabel.getDisplay()));
		
		Button removeNodeButton = new Button(detailsContainer, SWT.PUSH);
		removeNodeButton.setText("Remove");
		removeNodeButton.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
		
		removeNodeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onRemove.run();
			}
		});
	}

	public void setOnRemove(Action onRemove) {
		this.onRemove = onRemove;
	}
}
