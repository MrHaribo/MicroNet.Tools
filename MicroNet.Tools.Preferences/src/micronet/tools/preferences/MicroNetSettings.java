package micronet.tools.preferences;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;
import micronet.tools.core.Activator;
import micronet.tools.core.Icons;
import micronet.tools.core.ModelProvider;
import micronet.tools.core.PreferenceConstants;
import micronet.tools.launch.utility.DockerUtility;
import micronet.tools.model.ModelConstants;

/**
 * This class represents a preference page that is contributed to the
 * Preferences dialog. By subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the
 * preference store that belongs to the main plug-in class. That way,
 * preferences can be accessed directly via the preference store.
 */

public class MicroNetSettings extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	Composite dockerStatus;
	Composite dockerNetworkStatus;

	public MicroNetSettings() {
		super(GRID);
		setPreferenceStore(ModelProvider.INSTANCE.getPreferenceStore());
		setDescription("The configuration of the MicroNet tools");
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */
	public void createFieldEditors() {

		Group dockerPanel = new Group(getFieldEditorParent(), SWT.NONE);
		dockerPanel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));
		dockerPanel.setText("Docker Settings");

		addField(new BooleanFieldEditor(PreferenceConstants.P_USE_DOCKER_TOOLBOX, "&Use Docker Toolbox", dockerPanel));

		addField(new DirectoryFieldEditor(PreferenceConstants.P_DOCKER_TOOLBOX_PATH, "&Docker Toolbox Directory:",
				dockerPanel));

		Button button = new Button(dockerPanel, SWT.NONE);
		button.setText("Test Docker");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				DockerUtility.testDocker(testSuccessful -> {
					Display.getDefault().asyncExec(() -> {
						if (testSuccessful) {
							dockerStatus.setBackgroundImage(Icons.IMG_CHECK.createImage());
						} else {
							dockerStatus.setBackgroundImage(Icons.IMG_REMOVE.createImage());
						}
					});
				});
			}
		});

		dockerStatus = new Composite(dockerPanel, SWT.BORDER);
		dockerStatus.setBackgroundImage(Icons.IMG_QUESTION.createImage());
		dockerStatus.setLayoutData(new GridData(16, 16));

		Group dockerNetworkPanel = new Group(getFieldEditorParent(), SWT.NONE);
		dockerNetworkPanel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		dockerNetworkPanel.setText("Application Docker Network");

		StringFieldEditor networkNameEditor = new StringFieldEditor(PreferenceConstants.P_DOCKER_NETWORK_NAME,
				"&Docker Network Name", dockerNetworkPanel);
		addField(networkNameEditor);

		button = new Button(dockerNetworkPanel, SWT.NONE);
		button.setText("Create Network");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				String networkName = networkNameEditor.getStringValue();

				if (!ModelConstants.isValidJavaIdentifier(networkName)) {
					MessageDialog.openError(getFieldEditorParent().getShell(), "Error Creating Network", "Invalid Network Name: " + networkName);
					return;
				}

				DockerUtility.createNetwork(networkName, result -> {
					Display.getDefault().asyncExec(() -> {
						if (result.contains("error") || result.contains("Error")) {
							MessageDialog.openError(getFieldEditorParent().getShell(), "Error Creating Network", result);
							return;
						}
						MessageDialog.openInformation(getFieldEditorParent().getShell(), "Network Created", networkName + " Network Created: " + result);
					});
				});
			}
		});

		Composite networkTestComposite = new Composite(dockerNetworkPanel, SWT.NONE);
		networkTestComposite.setLayout(new GridLayout(2, false));
		
		button = new Button(networkTestComposite, SWT.NONE);
		button.setText("Test Network");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				String networkName = networkNameEditor.getStringValue();
				
				DockerUtility.testNetwork(networkName, result -> {
					Display.getDefault().asyncExec(() -> {
						if (result.contains("error") || result.contains("Error")) {
							MessageDialog.openError(getFieldEditorParent().getShell(), "Error Network Detected", result);
							dockerNetworkStatus.setBackgroundImage(Icons.IMG_REMOVE.createImage());
							return;
						}
						dockerNetworkStatus.setBackgroundImage(Icons.IMG_CHECK.createImage());
						MessageDialog.openInformation(getFieldEditorParent().getShell(), "Network Tested Successful", result);
					});
				});
			}
		});
		
		dockerNetworkStatus = new Composite(networkTestComposite, SWT.BORDER);
		dockerNetworkStatus.setBackgroundImage(Icons.IMG_QUESTION.createImage());
		dockerNetworkStatus.setLayoutData(new GridData(16, 16));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

}