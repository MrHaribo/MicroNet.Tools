package micronet.tools.ui.modelview.views;

import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import micronet.tools.ui.modelview.NumberType;
import micronet.tools.ui.modelview.VariableType;

public class VariableNodeDetails extends NodeDetails {
	
	Label label;
	
	Composite detailsPanel;

	private Combo typeSelect;

	private Composite detailsContainer;
	
	public VariableNodeDetails(Composite parent, int style) {
		super(parent, style);
		
		detailsContainer = new Composite(this, SWT.NONE);
		detailsContainer.setLayout(new GridLayout(2, false));

		label = new Label(detailsContainer, SWT.NONE);
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
			detailsPanel = new NumberVariableDetails(detailsContainer, SWT.NONE);
			break;
		case BOOLEAN:
		case CHAR:
		case COMPONENT:
		case ENUM:
		case LIST:
		case MAP:
		case REF:
		case SET:
		case STRING:
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
	
	private class NumberVariableDetails extends Composite {

		public NumberVariableDetails(Composite parent, int style) {
			super(parent, style);
			
			setLayout(new GridLayout(2, false));
			
			label = new Label(this, SWT.NONE);
			label.setText("Number Type:");
			
			String[] numberTypes = Arrays.stream(NumberType.class.getEnumConstants()).map(Enum::name).toArray(String[]::new);
			
			Combo numberTypeSelect = new Combo(this, SWT.READ_ONLY);
			numberTypeSelect.setItems(numberTypes);
			numberTypeSelect.setText("INT");
		}
		
	}
}
