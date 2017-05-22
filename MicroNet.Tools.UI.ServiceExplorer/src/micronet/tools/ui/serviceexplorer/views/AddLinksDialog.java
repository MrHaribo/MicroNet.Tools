package micronet.tools.ui.serviceexplorer.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import micronet.tools.core.ModelProvider;
import micronet.tools.core.ServiceProject;

public class AddLinksDialog extends Dialog {
	
	List<String> availableLinks;
	List<String> currentLinks;
	
	ServiceProject serviceProject;

	public AddLinksDialog(Shell shell, ServiceProject serviceProject) {
		super(shell);
		
		this.serviceProject = serviceProject;
		this.availableLinks = new ArrayList<>();
		this.currentLinks = serviceProject.getLinks();
		
		List<ServiceProject> serviceProjects = ModelProvider.INSTANCE.getServiceProjects();
		for (ServiceProject project : serviceProjects) {
			if (!currentLinks.contains(project.getName()))
				availableLinks.add(project.getName());
		}
		availableLinks.remove(serviceProject.getName());
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.getShell().setText("Add Links to " + serviceProject.getName());
		GridLayout layout = new GridLayout(3, false);
		layout.marginRight = 5;
		layout.marginLeft = 10;
		container.setLayout(layout);

		Label lblInfo = new Label(container, SWT.NONE);
		lblInfo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		lblInfo.setText("Available Links:");
		
		lblInfo = new Label(container, SWT.NONE);
		lblInfo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblInfo.setText("Current Links:");
		
		org.eclipse.swt.widgets.List avalibleList = new org.eclipse.swt.widgets.List (container, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		avalibleList.setItems((String[]) availableLinks.toArray(new String[availableLinks.size()]));
		avalibleList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));
				
		Button addButton = new Button(container, SWT.PUSH);
		addButton.setText("Add >");
		addButton.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false, 1, 1));

		org.eclipse.swt.widgets.List currentList = new org.eclipse.swt.widgets.List (container, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		currentList.setItems((String[]) currentLinks.toArray(new String[currentLinks.size()]));
		currentList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));
		
		Button removeButton = new Button(container, SWT.PUSH);
		removeButton.setText("< Remove");
		removeButton.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false, 1, 1));
		
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				for (String selectedService : avalibleList.getSelection()) {
					availableLinks.remove(selectedService);
					avalibleList.remove(selectedService);
					
					currentLinks.add(selectedService);
					currentList.add(selectedService);
				}
			}
		});
		
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				for (String selectedService : currentList.getSelection()) {
					availableLinks.add(selectedService);
					avalibleList.add(selectedService);
					
					currentLinks.remove(selectedService);
					currentList.remove(selectedService);
				}
			}
		});
		
		return container;
	}

	// override method to use "Login" as label for the OK button
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(450, 300);
	}

	@Override
	protected void okPressed() {
		super.okPressed();
	}

	public List<String> getCurrentLinks() {
		return currentLinks;
	}
}
