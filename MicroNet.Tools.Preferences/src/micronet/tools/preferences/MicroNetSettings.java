package micronet.tools.preferences;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

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

	private Composite dockerStatus;
	private Composite dockerNetworkStatus;
	
	private Group dockerPanel;
	private BooleanFieldEditor useToolboxEdit;
	private Label tolboxInfoLabel;
	private DirectoryFieldEditor toolboxDirectoryEdit;

	public MicroNetSettings() {
		super(GRID);
		setPreferenceStore(ModelProvider.INSTANCE.getPreferenceStore());
		setDescription("The configuration of the MicroNet tools");
	}

	@Override
	public void createFieldEditors() {

		createDockerPanel();

		createDockerNetworkPanel();

		createWorkspacePanel();
	}
	
	@Override
	protected void initialize() {
		super.initialize();
		
		useToolboxEdit.setPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent arg0) {
				tolboxInfoLabel.setEnabled(useToolboxEdit.getBooleanValue());
				toolboxDirectoryEdit.setEnabled(useToolboxEdit.getBooleanValue(), dockerPanel);
			}
		});
	}

	private void createWorkspacePanel() {
		Group workspacePanel = new Group(getFieldEditorParent(), SWT.NONE);
		workspacePanel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));
		workspacePanel.setText("Application Workspace");

		StringFieldEditor groupIdFieldEditor = new StringFieldEditor(PreferenceConstants.PREF_APP_GROUP_ID, "&Application GroupID (required)", workspacePanel);
		groupIdFieldEditor.setEmptyStringAllowed(false);
		addField(groupIdFieldEditor);
		
		StringFieldEditor atrifactIdFieldEditor = new StringFieldEditor(PreferenceConstants.PREF_APP_ARTIFACT_ID, "&Application ArtifactID (required)", workspacePanel);
		//atrifactIdFieldEditor.setEmptyStringAllowed(false);
		addField(atrifactIdFieldEditor);
		
		StringFieldEditor versionFieldEditor = new StringFieldEditor(PreferenceConstants.PREF_APP_VERSION, "&Application Version (required)", workspacePanel);
		//versionFieldEditor.setEmptyStringAllowed(false);
		addField(versionFieldEditor);
	}

	private void createDockerNetworkPanel() {
		Button button;
		Group dockerNetworkPanel = new Group(getFieldEditorParent(), SWT.NONE);
		dockerNetworkPanel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));
		dockerNetworkPanel.setText("Application Docker Network");

		StringFieldEditor networkNameEditor = new StringFieldEditor(PreferenceConstants.PREF_DOCKER_NETWORK_NAME,
				"&Docker Network Name", dockerNetworkPanel);
		addField(networkNameEditor);

		button = new Button(dockerNetworkPanel, SWT.NONE);
		button.setText("Create Network");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				String networkName = networkNameEditor.getStringValue();

				if (!ModelConstants.isValidJavaIdentifier(networkName)) {
					MessageDialog.openError(getFieldEditorParent().getShell(), "Error Creating Network",
							"Invalid Network Name: " + networkName);
					return;
				}

				DockerUtility.createNetwork(networkName, (success, result) -> {
					Display.getDefault().asyncExec(() -> {
						if (!success || isErrorResult(result)) {
							MessageDialog.openError(getFieldEditorParent().getShell(), "Error Creating Network", result);
							return;
						}
						MessageDialog.openInformation(getFieldEditorParent().getShell(), "Network Created", networkName + " Network Created: " + result);
					});
				});
			}
		});

		Composite networkTestPanel = new Composite(dockerNetworkPanel, SWT.NONE);
		networkTestPanel.setLayout(new GridLayout(2, false));

		button = new Button(networkTestPanel, SWT.NONE);
		button.setText("Test Network");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				String networkName = networkNameEditor.getStringValue();

				DockerUtility.testNetwork(networkName, (success, result) -> {
					Display.getDefault().asyncExec(() -> {
						if (!success || isErrorResult(result)) {
							dockerNetworkStatus.setBackgroundImage(Icons.IMG_REMOVE.createImage());
							MessageDialog.openError(getFieldEditorParent().getShell(), "Error Testing Network", result);
						} else {
							dockerNetworkStatus.setBackgroundImage(Icons.IMG_CHECK.createImage());
							MessageDialog.openInformation(getFieldEditorParent().getShell(), "Network Tested Successful", result);
						}

					});
				});
			}
		});

		dockerNetworkStatus = new Composite(networkTestPanel, SWT.BORDER);
		dockerNetworkStatus.setBackgroundImage(Icons.IMG_QUESTION.createImage());
		dockerNetworkStatus.setLayoutData(new GridData(16, 16));
	}

	private void createDockerPanel() {
		dockerPanel = new Group(getFieldEditorParent(), SWT.NONE);
		dockerPanel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));
		dockerPanel.setText("Docker Settings");

		boolean toolboxEnabled = ModelProvider.INSTANCE.getPreferenceStore().getBoolean(PreferenceConstants.PREF_USE_DOCKER_TOOLBOX);
		
		useToolboxEdit = new BooleanFieldEditor(PreferenceConstants.PREF_USE_DOCKER_TOOLBOX, "&Use Docker Toolbox", dockerPanel);
		addField(useToolboxEdit);
		
		tolboxInfoLabel = new Label(dockerPanel, SWT.NONE);
		tolboxInfoLabel.setText("Specify the Docker Toolbox /bin Directory containing the docker, docker-compose executables");
		tolboxInfoLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));
		tolboxInfoLabel.setEnabled(toolboxEnabled);
		
		toolboxDirectoryEdit = new DirectoryFieldEditor(PreferenceConstants.PREF_DOCKER_TOOLBOX_PATH, "&Docker Toolbox Bin", dockerPanel);
		toolboxDirectoryEdit.setEnabled(toolboxEnabled, dockerPanel);
		addField(toolboxDirectoryEdit);
		
		

		Button button = new Button(dockerPanel, SWT.NONE);
		button.setText("Test Docker");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				performApply();
				DockerUtility.testDocker((success, result) -> {
					Display.getDefault().asyncExec(() -> {
						if (!success || isErrorResult(result)) {
							dockerStatus.setBackgroundImage(Icons.IMG_REMOVE.createImage());
							MessageDialog.openError(getFieldEditorParent().getShell(), "Error Testing Docker", result);
						} else {
							dockerStatus.setBackgroundImage(Icons.IMG_CHECK.createImage());
							MessageDialog.openInformation(getFieldEditorParent().getShell(), "Docker Running", result);
						}
					});
				});
			}
		});

		dockerStatus = new Composite(dockerPanel, SWT.BORDER);
		dockerStatus.setBackgroundImage(Icons.IMG_QUESTION.createImage());
		dockerStatus.setLayoutData(new GridData(16, 16));
	}
	
	private boolean isErrorResult(String result) {
		return result.contains("error") || result.contains("Error");
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