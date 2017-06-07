package micronet.tools.ui.modelview.views;

import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import micronet.tools.ui.modelview.nodes.PrefabVariableNode;

public class PrefabVariableNodeDetails extends Composite {

	public PrefabVariableNodeDetails(PrefabVariableNode variableNode, Composite parent, int style) {
		super(parent, style);
		
		setLayout(new GridLayout(2, false));
		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Label nameLabel = new Label(this, SWT.NONE);
		nameLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
		nameLabel.setText(variableNode.getName());
		
		FontDescriptor descriptor = FontDescriptor.createFrom(nameLabel.getFont());
		descriptor = descriptor.setStyle(SWT.BOLD);
		nameLabel.setFont(descriptor.createFont(nameLabel.getDisplay()));
		
		Label label = new Label(this, SWT.NONE);
		label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		label.setText("Variable Type: ");
		
		label = new Label(this, SWT.NONE);
		label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		label.setText(variableNode.getVariableType().toString());
		
		label = new Label(this, SWT.NONE);
		label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		label.setText("Contributing template: ");
		
		label = new Label(this, SWT.NONE);
		label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		label.setText(variableNode.getContributingTemplate());
	}
	

}
