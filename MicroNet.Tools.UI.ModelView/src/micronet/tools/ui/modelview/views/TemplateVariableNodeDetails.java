package micronet.tools.ui.modelview.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;

import micronet.tools.core.ModelProvider;
import micronet.tools.ui.modelview.INode;
import micronet.tools.ui.modelview.ModelConstants;
import micronet.tools.ui.modelview.SyncEnumTree;
import micronet.tools.ui.modelview.SyncTemplateTree;
import micronet.tools.ui.modelview.actions.TemplateVariableRemoveAction;
import micronet.tools.ui.modelview.nodes.EntityTemplateNode;
import micronet.tools.ui.modelview.nodes.EntityVariableNode;
import micronet.tools.ui.modelview.nodes.EnumRootNode;
import micronet.tools.ui.modelview.variables.CollectionDescription;
import micronet.tools.ui.modelview.variables.ComponentDescription;
import micronet.tools.ui.modelview.variables.EnumDescription;
import micronet.tools.ui.modelview.variables.MapDescription;
import micronet.tools.ui.modelview.variables.NumberDescription;
import micronet.tools.ui.modelview.variables.NumberType;
import micronet.tools.ui.modelview.variables.VariableDescription;
import micronet.tools.ui.modelview.variables.VariableType;

public class TemplateVariableNodeDetails extends NodeDetails {

	private static String[] setTypes = { VariableType.ENUM.toString(), VariableType.CHAR.toString(),
			VariableType.STRING.toString(), NumberType.BYTE.toString(), NumberType.SHORT.toString(),
			NumberType.INT.toString(), NumberType.LONG.toString(), NumberType.FLOAT.toString(),
			NumberType.DOUBLE.toString(), };

	private Composite detailsPanel;

	private Combo typeSelect;

	private Composite detailsContainer;

	private EntityVariableNode variableNode;

	private Action refreshViewerAction;

	private TemplateVariableRemoveAction removeVariableAction;

	@Override
	public void setRefreshViewerAction(Action refreshViewerAction) {
		this.refreshViewerAction = refreshViewerAction;
		removeVariableAction.setRefreshViewerAction(refreshViewerAction, true);
	}

	@Override
	protected void removeNode() {
		removeVariableAction.run();
	}

	private void refreshViewer() {
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

	public TemplateVariableNodeDetails(EntityVariableNode variableNode, Composite parent, int style) {
		super(variableNode, parent, style, true);

		this.variableNode = variableNode;

		removeVariableAction = new TemplateVariableRemoveAction(getShell(), variableNode);
		removeVariableAction.setText("Remove Template");
		removeVariableAction.setToolTipText("Remove the Template.");

		detailsContainer = new Composite(this, SWT.BORDER);
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
				updateVariableDetails();
			}
		});

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
			if (loadEnumTree.getChildren().length == 0) {
				MessageDialog.openInformation(typeSelect.getShell(), "No Enum Present",
						"No enum has been defined yet. Define Enum first.");
				return new VariableDescription(VariableType.STRING);
			}
			return new EnumDescription(loadEnumTree.getChildren()[0].getName());
		case BOOLEAN:
			return new VariableDescription(VariableType.BOOLEAN);
		case CHAR:
			return new VariableDescription(VariableType.CHAR);
		case STRING:
		default:
			return new VariableDescription(VariableType.STRING);
		}
	}

	private Composite createVariableDetails(VariableDescription variableDesc, Composite parent) {

		switch (variableDesc.getType()) {
		case NUMBER:
			return new NumberDetails((NumberDescription) variableDesc, parent, SWT.BORDER);
		case LIST:
			return new EntryDetails((CollectionDescription) variableDesc, parent, SWT.BORDER);
		case SET:
			return new SetDetails((CollectionDescription) variableDesc, parent, SWT.BORDER);
		case MAP:
			return new MapDetails((MapDescription) variableDesc, parent, SWT.BORDER);
		case ENUM:
			return new EnumDetails((EnumDescription) variableDesc, parent, SWT.BORDER);
		case COMPONENT:
			return new ComponentDetails((ComponentDescription) variableDesc, parent, SWT.BORDER);
		case STRING:
		case BOOLEAN:
		case CHAR:
		default:
			return null;
		}
	}

	private void updateVariableDetails() {

		if (detailsPanel != null)
			detailsPanel.dispose();

		detailsPanel = createVariableDetails(variableNode.getVariabelDescription(), detailsContainer);
		if (detailsPanel != null && !detailsPanel.isDisposed()) {
			detailsPanel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false, 2, 1));
		}

		refreshDetailsPanel();
		
		saveTemplate();
		refreshViewer();
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

					refreshViewer();
				}
			});
		}
	}

	private class MapDetails extends Composite {

		private Combo keyTypeSelect;
		private Composite entryDetails;
		private Composite keyDetails;

		public MapDetails(MapDescription mapDesc, Composite parent, int style) {
			super(parent, style);

			setLayout(new GridLayout(2, false));

			Label label = new Label(this, SWT.NONE);
			label.setText("Key Type:");

			keyTypeSelect = new Combo(this, SWT.READ_ONLY);
			keyTypeSelect.setItems(setTypes);
			keyTypeSelect.setText(mapDesc.getKeyType().toString());
			keyTypeSelect.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			keyTypeSelect.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {

					VariableType variableType = Enum.valueOf(VariableType.class, keyTypeSelect.getText());
					VariableDescription variableDesc = createNewVariableDescription(variableType);
					mapDesc.setEntryType(variableDesc);
					
					if (keyDetails != null)
						keyDetails.dispose();
					keyDetails = createVariableDetails(variableDesc, MapDetails.this);
					if (keyDetails != null)
						keyDetails.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
					
					refreshDetailsPanel();
					
					saveTemplate();
					refreshViewer();
				}
			});

			entryDetails = new EntryDetails(mapDesc, this, style);
			entryDetails.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
		}
	}

	private class SetDetails extends Composite {

		private Composite entryDetails;
		
		private Combo setTypeSelect;

		public SetDetails(CollectionDescription collectionDesc, Composite parent, int style) {
			super(parent, style);

			setLayout(new GridLayout(2, false));

			Label label = new Label(this, SWT.NONE);
			label.setText("Set Type:");

			setTypeSelect = new Combo(this, SWT.READ_ONLY);
			setTypeSelect.setItems(setTypes);
			setTypeSelect.setText(collectionDesc.getEntryType().toString());
			setTypeSelect.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			setTypeSelect.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					
					VariableType variableType = Enum.valueOf(VariableType.class, setTypeSelect.getText());
					VariableDescription variableDesc = createNewVariableDescription(variableType);
					collectionDesc.setEntryType(variableDesc);
					
					if (entryDetails != null)
						entryDetails.dispose();
					entryDetails = createVariableDetails(variableDesc, SetDetails.this);
					if (entryDetails != null)
						entryDetails.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
					
					refreshDetailsPanel();
					
					saveTemplate();
					refreshViewer();
				}
			});
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
					
					updateSubEntryDetails(variableDesc);
				}
			});
			
			updateSubEntryDetails(collectionDesc.getEntryType());
		}
		
		private void updateSubEntryDetails(VariableDescription variableDesc) {
			if (subEntryDetails != null)
				subEntryDetails.dispose();
			subEntryDetails = createVariableDetails(variableDesc, EntryDetails.this);
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
					
					saveTemplate();
					refreshViewer();
				}
			});
		}
	}
}
