package micronet.tools.ui.modelview.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.ListSelectionDialog;

import micronet.tools.core.ModelProvider;
import micronet.tools.filesync.SyncEnumTree;
import micronet.tools.filesync.SyncScripts;
import micronet.tools.filesync.SyncTemplateTree;
import micronet.tools.model.INode;
import micronet.tools.model.ModelConstants;
import micronet.tools.model.nodes.EntityTemplateNode;
import micronet.tools.model.nodes.EntityVariableDynamicNode;
import micronet.tools.model.nodes.EntityVariableNode;
import micronet.tools.model.nodes.EnumRootNode;
import micronet.tools.model.nodes.ModelNode;
import micronet.tools.model.nodes.ScriptRootNode;
import micronet.tools.model.variables.CollectionDescription;
import micronet.tools.model.variables.ComponentDescription;
import micronet.tools.model.variables.EnumDescription;
import micronet.tools.model.variables.GeometryDescription;
import micronet.tools.model.variables.GeometryType;
import micronet.tools.model.variables.MapDescription;
import micronet.tools.model.variables.NumberDescription;
import micronet.tools.model.variables.NumberType;
import micronet.tools.model.variables.ScriptDescription;
import micronet.tools.model.variables.VariableDescription;
import micronet.tools.model.variables.VariableType;
import micronet.tools.ui.modelview.actions.TemplateVariableRemoveAction;

public class TemplateVariableNodeDetails extends NodeDetails {

	private static String[] keyTypes = { VariableType.ENUM.toString(), VariableType.CHAR.toString(),
			VariableType.STRING.toString(), VariableType.NUMBER.toString(), };

	private Composite detailsPanel;

	private Combo typeSelect;

	private Composite detailsContainer;

	private EntityVariableNode variableNode;

	private Action refreshViewerAction;

	private TemplateVariableRemoveAction removeVariableAction;

	protected void variableDetailsChanged() {
	}

	@Override
	public void setRefreshViewerAction(Action refreshViewerAction) {
		this.refreshViewerAction = refreshViewerAction;
		removeVariableAction.setRefreshViewerAction(refreshViewerAction, true);
	}

	@Override
	protected void removeNode() {
		removeVariableAction.run();
	}

	protected void refreshViewer() {
		if (refreshViewerAction != null) {
			Event event = new Event();
			event.data = true;
			refreshViewerAction.runWithEvent(event);
		}
	}

	private void saveTemplate() {
		String sharedDir = ModelProvider.INSTANCE.getSharedDir();
		SyncTemplateTree.saveTemplateTree((EntityTemplateNode) variableNode.getParent(), sharedDir);
	}

	private void refreshDetailsPanel() {
		if (detailsPanel != null && !detailsPanel.isDisposed())
			detailsPanel.layout();
		detailsContainer.layout();
		detailsContainer.getParent().layout();
		detailsContainer.getParent().getParent().layout();
	}

	private void updateVariableDetails() {

		if (detailsPanel != null)
			detailsPanel.dispose();

		detailsPanel = createVariableDetails(variableNode.getVariabelDescription(), detailsContainer, SWT.BORDER);
		if (detailsPanel != null && !detailsPanel.isDisposed()) {
			detailsPanel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
		}

		refreshDetailsPanel();

		saveTemplate();
		refreshViewer();
	}

