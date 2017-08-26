package micronet.tools.ui.serviceexplorer.views;

import java.util.HashMap;
import java.util.Map;

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

public class AddEnvArgsDialog extends Dialog {
	
	Map<String, String> envArgs;
	
	ServiceProject serviceProject;

	private Text argsText;

	public AddEnvArgsDialog(Shell shell, ServiceProject serviceProject) {
		super(shell);
		
		this.serviceProject = serviceProject;
		this.envArgs = serviceProject.getEnvArgs();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.getShell().setText("Add Env Args to " + serviceProject.getName());
		GridLayout layout = new GridLayout(1, false);
		layout.marginRight = 5;
		layout.marginLeft = 10;
		container.setLayout(layout);
		
		Label label = new Label(container, SWT.WRAP);
		label.setText("Add Environment Arguments for Native Launch Configurations to " + serviceProject.getName() + "(\"key=value\" separated by newline)");
		label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));

		argsText = new Text(container, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		argsText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		for (Map.Entry<String, String> arg : envArgs.entrySet()) {
			argsText.append(String.format("%s=%s\n", arg.getKey(), arg.getValue()));
		}
		
		return container;
	}

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
		String[] argArray = argsText.getText().split("\\r?\\n");
		envArgs = new HashMap<>();
		for (String entry : argArray) {
			String[] tokens = entry.split("=");
			if (tokens.length != 2)
				continue;
			envArgs.put(tokens[0], tokens[1]);
		}
		super.okPressed();
	}

	public Map<String, String> getEnvArgs() {
		return envArgs;
	}
}
