package micronet.tools.ui.modelview.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import micronet.tools.core.ModelProvider;
import micronet.tools.ui.modelview.INode;
import micronet.tools.ui.modelview.ModelConstants;
import micronet.tools.ui.modelview.SyncEnumTree;
import micronet.tools.ui.modelview.SyncTemplateTree;
import micronet.tools.ui.modelview.nodes.EntityTemplateNode;
import micronet.tools.ui.modelview.nodes.EntityVariableNode;
import micronet.tools.ui.modelview.nodes.EnumNode;
import micronet.tools.ui.modelview.nodes.PrefabVariableNode;
import micronet.tools.ui.modelview.variables.CollectionDescription;
import micronet.tools.ui.modelview.variables.ComponentDescription;
import micronet.tools.ui.modelview.variables.EnumDescription;
import micronet.tools.ui.modelview.variables.NumberDescription;
import micronet.tools.ui.modelview.variables.NumberType;
import micronet.tools.ui.modelview.variables.VariableDescription;
import micronet.tools.ui.modelview.variables.VariableType;

public class PrefabVariableNodeDetails extends Composite {

	private PrefabVariableNode variableNode;
	
	private Composite detailsPanel = null;
	
	private Button nullCheckBox;
	
	private Action refreshPrefabTree;
	
	public PrefabVariableNodeDetails(PrefabVariableNode variableNode, Composite parent, int style) {
		super(parent, style);
		
		this.variableNode = variableNode;
		
		setLayout(new GridLayout(2, false));
		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Label nameLabel = new Label(this, SWT.NONE);
		nameLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
		nameLabel.setText(variableNode.getName());
		
		FontDescriptor descriptor = FontDescriptor.createFrom(nameLabel.getFont());
		descriptor = descriptor.setStyle(SWT.BOLD);
		nameLabel.setFont(descriptor.createFont(nameLabel.getDisplay()));
		
		Label label = new Label(this, SWT.NONE);
		label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		label.setText("Variable Type: ");
		
		label = new Label(this, SWT.NONE);
		label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		label.setText(variableNode.getVariableType().toString());
		
		if (!variableNode.getContributingTemplate().equals("")) {
			label = new Label(this, SWT.NONE);
			label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			label.setText("Contributing template: ");
			
			label = new Label(this, SWT.NONE);
			label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			label.setText(variableNode.getContributingTemplate());
		}
		
		new NullPanel(this, SWT.NONE);
		
		if (variableNode.getVariableValue() != null)
			updateVariableDetails();
	}

	private void updateVariableDetails() {

		
		switch (variableNode.getVariableType()) {
		case BOOLEAN:
			detailsPanel = new BooleanEditor(this, SWT.NONE);
			break;
		case CHAR:
			detailsPanel = new CharEditor(this, SWT.NONE);
			break;
		case ENUM:
			detailsPanel = new EnumEditor(this, SWT.NONE);
			break;
		case NUMBER:
			detailsPanel = new NumberEditor(this, SWT.NONE);
			break;
		case STRING:
			detailsPanel = new StringEditor(this, SWT.NONE);
			break;
		case COMPONENT:
			detailsPanel = new ComponentEditor(this, SWT.NONE);
			break;
		case LIST:
			detailsPanel = new ListEditor(this, SWT.NONE);
			break;
		case SET:
			break;
		case MAP:
			break;
		default:
			break;
			
		}
	}

	public void setOnPrefabTreeChanged(Action prefabTreeChanged) {
		this.refreshPrefabTree = prefabTreeChanged;
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
					parent.layout();
					refreshPrefabTree.run();
				}
			});
		}
	}
	
	private class ListEditor extends Composite {

		private VariableDescription entryDesc = null;

		public ListEditor(Composite parent, int style) {
			super(parent, style);
			setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
			setLayout(new GridLayout(2, false));
			
			variableNode.setVariableValue(new Object());
			
			CollectionDescription listDescription = (CollectionDescription) variableNode.getVariableDescription();
			
			entryDesc = ModelConstants.getEntryDescription(listDescription);
			
			Button button = new Button(this, SWT.NONE);
			button.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 2, 1));
			button.setText("Add Entry");
			button.addSelectionListener(new SelectionAdapter() {
				
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					PrefabVariableNode prefabVariable = new PrefabVariableNode("entry", entryDesc);
					variableNode.addChild(prefabVariable);
					refreshPrefabTree.run();
				}
			});
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
			textField.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
			
			if (variableNode.getVariableValue() != null) {
				textField.setText(variableNode.getVariableValue().toString());
			}
			
			textField.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent event) {
					variableNode.setVariableValue(textField.getText());
				}
			});
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
				this.dispose();
				nullCheckBox.setEnabled(false);
				return;
			}
			
			comboBox = new Combo (parent, SWT.READ_ONLY);
			comboBox.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
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
			textField.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			
			if (variableNode.getVariableValue() == null) {
				variableNode.setVariableValue(0);
				textField.setText("0");
			} else {
				textField.setText(variableNode.getVariableValue().toString());
			}
			
			textField.addModifyListener(new ModifyListener(){
			      public void modifyText(ModifyEvent event) {

					Object value = null;

					try {

						switch (numberDescription.getNumberType()) {
						case BYTE:
							value = Byte.parseByte(textField.getText());
							break;
						case DOUBLE:
							value = Double.parseDouble(textField.getText());
							break;
						case FLOAT:
							value = Float.parseFloat(textField.getText());
							break;
						case INT:
							value = Integer.parseInt(textField.getText());
							break;
						case LONG:
							value = Long.parseLong(textField.getText());
							break;
						case SHORT:
							value = Short.parseShort(textField.getText());
							break;
						default:
							value = null;
						}
						
						textField.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
						textField.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));

					} catch (NumberFormatException ex) {
						textField.setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
						textField.setBackground(getDisplay().getSystemColor(SWT.COLOR_DARK_RED));
						variableNode.setVariableValue(null);
					}
					
					variableNode.setVariableValue(value);
				}
			});
		}
	}
}
