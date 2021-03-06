package micronet.tools.ui.modelview.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import micronet.tools.core.ModelProvider;
import micronet.tools.filesync.SyncEnumTree;
import micronet.tools.filesync.SyncTemplateTree;
import micronet.tools.model.INode;
import micronet.tools.model.nodes.EntityTemplateNode;
import micronet.tools.model.nodes.EntityVariableNode;
import micronet.tools.model.nodes.EnumNode;
import micronet.tools.model.nodes.ModelNode;
import micronet.tools.model.nodes.PrefabVariableEntryNode;
import micronet.tools.model.nodes.PrefabVariableKeyNode;
import micronet.tools.model.nodes.PrefabVariableNode;
import micronet.tools.model.variables.CollectionDescription;
import micronet.tools.model.variables.ComponentDescription;
import micronet.tools.model.variables.EnumDescription;
import micronet.tools.model.variables.MapDescription;
import micronet.tools.model.variables.NumberDescription;
import micronet.tools.model.variables.VariableDescription;
import micronet.tools.model.variables.VariableType;

public class PrefabVariableNodeDetails extends NodeDetails implements IDetails {

	private PrefabVariableNode variableNode;
	
	private Composite detailsContainer = null;
	private Composite detailsPanel = null;
	
	private Button nullCheckBox;
	
	private Action refreshViewerAction;
	
	@Override
	public void setRefreshViewerAction(Action refreshViewerAction) {
		this.refreshViewerAction = refreshViewerAction;
	}
	
	private void refreshViewer() {
		if (refreshViewerAction != null) {
			Event event = new Event();
			event.data = false;
			refreshViewerAction.runWithEvent(event);
		}
	}
	
	private void refreshDetailsContainer() {
		if 	(detailsPanel != null && !detailsPanel.isDisposed())
			detailsPanel.layout();
		detailsContainer.layout();
		detailsContainer.getParent().layout();
	}
	
	@Override
	protected void removeNode() {
		if (variableNode instanceof PrefabVariableEntryNode) {
			ModelNode parentNode = (ModelNode)variableNode.getParent();
			parentNode.removeChild(variableNode);
			
			if (!(variableNode instanceof PrefabVariableKeyNode)) {
				int index = 0;
				for (INode siblingNode : parentNode.getChildren()) {
					PrefabVariableEntryNode prefabVariable = (PrefabVariableEntryNode)siblingNode;
					prefabVariable.setName(variableNode.getVariableType().toString() + index++);
				}
			}
			refreshViewer();
		}
	}
	
	public PrefabVariableNodeDetails(PrefabVariableNode variableNode, Composite parent, int style, boolean removable) {
		super(variableNode, parent, style, removable);
		
		this.variableNode = variableNode;
		
		detailsContainer = new Composite(this, SWT.NONE);
		detailsContainer.setLayout(new GridLayout(2, false));
		detailsContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Label label = new Label(detailsContainer, SWT.NONE);
		label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		label.setText("Variable Type: ");
		
		label = new Label(detailsContainer, SWT.NONE);
		label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		label.setText(variableNode.getVariableType().toString());
		
		if (!variableNode.getContributingTemplate().equals("")) {
			label = new Label(detailsContainer, SWT.NONE);
			label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			label.setText("Contributing template: ");
			
			label = new Label(detailsContainer, SWT.NONE);
			label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			label.setText(variableNode.getContributingTemplate());
		}
		
		new NullPanel(detailsContainer, SWT.NONE);
		
		if (variableNode.getVariableValue() != null)
			updateVariableDetails();
	}

