package micronet.tools.ui.modelview.views;

import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import micronet.tools.ui.modelview.INode;

public class NodeDetails extends Composite {
	
	public NodeDetails(INode node, Composite parent, int style) {
		super(parent, style);
		
		setLayout(new GridLayout(1, false));
		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		createDetailsContainer(node.getName(), SWT.NONE);
	}
	
	protected Composite createDetailsContainer(String name, int style) {
		Composite detailsContainer = new Composite(this, SWT.NONE);
		detailsContainer.setLayout(new GridLayout(2, false));
		detailsContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
				
		Label nameLabel = new Label(detailsContainer, SWT.NONE);
		nameLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		nameLabel.setText(name);
		
		FontDescriptor descriptor = FontDescriptor.createFrom(nameLabel.getFont());
		descriptor = descriptor.setStyle(SWT.BOLD);
		nameLabel.setFont(descriptor.createFont(nameLabel.getDisplay()));
		return detailsContainer;
	}
}
