package micronet.tools.ui.modelview.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class VariableNodeDetails extends NodeDetails {
	
	Label label;
	
	public VariableNodeDetails(Composite parent, int style) {
		super(parent, style);
		
		setLayout(new FillLayout(SWT.VERTICAL));

		label = new Label(this, SWT.NONE);
		label.setText("Variable Node: XXX");// + node.getName());
	}
}