	private void updateVariableDetails() {

		
		switch (variableNode.getVariableType()) {
		case BOOLEAN:
			detailsPanel = new BooleanEditor(detailsContainer, SWT.NONE);
			break;
		case CHAR:
			detailsPanel = new CharEditor(detailsContainer, SWT.NONE);
			break;
		case ENUM:
			detailsPanel = new EnumEditor(detailsContainer, SWT.NONE);
			break;
		case NUMBER:
			detailsPanel = new NumberEditor(detailsContainer, SWT.NONE);
			break;
		case STRING:
			detailsPanel = new StringEditor(detailsContainer, SWT.NONE);
			break;
		case COMPONENT:
			detailsPanel = new ComponentEditor(detailsContainer, SWT.NONE);
			break;
		case LIST:
			detailsPanel = new ListEditor(detailsContainer, SWT.NONE);
			break;
		case SET:
			detailsPanel = new SetEditor(detailsContainer, SWT.NONE);
			break;
		case MAP:
			detailsPanel = new MapEditor(detailsContainer, SWT.NONE);
		default:
			break;
			
		}
	}
	
	private Object parseVariableValue(VariableDescription variableDescription, String value) {
		switch (variableDescription.getType()) {
		case BOOLEAN:
			return value.equals("true");
		case CHAR:
			return value.length() > 0 ? value.charAt(0) : null;
		case COMPONENT:
			return null;
		case ENUM:
			return value;
		case LIST:
			return null;
		case MAP:
			return null;
		case NUMBER:
			NumberDescription numberDesc = (NumberDescription) variableDescription;
			return parseNumberVariableValue(numberDesc, value);
		case SET:
			return null;
		case STRING:
			return value;
		default:
			return value;
		}
	}
	
	private Object parseNumberVariableValue(NumberDescription numberDescription, String valueString) {
		try {
			switch (numberDescription.getNumberType()) {
			case BYTE:
				return Byte.parseByte(valueString);
			case DOUBLE:
				return Double.parseDouble(valueString);
			case FLOAT:
				return Float.parseFloat(valueString);
			case INT:
				return Integer.parseInt(valueString);
			case LONG:
				return Long.parseLong(valueString);
			case SHORT:
				return Short.parseShort(valueString);
			default:
				return null;
			}
		} catch (NumberFormatException ex) {
			return null;
		}
	}
	
	private String promtKey(VariableDescription keyType) {
		String value = null;
		if (keyType.getType().equals(VariableType.ENUM)) {
			
			EnumDescription enumDesc = (EnumDescription) keyType;

			String sharedDir = ModelProvider.INSTANCE.getSharedDir();
			EnumNode enumMirror = SyncEnumTree.loadEnum(enumDesc.getEnumType(), sharedDir);
			List<String> values = enumMirror.getEnumConstants();
			
			if (values.size() == 0) {
				MessageDialog.openConfirm(detailsContainer.getShell(), "No Enum Constants", "No enum Constants are defined in: " + enumMirror);
				return null;
			}
			
			ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), new LabelProvider());
			dialog.setElements(values.toArray());
			dialog.setTitle("Select A Enum Constant");
			if (dialog.open() != Window.OK)
				return null;
			
			Object[] selectedType = dialog.getResult();
			if (selectedType.length == 0 || selectedType.length > 1)
				return null;
			
