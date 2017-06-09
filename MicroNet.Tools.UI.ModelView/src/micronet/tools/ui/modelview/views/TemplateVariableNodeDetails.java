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
	
	private static String[] setTypes = {
		VariableType.CHAR.toString(),
		VariableType.STRING.toString(),
		NumberType.BYTE.toString(),
		NumberType.SHORT.toString(),
		NumberType.INT.toString(),
		NumberType.LONG.toString(),
		NumberType.FLOAT.toString(),
		NumberType.DOUBLE.toString(),
	};
	
	private static String[] listTypes = {
		VariableType.ENUM.toString(),
		VariableType.BOOLEAN.toString(),
		VariableType.CHAR.toString(),
		VariableType.STRING.toString(),
		NumberType.BYTE.toString(),
		NumberType.SHORT.toString(),
		NumberType.INT.toString(),
		NumberType.LONG.toString(),
		NumberType.FLOAT.toString(),
		NumberType.DOUBLE.toString(),
	};
	
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

	public TemplateVariableNodeDetails(EntityVariableNode variableNode, Composite parent, int style) {
		super(variableNode, parent, style, true);
		
		this.variableNode = variableNode;
		
		removeVariableAction = new TemplateVariableRemoveAction(getShell(), variableNode);
		removeVariableAction.setText("Remove Template");
		removeVariableAction.setToolTipText("Remove the Template.");

		
		detailsContainer = new Composite(this, SWT.NONE);
		detailsContainer.setLayout(new GridLayout(2, false));

		Label label = new Label(detailsContainer, SWT.NONE);
		label.setText("Variable Type:");
		
		String[] baseTypes = Arrays.stream(VariableType.class.getEnumConstants()).map(Enum::name).toArray(String[]::new);
		
		typeSelect = new Combo(detailsContainer, SWT.READ_ONLY);
		typeSelect.setItems(baseTypes);
		typeSelect.setText(variableNode.getVariabelDescription().getType().toString());
		typeSelect.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				VariableType variableType = Enum.valueOf(VariableType.class, typeSelect.getText());
				String sharedDir = ModelProvider.INSTANCE.getSharedDir();
				
				if (variableNode.getVariabelDescription().getType() != variableType){
					switch (variableType) {
					case NUMBER:
						variableNode.setVariabelDescription(new NumberDescription(NumberType.INT));
						break;
					case LIST:
						variableNode.setVariabelDescription(new CollectionDescription(VariableType.LIST, VariableType.STRING.toString()));
						break;
					case SET:
						variableNode.setVariabelDescription(new CollectionDescription(VariableType.SET, VariableType.STRING.toString()));
						break;
					case MAP:
						variableNode.setVariabelDescription(new MapDescription(NumberType.INT.toString(), VariableType.STRING.toString()));
						break;
					case COMPONENT:
						List<String> templateNames = SyncTemplateTree.getAllTemplateNames(sharedDir);
						variableNode.setVariabelDescription(new ComponentDescription(templateNames.get(0)));
						break;
					case ENUM:
						EnumRootNode loadEnumTree = SyncEnumTree.loadEnumTree(sharedDir);
						if (loadEnumTree.getChildren().length == 0) {
							variableNode.setVariabelDescription(new VariableDescription(VariableType.STRING));
							typeSelect.setText(VariableType.STRING.toString());
							MessageDialog.openInformation(typeSelect.getShell(), "No Enum Present", "No enum has been defined yet. Define Enum first.");
							return;
						}
						variableNode.setVariabelDescription(new EnumDescription(loadEnumTree.getChildren()[0].getName()));
						break;
					case STRING:
						variableNode.setVariabelDescription(new VariableDescription(VariableType.STRING));
						break;
					case BOOLEAN:
						variableNode.setVariabelDescription(new VariableDescription(VariableType.BOOLEAN));
						break;
					case CHAR:
						variableNode.setVariabelDescription(new VariableDescription(VariableType.CHAR));
						break;

					}
				}
				updateVariableDetails();
			}
		});
		
		updateVariableDetails();
	}
	
	private void updateVariableDetails() {
		
		if (detailsPanel != null)
			detailsPanel.dispose();
		
		VariableType variableType = Enum.valueOf(VariableType.class, typeSelect.getText());
		switch (variableType) {
		case NUMBER:
			detailsPanel = new NumberDetails(detailsContainer, SWT.NONE);
			break;
		case LIST:
			detailsPanel = new ListDetails(detailsContainer, SWT.NONE);
			break;
		case SET:
			detailsPanel = new SetDetails(detailsContainer, SWT.NONE);
			break;
		case MAP:
			detailsPanel = new MapDetails(detailsContainer, SWT.NONE);
			break;
		case ENUM:
			detailsPanel = new EnumDetails(detailsContainer, SWT.NONE, enumType -> {
				variableNode.setVariabelDescription(new EnumDescription(enumType));
				String sharedDir = ModelProvider.INSTANCE.getSharedDir();
				SyncTemplateTree.saveTemplateTree((EntityTemplateNode)variableNode.getParent(), sharedDir);
			});
			break;
		case COMPONENT:
			detailsPanel = new ComponentDetails(detailsContainer, SWT.NONE);
			break;
		case STRING:
		case BOOLEAN:
		case CHAR:
		}
		
		if (detailsPanel != null && !detailsPanel.isDisposed()) {
			GridData gridData = new GridData(SWT.LEFT, SWT.TOP, true, false);
			gridData.horizontalSpan = 2;
			detailsPanel.setLayoutData(gridData);
			detailsPanel.layout();
		}
		detailsContainer.layout();
		detailsContainer.getParent().layout();
		detailsContainer.getParent().getParent().layout();
		
		String sharedDir = ModelProvider.INSTANCE.getSharedDir();
		SyncTemplateTree.saveTemplateTree((EntityTemplateNode)variableNode.getParent(), sharedDir);
		
		refreshViewer();
	}
	
	private class EnumDetails extends Composite {

		public EnumDetails(Composite parent, int style, Consumer<String> changedCallback) {
			super(parent, style);
			
			EnumDescription numberDesc = (EnumDescription) variableNode.getVariabelDescription();
			
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
			enumTypeSelect.setText(numberDesc.getEnumType());
			enumTypeSelect.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					changedCallback.accept(enumTypeSelect.getText());
				}
			});
		}
	}
	
	private class MapDetails extends Composite {

		private Combo keyTypeSelect;
		private EntryDetails entryDetails;

		public MapDetails(Composite parent, int style) {
			super(parent, style);
			
			setLayout(new GridLayout(2, false));
			
			MapDescription mapDesc = (MapDescription) variableNode.getVariabelDescription();
			
			Label label = new Label(this, SWT.NONE);
			label.setText("Key Type:");
			
			keyTypeSelect = new Combo(this, SWT.READ_ONLY);
			keyTypeSelect.setItems(setTypes);
			keyTypeSelect.setText(mapDesc.getKeyType());
			keyTypeSelect.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			keyTypeSelect.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					updateMapDescription();
				}
			});
			
			entryDetails = new EntryDetails(mapDesc, this, style, entryType -> {
				updateMapDescription();
			});
			entryDetails.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
		}

		private void updateMapDescription() {
			MapDescription mapDesc = new MapDescription(keyTypeSelect.getText(), entryDetails.getSelectedEntryType());
			variableNode.setVariabelDescription(mapDesc);
			String sharedDir = ModelProvider.INSTANCE.getSharedDir();
			SyncTemplateTree.saveTemplateTree((EntityTemplateNode)variableNode.getParent(), sharedDir);
			refreshViewer();
		}
	}
	
	private class SetDetails extends Composite {

		private Combo setTypeSelect;

		public SetDetails(Composite parent, int style) {
			super(parent, style);
			
			CollectionDescription collectionDesc = (CollectionDescription) variableNode.getVariabelDescription();
			
			setLayout(new GridLayout(2, false));
			
			Label label = new Label(this, SWT.NONE);
			label.setText("Set Entry Type:");

			setTypeSelect = new Combo(this, SWT.READ_ONLY);
			setTypeSelect.setItems(setTypes);
			setTypeSelect.setText(collectionDesc.getEntryType());
			setTypeSelect.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			setTypeSelect.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					updateSetDescription();
				}
			});
		}
		
		private void updateSetDescription() {
			variableNode.setVariabelDescription(new CollectionDescription(VariableType.SET, setTypeSelect.getText()));
			String sharedDir = ModelProvider.INSTANCE.getSharedDir();
			SyncTemplateTree.saveTemplateTree((EntityTemplateNode)variableNode.getParent(), sharedDir);
			refreshViewer();
		}
	}
	
	private class ListDetails extends Composite {

		private EntryDetails entryDetails;

		public ListDetails(Composite parent, int style) {
			super(parent, style);

			setLayout(new GridLayout(1, false));
			
			CollectionDescription collectionDesc = (CollectionDescription) variableNode.getVariabelDescription();
			
			entryDetails = new EntryDetails(collectionDesc, this, style, entryType -> {
				updateListDescription();
			});
			
			//if (collectionDesc.getEntryType())
			
			entryDetails.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		}
		
		private void updateListDescription() {
			variableNode.setVariabelDescription(new CollectionDescription(VariableType.LIST, entryDetails.getSelectedEntryType()));
			String sharedDir = ModelProvider.INSTANCE.getSharedDir();
			SyncTemplateTree.saveTemplateTree((EntityTemplateNode)variableNode.getParent(), sharedDir);
			refreshViewer();
		}
	}
	
	private class EntryDetails extends Composite {

		private Button primitiveRadio;
		private Button templateRadio;
		private Combo listTypeSelect;
		
		public String getSelectedEntryType() {
			return listTypeSelect.getText();
		}

		public EntryDetails(CollectionDescription collectionDesc, Composite parent, int style, Consumer<String> changeCallback) {
			super(parent, style);
			
			VariableType variableType = ModelConstants.getVariableEntryTypeOfCollection(collectionDesc);
			NumberType numberType = ModelConstants.getNumberEntryTypeOfCollection(collectionDesc);
			boolean isTemplateType = variableType == null && numberType == null;
			
			String sharedDir = ModelProvider.INSTANCE.getSharedDir();
			List<String> templateNames = SyncTemplateTree.getAllTemplateNames(sharedDir);
			String[] items = isTemplateType ? templateNames.toArray(new String[templateNames.size()]) : listTypes;
			
			setLayout(new GridLayout(3, false));
			
			Label label = new Label(this, SWT.NONE);
			label.setText("List Enties:");
			
			primitiveRadio = new Button (this, SWT.RADIO);
			primitiveRadio.setText("Primitive");
			primitiveRadio.setSelection(!isTemplateType);
			primitiveRadio.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					listTypeSelect.setItems(listTypes);
					listTypeSelect.setText(VariableType.STRING.toString());
					changeCallback.accept(listTypeSelect.getText());
				}
			});
			
			templateRadio = new Button (this, SWT.RADIO);
			templateRadio.setText("Template");
			templateRadio.setSelection(isTemplateType);
			templateRadio.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					List<String> templateNames = SyncTemplateTree.getAllTemplateNames(sharedDir);
					listTypeSelect.setItems(templateNames.toArray(new String[templateNames.size()]));
					listTypeSelect.setText(templateNames.get(0));
					changeCallback.accept(listTypeSelect.getText());
				}
			});
			
			label = new Label(this, SWT.NONE);
			label.setText("List Entry Type:");

			listTypeSelect = new Combo(this, SWT.READ_ONLY);
			listTypeSelect.setItems(items);
			listTypeSelect.setText(collectionDesc.getEntryType());
			listTypeSelect.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 2, 1));
			listTypeSelect.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					changeCallback.accept(listTypeSelect.getText());
				}
			});
		}
	}
	
	private class ComponentDetails extends Composite {

		private Combo componentTypeSelect;
		
		public ComponentDetails(Composite parent, int style) {
			super(parent, style);
			
			ComponentDescription componentDesc = (ComponentDescription) variableNode.getVariabelDescription();
			
			String sharedDir = ModelProvider.INSTANCE.getSharedDir();
			List<String> templateNames = SyncTemplateTree.getAllTemplateNames(sharedDir);
			String[] items = templateNames.toArray(new String[templateNames.size()]);
			
			setLayout(new GridLayout(3, false));
			
			Label label = new Label(this, SWT.NONE);
			label.setText("Component Type:");

			componentTypeSelect = new Combo(this, SWT.READ_ONLY);
			componentTypeSelect.setItems(items);
			componentTypeSelect.setText(componentDesc.getComponentType());
			componentTypeSelect.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 2, 1));
			componentTypeSelect.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					variableNode.setVariabelDescription(new ComponentDescription(componentTypeSelect.getText()));
					String sharedDir = ModelProvider.INSTANCE.getSharedDir();
					SyncTemplateTree.saveTemplateTree((EntityTemplateNode)variableNode.getParent(), sharedDir);
					refreshViewer();
				}
			});
		}
	}
	
	private class NumberDetails extends Composite {

		public NumberDetails(Composite parent, int style) {
			super(parent, style);
			
			NumberDescription numberDesc = (NumberDescription) variableNode.getVariabelDescription();
			
			setLayout(new GridLayout(2, false));
			
			Label label = new Label(this, SWT.NONE);
			label.setText("Number Type:");
			
			String[] numberTypes = Arrays.stream(NumberType.class.getEnumConstants()).map(Enum::name).toArray(String[]::new);
			
			Combo numberTypeSelect = new Combo(this, SWT.READ_ONLY);
			numberTypeSelect.setItems(numberTypes);
			numberTypeSelect.setText(numberDesc.getNumberType().toString());
			numberTypeSelect.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					NumberType newType = Enum.valueOf(NumberType.class, numberTypeSelect.getText());
					variableNode.setVariabelDescription(new NumberDescription(newType));
					String sharedDir = ModelProvider.INSTANCE.getSharedDir();
					SyncTemplateTree.saveTemplateTree((EntityTemplateNode)variableNode.getParent(), sharedDir);
					refreshViewer();
				}
			});
		}
	}

	private void refreshViewer() {
		if (refreshViewerAction != null) {
			Event event = new Event();
			event.data = true;
			refreshViewerAction.runWithEvent(event);
		}
	}
}
