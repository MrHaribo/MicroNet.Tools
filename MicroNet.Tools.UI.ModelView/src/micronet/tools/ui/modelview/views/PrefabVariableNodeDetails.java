package micronet.tools.ui.modelview.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import micronet.tools.core.ModelProvider;
import micronet.tools.ui.modelview.SyncEnumTree;
import micronet.tools.ui.modelview.nodes.EnumNode;
import micronet.tools.ui.modelview.nodes.PrefabVariableNode;
import micronet.tools.ui.modelview.variables.EnumDescription;

public class PrefabVariableNodeDetails extends Composite {

	private PrefabVariableNode variableNode;
	
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
		
		label = new Label(this, SWT.NONE);
		label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		label.setText("Contributing template: ");
		
		label = new Label(this, SWT.NONE);
		label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		label.setText(variableNode.getContributingTemplate());
		
		switch (variableNode.getVariableType()) {
		case BOOLEAN:
			new BooleanEditor(this, SWT.NONE);
			break;
		case CHAR:
			new CharEditor(this, SWT.NONE);
			break;
		case COMPONENT:
			break;
		case ENUM:
			new EnumEditor(this, SWT.NONE);
			break;
		case LIST:
			break;
		case MAP:
			break;
		case NUMBER:
			new NumberEditor(this, SWT.NONE);
			break;
		case REF:
			break;
		case SET:
			break;
		case STRING:
			break;
		default:
			break;
			
		}
	}
	
	private class EnumEditor {

		private Combo comboBox;
		
		public EnumEditor(Composite parent, int style) {
			
			EnumDescription enumDesc = (EnumDescription) variableNode.getVariableDescription();
			
			Label label = new Label(parent, SWT.NONE);
			label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			label.setText("Enum Type:");
			
			label = new Label(parent, SWT.NONE);
			label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			label.setText(enumDesc.getEnumType());
			
			label = new Label(parent, SWT.NONE);
			label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			label.setText("Value:");
			
			String sharedDir = ModelProvider.INSTANCE.getSharedDir();
			EnumNode enumMirror = SyncEnumTree.loadEnum(enumDesc.getEnumType(), sharedDir);
			
			List<String> values = enumMirror.getEnumConstants();
			
			comboBox = new Combo (parent, SWT.READ_ONLY);
			comboBox.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			comboBox.setItems(values.toArray(new String[values.size()]));
		}
	}
	
	private class CharEditor {

		private Combo comboBox;
		
		public CharEditor(Composite parent, int style) {
			
			Label label = new Label(parent, SWT.NONE);
			label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			label.setText("Value:");
			
			List<String> values = new ArrayList<>();
			for (int c = 32; c < 128; c++) {
				values.add(Character.toString((char) c));
			}
			
			values.set(values.indexOf(" "), "<BLANK>");
			
			comboBox = new Combo (parent, SWT.READ_ONLY);
			comboBox.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			comboBox.setItems(values.toArray(new String[values.size()]));
			comboBox.setText("a");
		}
	}
	
	private class BooleanEditor {

		private Button checkBox;
		
		public BooleanEditor(Composite parent, int style) {
			
			Label label = new Label(parent, SWT.NONE);
			label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			label.setText("Value:");
			
			checkBox = new Button (parent, SWT.CHECK);
			checkBox.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		}
	}
	
	private class NumberEditor {

		private Text textField;
		
		public NumberEditor(Composite parent, int style) {
			
			Label label = new Label(parent, SWT.NONE);
			label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			label.setText("Value:");
			
			textField = new Text (parent, SWT.SINGLE | SWT.BORDER);
			textField.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		}
	}
}
