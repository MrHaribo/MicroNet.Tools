package micronet.tools.contribution;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class ContributionChoiceDialog extends Dialog {

	public enum ContributionChoice {
		NONE,
		KEEP,
		REPLACE,
	}
	
	private String title;
	private String message;
	private Object replaceChoice;
	private Object keepChoice;
	
	private ContributionChoice choice = ContributionChoice.NONE;
	
	public ContributionChoiceDialog(Shell parentShell, String title, String message, Object replaceChoice, Object keepChoice) {
		super(parentShell);
		this.title = title;
		this.message = message;
		this.replaceChoice = replaceChoice;
		this.keepChoice = keepChoice;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		
		container.getShell().setText(title);
		GridLayout layout = new GridLayout(2, false);
		layout.marginRight = 5;
		layout.marginLeft = 10;
		container.setLayout(layout);
		
		Label label = new Label(container, SWT.NONE);
		label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
		label.setText(message);

		
		Button button = new Button(container, SWT.NONE);
		button.setText("Change Type");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				choice = ContributionChoice.REPLACE;
				okPressed();
			}
		});
		label = new Label(container, SWT.NONE);
		label.setText("Contributed Type: " + replaceChoice.toString());
		
		button = new Button(container, SWT.NONE);
		button.setText("Keep Type");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				choice = ContributionChoice.KEEP;
				okPressed();
			}
		});
		label = new Label(container, SWT.NONE);
		label.setText("Existing Type: " + keepChoice.toString());

		return container;
	}

	// override method to use "Login" as label for the OK button
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		//createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(600, 250);
	}

	@Override
	protected void okPressed() {
		super.okPressed();
	}

	public ContributionChoice getChoice() {
		return choice;
	}
	
	
}