	public TemplateVariableNodeDetails(EntityVariableNode variableNode, Composite parent, int style) {
		super(variableNode, parent, style, true);

		this.variableNode = variableNode;

		removeVariableAction = new TemplateVariableRemoveAction(getShell(), variableNode);

		detailsContainer = new Composite(this, SWT.NONE);
		detailsContainer.setLayout(new GridLayout(2, false));

		Label label = new Label(detailsContainer, SWT.NONE);
		label.setText("Variable Type:");

		String[] baseTypes = Arrays.stream(VariableType.class.getEnumConstants()).map(Enum::name)
				.toArray(String[]::new);

		typeSelect = new Combo(detailsContainer, SWT.READ_ONLY);
		typeSelect.setItems(baseTypes);
		typeSelect.setText(variableNode.getVariabelDescription().getType().toString());
		typeSelect.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				VariableType variableType = Enum.valueOf(VariableType.class, typeSelect.getText());

				if (variableNode.getVariabelDescription().getType() != variableType) {
					VariableDescription variableDesc = createNewVariableDescription(variableType);
					variableNode.setVariabelDescription(variableDesc);
				}
				variableDetailsChanged();
				updateVariableDetails();
			}
		});

		if (variableNode instanceof EntityVariableDynamicNode) {
			EntityVariableDynamicNode dynamicVariable = (EntityVariableDynamicNode) variableNode;

			Composite ctorContainer = new Composite(this, SWT.NONE);
			ctorContainer.setLayout(new GridLayout(2, false));

			label = new Label(ctorContainer, SWT.NONE);
			label.setText("Constructor Argument");

			Button check = new Button(ctorContainer, SWT.CHECK);
			check.setSelection(dynamicVariable.isCtorArg());
			check.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					dynamicVariable.setCtorArg(check.getSelection());
					saveTemplate();
				}
			});
		}

		updateVariableDetails();
	}

	private VariableDescription createNewVariableDescription(VariableType variableType) {
		String sharedDir = ModelProvider.INSTANCE.getSharedDir();
		switch (variableType) {
		case NUMBER:
			return new NumberDescription(NumberType.INT);
		case LIST:
			return new CollectionDescription(VariableType.LIST, new VariableDescription(VariableType.STRING));
		case SET:
			return new CollectionDescription(VariableType.SET, new VariableDescription(VariableType.STRING));
		case MAP:
			return new MapDescription(new NumberDescription(NumberType.INT),
					new VariableDescription(VariableType.STRING));
		case COMPONENT:
			List<String> templateNames = SyncTemplateTree.getAllTemplateNames(sharedDir);
			return new ComponentDescription(templateNames.get(0));
		case ENUM:
			EnumRootNode loadEnumTree = SyncEnumTree.loadEnumTree(sharedDir);
			if (loadEnumTree.getChildren().size() == 0) {
				MessageDialog.openInformation(typeSelect.getShell(), "No Enum Present",
						"No enum has been defined yet. Define Enum first.");
				return new VariableDescription(VariableType.STRING);
			}
			return new EnumDescription(loadEnumTree.getChildren().get(0).getName());
		case BOOLEAN:
			return new VariableDescription(VariableType.BOOLEAN);
		case CHAR:
			return new VariableDescription(VariableType.CHAR);
		case SCRIPT:
			return new ScriptDescription(null);
		case GEOMETRY:
			return new GeometryDescription(GeometryType.VECTOR3);
		case STRING:
		default:
			return new VariableDescription(VariableType.STRING);
		}
	}

	private Composite createVariableDetails(VariableDescription variableDesc, Composite parent, int style) {

		switch (variableDesc.getType()) {
		case NUMBER:
			return new NumberDetails((NumberDescription) variableDesc, parent, style);
		case LIST:
			return new EntryDetails((CollectionDescription) variableDesc, parent, style);
		case SET:
			return new SetDetails((CollectionDescription) variableDesc, parent, style);
		case MAP:
			return new MapDetails((MapDescription) variableDesc, parent, style);
		case ENUM:
			return new EnumDetails((EnumDescription) variableDesc, parent, style);
		case COMPONENT:
			return new ComponentDetails((ComponentDescription) variableDesc, parent, style);
		case SCRIPT:
			return new ScriptDetails((ScriptDescription) variableDesc, parent, style);
		case GEOMETRY:
			return new GeometryDetails((GeometryDescription) variableDesc, parent, style);
		case STRING:
		case BOOLEAN:
		case CHAR:
		default:
			return null;
		}
	}
	
	private class GeometryDetails extends Composite {

		public GeometryDetails(GeometryDescription geometryDesc, Composite parent, int style) {
			super(parent, style);
		
			setLayout(new GridLayout(2, false));
	
			Label label = new Label(this, SWT.NONE);
			label.setText("Geometry Type:");
	
			String[] numberTypes = Arrays.stream(GeometryType.class.getEnumConstants()).map(Enum::name)
					.toArray(String[]::new);
	
			Combo numberTypeSelect = new Combo(this, SWT.READ_ONLY);
			numberTypeSelect.setItems(numberTypes);
			numberTypeSelect.setText(geometryDesc.getGeometryType().toString());
			numberTypeSelect.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					GeometryType geometryType = Enum.valueOf(GeometryType.class, numberTypeSelect.getText());
					geometryDesc.setGeometryType(geometryType);
	
					variableDetailsChanged();
					saveTemplate();
					refreshViewer();
				}
			});
		}
	}

	private class ScriptDetails extends Composite {

		private Composite argContainer;
		
		public ScriptDetails(ScriptDescription scriptDesc, Composite parent, int style) {
			super(parent, style);

			setLayout(new GridLayout(2, false));

			Label label = new Label(this, SWT.NONE);
			label.setText("Script Type:");

			String sharedDir = ModelProvider.INSTANCE.getSharedDir();
			ScriptRootNode scriptRootNode = SyncScripts.loadScripts(sharedDir);
			List<String> scriptTypes = new ArrayList<>();
			for (INode enumNode : scriptRootNode.getChildren()) {
				scriptTypes.add(enumNode.getName());
			}

			Combo scriptTypeSelect = new Combo(this, SWT.READ_ONLY);
			scriptTypeSelect.setItems(scriptTypes.toArray(new String[scriptTypes.size()]));
			if (scriptDesc.getScriptName() != null)
				scriptTypeSelect.setText(scriptDesc.getScriptName());
			scriptTypeSelect.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {

					scriptDesc.setScriptName(scriptTypeSelect.getText());

					String sharedDir = ModelProvider.INSTANCE.getSharedDir();
					SyncTemplateTree.saveTemplateTree((EntityTemplateNode) variableNode.getParent(), sharedDir);

					variableDetailsChanged();
					refreshViewer();
				}
			});

			label = new Label(this, SWT.NONE);
			label.setText("Arguments:\n");
			label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
			
			argContainer = new Composite(this, SWT.NONE);
			argContainer.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
			argContainer.setLayout(new GridLayout(1, false));
			
			
			Button button = new Button(this, SWT.NONE);
			button.setText("Add Member Arg");
			button.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					ModelNode parent = (ModelNode) variableNode.getParent();
					
					List<String> variables = new ArrayList<>();
					for (INode child : parent.getChildren()) {
						if (child instanceof EntityVariableNode && child != variableNode) {
							if (scriptDesc.getMemberArgs().contains(child.getName()))
								continue;
							variables.add(child.getName());
						}
					}
					
					ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), new LabelProvider());
					dialog.setTitle("Add Member Arguments");
					dialog.setMessage("Add Member Arguments to Script: " + variableNode.getName());
					dialog.setElements(variables.toArray());
					dialog.setMultipleSelection(true);
					dialog.open();
					
					if (dialog.getReturnCode() == ListSelectionDialog.OK) {
						
						for (Object resultEntry : dialog.getResult()) {
							scriptDesc.getMemberArgs().add(resultEntry.toString());
						}
						refreshArguments(scriptDesc);
						refreshDetailsPanel();
					}
				}
			});

			button = new Button(this, SWT.NONE);
			button.setText("Add External Arg");
			button.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {

					InputDialog dlg = new InputDialog(getShell(), "Add External Arg",
							"Add an External new Variable to " + scriptDesc.getScriptName(), "newVariable", null);
					if (dlg.open() == Window.OK) {
						String name = dlg.getValue();
						if (name == null)
							return;

						if (!ModelConstants.isValidJavaIdentifier(name)) {
							MessageDialog.openInformation(getShell(), "Invalid Name", "\"" + name + "\" is an invalid name.");
							return;
						}
						
						if (scriptDesc.getExternalArgs().containsKey(name)) {
							MessageDialog.openInformation(getShell(), "Duplicate Name", "\"" + name + "\" is an invalid name.");
							return;
						}
						
						List<VariableDescription> availableArgumentTypes = new ArrayList<>();
						availableArgumentTypes.add(new VariableDescription(VariableType.STRING));
						availableArgumentTypes.add(new NumberDescription(NumberType.BYTE));
						availableArgumentTypes.add(new NumberDescription(NumberType.DOUBLE));
						availableArgumentTypes.add(new NumberDescription(NumberType.FLOAT));
						availableArgumentTypes.add(new NumberDescription(NumberType.INT));
						availableArgumentTypes.add(new NumberDescription(NumberType.LONG));
						availableArgumentTypes.add(new NumberDescription(NumberType.SHORT));
						
						ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), new LabelProvider());
						dialog.setTitle("External Argument Type");
						dialog.setMessage("Select a Type for: " + name);
						dialog.setElements(availableArgumentTypes.toArray());
						dialog.setMultipleSelection(false);
						dialog.open();
						
						if (dialog.getReturnCode() == ListSelectionDialog.OK) {
							for (Object resultEntry : dialog.getResult()) {
								
								if (resultEntry instanceof NumberDescription)
									scriptDesc.getExternalArgs().put(name, (NumberDescription) resultEntry);
								else
									scriptDesc.getExternalArgs().put(name, (VariableDescription) resultEntry);
								break;
							}
							refreshArguments(scriptDesc);
							refreshDetailsPanel();
						}
					}
				}
			});
			
			refreshArguments(scriptDesc);
			refreshDetailsPanel();
		}
		
		private void refreshArguments(ScriptDescription scriptDesc) {

			for (Control c : argContainer.getChildren()) {
				c.dispose();
			}
			
			for (String memberArg : scriptDesc.getMemberArgs()) {
				Composite argEntry = new Composite(argContainer, SWT.NONE);
				argEntry.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
				argEntry.setLayout(new GridLayout(2, false));

				Label label = new Label(argEntry, SWT.NONE);
				label.setText(memberArg);
				label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

				Button button = new Button(argEntry, SWT.NONE);
				button.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
				button.setText("Remove");
				button.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						scriptDesc.getMemberArgs().remove(memberArg);
						refreshArguments(scriptDesc);
					};
				});
			}
			
			for (Map.Entry<String, VariableDescription> externalArg : scriptDesc.getExternalArgs().entrySet()) {
				Composite argEntry = new Composite(argContainer, SWT.NONE);
				argEntry.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
				argEntry.setLayout(new GridLayout(2, false));

				Label label = new Label(argEntry, SWT.NONE);
				label.setText(externalArg.getKey() + "(" + externalArg.getValue() + ")");
				label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

				Button button = new Button(argEntry, SWT.NONE);
				button.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
				button.setText("Remove");
				button.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						scriptDesc.getExternalArgs().remove(externalArg.getKey());
						refreshArguments(scriptDesc);
					};
				});
			}
			
			variableDetailsChanged();
			saveTemplate();
			refreshViewer();
			
			refreshDetailsPanel();
		}
	}

	private class EnumDetails extends Composite {

		public EnumDetails(EnumDescription enumDesc, Composite parent, int style) {
			super(parent, style);

			setLayout(new GridLayout(2, false));

			Label label = new Label(this, SWT.NONE);
			label.setText("Enum Type:");

			String sharedDir = ModelProvider.INSTANCE.getSharedDir();
			EnumRootNode enumRootNode = SyncEnumTree.loadEnumTree(sharedDir);
			List<String> enumTypes = new ArrayList<>();
			for (INode enumNode : enumRootNode.getChildren()) {
				enumTypes.add(enumNode.getName());
			}

			Combo enumTypeSelect = new Combo(this, SWT.READ_ONLY);
			enumTypeSelect.setItems(enumTypes.toArray(new String[enumTypes.size()]));
			enumTypeSelect.setText(enumDesc.getEnumType());
			enumTypeSelect.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {

					enumDesc.setEnumType(enumTypeSelect.getText());

					String sharedDir = ModelProvider.INSTANCE.getSharedDir();
					SyncTemplateTree.saveTemplateTree((EntityTemplateNode) variableNode.getParent(), sharedDir);

					variableDetailsChanged();
					refreshViewer();
				}
			});
		}
	}

	private class MapDetails extends Composite {

		private Composite entryDetails;
		private Composite keyDetails;

		private Composite keyPanel;

		public MapDetails(MapDescription mapDesc, Composite parent, int style) {
			super(parent, style);

			setLayout(new GridLayout(1, false));

			keyPanel = new Composite(this, SWT.NONE);
			keyPanel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			keyPanel.setLayout(new GridLayout(2, false));

			Label label = new Label(keyPanel, SWT.NONE);
			label.setText("Key Type:");

			Combo keyTypeSelect = new Combo(keyPanel, SWT.READ_ONLY);
			keyTypeSelect.setItems(keyTypes);
			keyTypeSelect.setText(mapDesc.getKeyType().getType().toString());
			keyTypeSelect.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			keyTypeSelect.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {

					VariableType variableType = Enum.valueOf(VariableType.class, keyTypeSelect.getText());
					VariableDescription variableDesc = createNewVariableDescription(variableType);
					mapDesc.setKeyType(variableDesc);

					variableDetailsChanged();
					updateKeyDetails(variableDesc);
				}
			});

			updateKeyDetails(mapDesc.getKeyType());

			entryDetails = new EntryDetails(mapDesc, this, style);
			entryDetails.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
		}

		private void updateKeyDetails(VariableDescription variableDesc) {
			if (keyDetails != null)
				keyDetails.dispose();
			keyDetails = createVariableDetails(variableDesc, keyPanel, SWT.NONE);
			if (keyDetails != null)
				keyDetails.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));

			keyPanel.layout();
			refreshDetailsPanel();
			saveTemplate();
			refreshViewer();
		}
	}

	private class SetDetails extends Composite {

		private Composite entryDetails;

		private Combo keyTypeSelect;

		public SetDetails(CollectionDescription collectionDesc, Composite parent, int style) {
			super(parent, style);

			setLayout(new GridLayout(2, false));

			Label label = new Label(this, SWT.NONE);
			label.setText("Key Type:");

			keyTypeSelect = new Combo(this, SWT.READ_ONLY);
			keyTypeSelect.setItems(keyTypes);
			keyTypeSelect.setText(collectionDesc.getEntryType().getType().toString());
			keyTypeSelect.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			keyTypeSelect.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {

					VariableType variableType = Enum.valueOf(VariableType.class, keyTypeSelect.getText());
					VariableDescription variableDesc = createNewVariableDescription(variableType);
					collectionDesc.setEntryType(variableDesc);

					variableDetailsChanged();
					updateKeyDetails(variableDesc);
				}
			});

			updateKeyDetails(collectionDesc.getEntryType());
		}

		private void updateKeyDetails(VariableDescription variableDesc) {
			if (entryDetails != null)
				entryDetails.dispose();
			entryDetails = createVariableDetails(variableDesc, SetDetails.this, SWT.NONE);
			if (entryDetails != null)
				entryDetails.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));

			refreshDetailsPanel();
			saveTemplate();
			refreshViewer();
		}
	}

	private class EntryDetails extends Composite {

		Composite subEntryDetails;

		public EntryDetails(CollectionDescription collectionDesc, Composite parent, int style) {
			super(parent, style);

			setLayout(new GridLayout(2, false));

			Label label = new Label(this, SWT.NONE);
			label.setText("Entry Type:");

			String[] baseTypes = Arrays.stream(VariableType.class.getEnumConstants()).map(Enum::name)
					.toArray(String[]::new);

			Combo entryTypeSelect = new Combo(this, SWT.READ_ONLY);
			entryTypeSelect.setItems(baseTypes);
			entryTypeSelect.setText(collectionDesc.getEntryType().getType().toString());
			entryTypeSelect.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));
			entryTypeSelect.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {

					VariableType variableType = Enum.valueOf(VariableType.class, entryTypeSelect.getText());
					VariableDescription variableDesc = createNewVariableDescription(variableType);
					collectionDesc.setEntryType(variableDesc);

					variableDetailsChanged();
					updateSubEntryDetails(variableDesc);
				}
			});

			updateSubEntryDetails(collectionDesc.getEntryType());
		}

		private void updateSubEntryDetails(VariableDescription variableDesc) {
			if (subEntryDetails != null)
				subEntryDetails.dispose();
			subEntryDetails = createVariableDetails(variableDesc, EntryDetails.this, SWT.NONE);
			if (subEntryDetails != null)
				subEntryDetails.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));

			refreshDetailsPanel();

			saveTemplate();
			refreshViewer();
		}
	}

	private class ComponentDetails extends Composite {

		public ComponentDetails(ComponentDescription componentDesc, Composite parent, int style) {
			super(parent, style);

			String sharedDir = ModelProvider.INSTANCE.getSharedDir();
			List<String> templateNames = SyncTemplateTree.getAllTemplateNames(sharedDir);
			String[] items = templateNames.toArray(new String[templateNames.size()]);

			setLayout(new GridLayout(3, false));

			Label label = new Label(this, SWT.NONE);
			label.setText("Component Type:");

			Combo componentTypeSelect = new Combo(this, SWT.READ_ONLY);
			componentTypeSelect.setItems(items);
			componentTypeSelect.setText(componentDesc.getComponentType());
			componentTypeSelect.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 2, 1));
			componentTypeSelect.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					componentDesc.setComponentType(componentTypeSelect.getText());

					variableDetailsChanged();
					saveTemplate();
					refreshViewer();
				}
			});
		}
	}

	private class NumberDetails extends Composite {

		public NumberDetails(NumberDescription numberDesc, Composite parent, int style) {
			super(parent, style);

			setLayout(new GridLayout(2, false));

			Label label = new Label(this, SWT.NONE);
			label.setText("Number Type:");

			String[] numberTypes = Arrays.stream(NumberType.class.getEnumConstants()).map(Enum::name)
					.toArray(String[]::new);

			Combo numberTypeSelect = new Combo(this, SWT.READ_ONLY);
			numberTypeSelect.setItems(numberTypes);
			numberTypeSelect.setText(numberDesc.getNumberType().toString());
			numberTypeSelect.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					NumberType numberType = Enum.valueOf(NumberType.class, numberTypeSelect.getText());
					numberDesc.setNumberType(numberType);

					variableDetailsChanged();
					saveTemplate();
					refreshViewer();
				}
			});
		}
	}
}
