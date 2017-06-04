package micronet.tools.ui.modelview.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class EnumNodeDetails extends NodeDetails {
	
	Label label;
	
	Composite detailsPanel;

	private Composite detailsContainer;
	
	public EnumNodeDetails(Composite parent, int style) {
		super(parent, style);
		
		detailsContainer = new Composite(this, SWT.NONE);
		detailsContainer.setLayout(new GridLayout(2, false));

		label = new Label(detailsContainer, SWT.NONE);
		label.setText("Set Enum Constants (CVS):");
		
		Text text = new Text (detailsContainer, SWT.NONE);
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	}
}
