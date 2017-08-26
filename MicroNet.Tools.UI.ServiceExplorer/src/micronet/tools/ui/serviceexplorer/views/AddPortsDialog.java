package micronet.tools.ui.serviceexplorer.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import micronet.tools.core.ServiceProject;

public class AddPortsDialog extends Dialog {
	
	List<String> ports;
	
	ServiceProject serviceProject;

	private Text portsText;

	public AddPortsDialog(Shell shell, ServiceProject serviceProject) {
		super(shell);
		
		this.serviceProject = serviceProject;
		this.ports = serviceProject.getPorts();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.getShell().setText("Add Ports to " + serviceProject.getName());
		GridLayout layout = new GridLayout(1, false);
		layout.marginRight = 5;
		layout.marginLeft = 10;
		container.setLayout(layout);
		
		Label label = new Label(container, SWT.WRAP);
		label.setText("Add published Ports to " + serviceProject.getName() + "(\"hostPort:containerPort\" separated by newline)");
		label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));

		portsText = new Text(container, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		portsText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		for (String port : ports) {
			portsText.append(port + "\n");
		}
		
		return container;
	}

	// override method to use "Login" as label for the OK button
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(400, 300);
	}

	@Override
	protected void okPressed() {
		String[] portArray = portsText.getText().split("\\r?\\n");
		ports = new ArrayList<>(Arrays.asList(portArray));
		ports.remove("");
		super.okPressed();
	}

	public List<String> getPorts() {
		return ports;
	}
}