			value = selectedType[0].toString();
			
		} else {
			InputDialog dlg = new InputDialog(getShell(), "Add Set Entry", "Enter new Set Entry of Type: " + keyType, "over 9000", null);
			if (dlg.open() == Window.OK) {
				value = dlg.getValue();
				if (value == null)
					return null;
				
				Object keyValue = parseVariableValue(keyType, value);
				if (keyValue == null) {
					MessageDialog.openInformation(getShell(), "Invalid Key", "Invalid Key input for Type :" + keyType);
					return null;
				}
			}
		}
		for (INode childNode : variableNode.getChildren()) {
			if (childNode instanceof PrefabVariableNode) {
				PrefabVariableNode existingVariable = (PrefabVariableNode) childNode;
				if (existingVariable.getVariableValue().toString().equals(value)) {
					MessageDialog.openInformation(getShell(), "Duplicate Set Entry", "Entry has already been added to the set");
					return null;
				}
			}
		}
		return value;
	}
	
	private class MapEditor extends Composite {

		private VariableDescription keyDesc = null;
		private VariableDescription entryDesc = null;

		public MapEditor(Composite parent, int style) {
			super(parent, style);
			setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
			setLayout(new GridLayout(2, false));
			
			variableNode.setVariableValue(new Object());
			
			MapDescription setDescription = (MapDescription) variableNode.getVariableDescription();
			entryDesc = setDescription.getEntryType();
			keyDesc = setDescription.getKeyType();
			
			Label label = new Label(this, SWT.NONE);
			label.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
			label.setText("Key Type:");
			
			label = new Label(this, SWT.NONE);
			label.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
			label.setText(keyDesc.toString());
			
			label = new Label(this, SWT.NONE);
			label.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
			label.setText("Entry Type:");
			
			label = new Label(this, SWT.NONE);
			label.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
			label.setText(entryDesc.toString());
			
			Button button = new Button(this, SWT.NONE);
			button.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 2, 1));
			button.setText("Add Entry");
			button.addSelectionListener(new SelectionAdapter() {
				
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					
					String keyValueString = promtKey(keyDesc);
					if (keyValueString == null)
						return;
					
					Object keyValue = parseVariableValue(keyDesc, keyValueString);
					
					PrefabVariableKeyNode keyNode = new PrefabVariableKeyNode(keyValueString, keyDesc);
					keyNode.setName(keyValueString);
					keyNode.setVariableValue(keyValue);
					keyNode.setEditable(false);
					variableNode.addChild(keyNode);
					
					PrefabVariableNode valueNode = new PrefabVariableNode("value", entryDesc);
					keyNode.addChild(valueNode);
					
					refreshViewer();
				}
			});
		}
	}
	
	private class SetEditor extends Composite {

		private VariableDescription entryDesc = null;

		public SetEditor(Composite parent, int style) {
			super(parent, style);
			setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
			setLayout(new GridLayout(2, false));
			
			variableNode.setVariableValue(new Object());
			
			CollectionDescription setDescription = (CollectionDescription) variableNode.getVariableDescription();
			entryDesc = setDescription.getEntryType();
			
			Label label = new Label(this, SWT.NONE);
			label.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
			label.setText("Entry Type:");
			
			label = new Label(this, SWT.NONE);
			label.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
			label.setText(entryDesc.toString());
			
			Button button = new Button(this, SWT.NONE);
			button.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 2, 1));
			button.setText("Add Entry");
			button.addSelectionListener(new SelectionAdapter() {
				
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					
					String value = promtKey(entryDesc);
					if (value == null)
						return;
					
					Object variableValue = parseVariableValue(entryDesc, value);
					
					String name = entryDesc.getType().toString() + variableNode.getChildren().size();
					PrefabVariableEntryNode prefabVariable = new PrefabVariableEntryNode(name, entryDesc);
					prefabVariable.setName(name);
					prefabVariable.setVariableValue(variableValue);
					prefabVariable.setEditable(false);
					variableNode.addChild(prefabVariable);
					
					refreshViewer();
				}
			});
		}
	}
	
	private class ListEditor extends Composite {

		public ListEditor(Composite parent, int style) {
			super(parent, style);
			setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
			setLayout(new GridLayout(2, false));
			
			variableNode.setVariableValue(new Object());
			
			CollectionDescription listDescription = (CollectionDescription) variableNode.getVariableDescription();
			VariableDescription entryDesc = listDescription.getEntryType();
			
			Label label = new Label(this, SWT.NONE);
			label.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
			label.setText("Entry Type:");
			
			label = new Label(this, SWT.NONE);
			label.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
			label.setText(entryDesc.toString());
			
			Button button = new Button(this, SWT.NONE);
			button.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 2, 1));
			button.setText("Add Entry");
			button.addSelectionListener(new SelectionAdapter() {
				
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					String entryName = entryDesc.getType().toString() + variableNode.getChildren().size();
					PrefabVariableEntryNode prefabVariable = new PrefabVariableEntryNode(entryName, entryDesc);
					prefabVariable.setName(entryName);
					variableNode.addChild(prefabVariable);
					refreshViewer();
				}
			});
			
			refreshDetailsContainer();
		}
	}
	
	private class ComponentEditor extends Composite{
		
		public ComponentEditor(Composite parent, int style) {
			super(parent, style);
			setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
			setLayout(new GridLayout(2, false));
			
			ComponentDescription componentDesc = (ComponentDescription) variableNode.getVariableDescription();
			
			Label label = new Label(this, SWT.NONE);
			label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			label.setText("Compoment Type:");

			label = new Label(this, SWT.NONE);
			label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			label.setText(componentDesc.getComponentType());

			String sharedDir = ModelProvider.INSTANCE.getSharedDir();
			EntityTemplateNode templateNodeMirror = SyncTemplateTree.loadTemplateType(componentDesc.getComponentType(), sharedDir);
			createVariables(templateNodeMirror);

			variableNode.setVariableValue(new Object());
			
			refreshDetailsContainer();
		}
		
		private void createVariables(EntityTemplateNode templateNodeMirror) {
			
			if (templateNodeMirror.getParent() != null) {
				createVariables((EntityTemplateNode)templateNodeMirror.getParent());
			}
			
			for (INode child : templateNodeMirror.getChildren()) {
				if (child instanceof EntityVariableNode) {
					EntityVariableNode variableNodeMirror = (EntityVariableNode) child;
					PrefabVariableNode prefabVariable = new PrefabVariableNode(variableNodeMirror, templateNodeMirror.getName());
					variableNode.addChild(prefabVariable);
				}
			}
		}
	}
	
	private class StringEditor extends Composite {

		private Text textField;
		
		public StringEditor(Composite parent, int style) {
			super(parent, style);
			setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
			setLayout(new GridLayout(2, false));
			
			
			Label label = new Label(this, SWT.NONE);
			label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			label.setText("Value:");
			
			textField = new Text (this, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
			textField.setEnabled(variableNode.isEditable());
			textField.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
			
			if (variableNode.getVariableValue() != null) {
				textField.setText(variableNode.getVariableValue().toString());
			}
			
			textField.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent event) {
					variableNode.setVariableValue(textField.getText());
				}
			});
			refreshDetailsContainer();
		}
	}
	
	private class EnumEditor extends Composite {

		private Combo comboBox;
		
		public EnumEditor(Composite parent, int style) {
			super(parent, style);
			setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
			setLayout(new GridLayout(2, false));
			
			EnumDescription enumDesc = (EnumDescription) variableNode.getVariableDescription();
			
			Label label = new Label(this, SWT.NONE);
			label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			label.setText("Enum Type:");
			
			label = new Label(this, SWT.NONE);
			label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			label.setText(enumDesc.getEnumType());
			
			label = new Label(this, SWT.NONE);
			label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			label.setText("Value:");
			
			String sharedDir = ModelProvider.INSTANCE.getSharedDir();
			EnumNode enumMirror = SyncEnumTree.loadEnum(enumDesc.getEnumType(), sharedDir);
			
			List<String> values = enumMirror.getEnumConstants();
			
			if (values.size() == 0) {
				MessageDialog.openConfirm(parent.getShell(), "No Enum Constants", "No enum Constants are defined in: " + enumMirror);
				return;
			}
			
			comboBox = new Combo (parent, SWT.READ_ONLY);
			comboBox.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			comboBox.setEnabled(variableNode.isEditable());
			comboBox.setItems(values.toArray(new String[values.size()]));
			
			if (variableNode.getVariableValue() == null) {
				variableNode.setVariableValue(values.get(0));
				comboBox.setText(values.get(0));
			} else {
				comboBox.setText(variableNode.getVariableValue().toString());
			}
			
			comboBox.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					variableNode.setVariableValue(comboBox.getText());
				}
			});
			refreshDetailsContainer();
		}
	}
	
	private class CharEditor extends Composite {

		private Combo comboBox;
		
		public CharEditor(Composite parent, int style) {
			super(parent, style);
			setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
			setLayout(new GridLayout(2, false));
			
			Label label = new Label(this, SWT.NONE);
			label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			label.setText("Value:");
			
			List<String> values = new ArrayList<>();
			for (int c = 32; c < 128; c++) {
				values.add(Character.toString((char) c));
			}
			
			values.set(values.indexOf(" "), "<BLANK>");
			
			comboBox = new Combo (this, SWT.READ_ONLY);
			comboBox.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			comboBox.setEnabled(variableNode.isEditable());
			comboBox.setItems(values.toArray(new String[values.size()]));
			
			if (variableNode.getVariableValue() == null) {
				variableNode.setVariableValue('a');
				comboBox.setText("a");
			} else {
				comboBox.setText(variableNode.getVariableValue().toString());
			}
			
			comboBox.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					variableNode.setVariableValue(comboBox.getText().charAt(0));
				}
			});
			refreshDetailsContainer();
		}
	}
	
	private class BooleanEditor extends Composite {

		private Button checkBox;
		
		public BooleanEditor(Composite parent, int style) {
			super(parent, style);
			setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
			setLayout(new GridLayout(2, false));
			
			Label label = new Label(this, SWT.NONE);
			label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			label.setText("Value:");
			
			checkBox = new Button (this, SWT.CHECK);
			checkBox.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
			checkBox.setEnabled(variableNode.isEditable());
			
			if (variableNode.getVariableValue() == null) {
				variableNode.setVariableValue(false);
				checkBox.setSelection(false);
			} else {
				checkBox.setSelection((boolean)variableNode.getVariableValue());
			}
			
			checkBox.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					variableNode.setVariableValue(checkBox.getSelection());
				}
			});
			refreshDetailsContainer();
		}
	}
	
	private class NumberEditor extends Composite {

		private Text textField;
		
		private NumberDescription numberDescription;
		
		public NumberEditor(Composite parent, int style) {
			super(parent, style);
			setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
			setLayout(new GridLayout(2, false));
			
			numberDescription = (NumberDescription) variableNode.getVariableDescription();
			
			Label label = new Label(this, SWT.NONE);
			label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			label.setText("Number Type:");
			
			label = new Label(this, SWT.NONE);
			label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			label.setText(numberDescription.getNumberType().toString());
			
			label = new Label(this, SWT.NONE);
			label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			label.setText("Value:");
			
			textField = new Text (this, SWT.SINGLE | SWT.BORDER);
			textField.setEnabled(variableNode.isEditable());
			textField.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			
			if (variableNode.getVariableValue() == null) {
				variableNode.setVariableValue(0);
				textField.setText("0");
			} else {
				textField.setText(variableNode.getVariableValue().toString());
			}
			
			textField.addModifyListener(new ModifyListener(){
			      public void modifyText(ModifyEvent event) {

					Object value = parseNumberVariableValue(numberDescription, textField.getText());
					variableNode.setVariableValue(value);
					
					if (value == null) {
						textField.setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
						textField.setBackground(getDisplay().getSystemColor(SWT.COLOR_DARK_RED));
					} else {
						textField.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
						textField.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
					}
				}
			});
			refreshDetailsContainer();
		}
	}
	
	private class NullPanel extends Composite {

		public NullPanel(Composite parent, int style) {
			super(parent, style);
			setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
			setLayout(new GridLayout(2, false));

			Label label = new Label(this, SWT.NONE);
			label.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
			label.setText("Is null:");

			nullCheckBox = new Button(this, SWT.CHECK);
			nullCheckBox.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
			nullCheckBox.setSelection(variableNode.getVariableValue() == null);
			nullCheckBox.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (!nullCheckBox.getSelection()) {
						updateVariableDetails();
					} else if (detailsPanel != null) {
						variableNode.setVariableValue(null);
						variableNode.clearChildren();
						detailsPanel.dispose();
					}
					refreshDetailsContainer();
					refreshViewer();
				}
			});
		}
	}
}
