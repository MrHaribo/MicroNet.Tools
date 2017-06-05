package micronet.tools.ui.modelview.views;

import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import micronet.tools.core.ModelProvider;
import micronet.tools.ui.modelview.NumberType;
import micronet.tools.ui.modelview.SyncModelTree;
import micronet.tools.ui.modelview.VariableType;

public class VariableNodeDetails extends NodeDetails {
	
	private static String[] primitiveTypes = {
		VariableType.CHAR.toString(),
		VariableType.ENUM.toString(),
		VariableType.STRING.toString(),
		NumberType.BYTE.toString(),
		NumberType.SHORT.toString(),
		NumberType.INT.toString(),
		NumberType.LONG.toString(),
		NumberType.FLOAT.toString(),
		NumberType.DOUBLE.toString(),
	};
	
	private static String[] listTypes = {
		VariableType.REF.toString(),
		VariableType.BOOLEAN.toString(),
		VariableType.CHAR.toString(),
		VariableType.ENUM.toString(),
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
	
	
	
	public VariableNodeDetails(Composite parent, int style) {
		super(parent, style);
		
		detailsContainer = new Composite(this, SWT.NONE);
		detailsContainer.setLayout(new GridLayout(2, false));

		Label label = new Label(detailsContainer, SWT.NONE);
		label.setText("Variable Type:");
		
		String[] baseTypes = Arrays.stream(VariableType.class.getEnumConstants()).map(Enum::name).toArray(String[]::new);
		
		typeSelect = new Combo(detailsContainer, SWT.READ_ONLY);
		typeSelect.setItems(baseTypes);
		typeSelect.setText("STRING");
		typeSelect.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateVariableDetails();
			}
		});
		//updateVariableDetails();
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
		case REF:
		case STRING:
		case BOOLEAN:
		case CHAR:
		case COMPONENT:
		case ENUM:
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
	}
	
	private class MapDetails extends Composite {

		private Combo keyTypeSelect;
		private Combo entryTypeSelect;
		
		private Button primitiveRadio;
		private Button templateRadio;

		public MapDetails(Composite parent, int style) {
			super(parent, style);
			
			setLayout(new GridLayout(3, false));
			
			Label label = new Label(this, SWT.NONE);
			label.setText("Key Type:");

			keyTypeSelect = new Combo(this, SWT.READ_ONLY);
			keyTypeSelect.setItems(primitiveTypes);
			keyTypeSelect.setText(NumberType.INT.toString());
			keyTypeSelect.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
			
			label = new Label(this, SWT.NONE);
			label.setText("Map Enties:");
			
			primitiveRadio = new Button (this, SWT.RADIO);
			primitiveRadio.setText("Primitive");
			primitiveRadio.setSelection(true);
			primitiveRadio.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					entryTypeSelect.setItems(listTypes);
				}
			});
			
			templateRadio = new Button (this, SWT.RADIO);
			templateRadio.setText("Template");
			templateRadio.setSelection(false);
			templateRadio.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					String sharedDir = ModelProvider.INSTANCE.getSharedDir();
					List<String> templateNames = SyncModelTree.getAllTemplateNames(sharedDir);
					entryTypeSelect.setItems(templateNames.toArray(new String[templateNames.size()]));
				}
			});
			
			label = new Label(this, SWT.NONE);
			label.setText("Map Entry Type:");

			entryTypeSelect = new Combo(this, SWT.READ_ONLY);
			entryTypeSelect.setItems(listTypes);
			entryTypeSelect.setText(NumberType.INT.toString());
			entryTypeSelect.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 2, 1));
		}
	}
	
	private class SetDetails extends Composite {

		private Combo setTypeSelect;

		public SetDetails(Composite parent, int style) {
			super(parent, style);
			
			setLayout(new GridLayout(2, false));
			
			Label label = new Label(this, SWT.NONE);
			label.setText("Set Entry Type:");

			setTypeSelect = new Combo(this, SWT.READ_ONLY);
			setTypeSelect.setItems(primitiveTypes);
			setTypeSelect.setText(NumberType.INT.toString());
			setTypeSelect.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		}
	}
	
	private class ListDetails extends Composite {

		private Button primitiveRadio;
		private Button templateRadio;
		private Combo listTypeSelect;

		public ListDetails(Composite parent, int style) {
			super(parent, style);
			
			setLayout(new GridLayout(3, false));
			
			Label label = new Label(this, SWT.NONE);
			label.setText("List Enties:");
			
			primitiveRadio = new Button (this, SWT.RADIO);
			primitiveRadio.setText("Primitive");
			primitiveRadio.setSelection(true);
			primitiveRadio.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					listTypeSelect.setItems(listTypes);
				}
			});
			
			templateRadio = new Button (this, SWT.RADIO);
			templateRadio.setText("Template");
			templateRadio.setSelection(false);
			templateRadio.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					String sharedDir = ModelProvider.INSTANCE.getSharedDir();
					List<String> templateNames = SyncModelTree.getAllTemplateNames(sharedDir);
					listTypeSelect.setItems(templateNames.toArray(new String[templateNames.size()]));
				}
			});
			
			label = new Label(this, SWT.NONE);
			label.setText("List Entry Type:");

			listTypeSelect = new Combo(this, SWT.READ_ONLY);
			listTypeSelect.setItems(listTypes);
			listTypeSelect.setText(NumberType.INT.toString());
			listTypeSelect.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 2, 1));
		}
	}
	
	private class NumberDetails extends Composite {

		public NumberDetails(Composite parent, int style) {
			super(parent, style);
			
			setLayout(new GridLayout(2, false));
			
			Label label = new Label(this, SWT.NONE);
			label.setText("Number Type:");
			
			String[] numberTypes = Arrays.stream(NumberType.class.getEnumConstants()).map(Enum::name).toArray(String[]::new);
			
			Combo numberTypeSelect = new Combo(this, SWT.READ_ONLY);
			numberTypeSelect.setItems(numberTypes);
			numberTypeSelect.setText(NumberType.INT.toString());
		}
		
	}
}
