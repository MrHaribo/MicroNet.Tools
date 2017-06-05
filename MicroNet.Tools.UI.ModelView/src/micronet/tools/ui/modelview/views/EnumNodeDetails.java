package micronet.tools.ui.modelview.views;

import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import micronet.tools.core.ModelProvider;
import micronet.tools.ui.modelview.INode;
import micronet.tools.ui.modelview.ModelConstants;
import micronet.tools.ui.modelview.SyncEnumTree;
import micronet.tools.ui.modelview.nodes.EnumNode;

public class EnumNodeDetails extends NodeDetails {
	
	private String editString = "Edit";
	private String saveString = "Save";
	private Text textField;
	
	public EnumNodeDetails(Composite parent, int style) {
		super(parent, style);
		
		Composite detailsContainer = new Composite(this, SWT.NONE);
		detailsContainer.setLayout(new GridLayout(2, false));
		detailsContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Label label = new Label(detailsContainer, SWT.NONE);
		label.setText("Enum Constants (CVS):");
		label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		
		Button editButton = new Button(detailsContainer, SWT.PUSH);
		editButton.setText(editString);
		editButton.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
		
		textField = new Text (detailsContainer, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 2;
		textField.setLayoutData(gridData);
		textField.setEditable(false);
		
		editButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				if (editButton.getText().equals(editString)) {
					editButton.setText(saveString);
					textField.setEditable(true);
				} else {
					editButton.setText(editString);
					textField.setEditable(false);
					saveEnumNode();
				}
			}
		});
	}
	
	@Override
	public void setNode(INode templateNode) {
		super.setNode(templateNode);
		
		EnumNode enumNode = (EnumNode)templateNode;
		
		StringJoiner joiner = new StringJoiner(",\n");
		for (String enumConstant : enumNode.getEnumConstants())
			joiner.add(enumConstant);
		textField.setText(joiner.toString());
	}
	
	private void saveEnumNode() {
		String text = textField.getText().replaceAll("\\s+","");
		
		List<String> enumTokens = Arrays.asList(text.split(","));
		enumTokens.remove("");
		
		for (String enumToken : enumTokens) {
			if (!ModelConstants.isValidJavaIdentifier(enumToken))
				MessageDialog.openInformation(textField.getShell(), "Invalid identifier", enumToken + "is not a valid enum constant identifier.");
		}
		
		EnumNode enumNode = (EnumNode)node;
		enumNode.setEnumConstants(enumTokens);
		
		String sharedDir = ModelProvider.INSTANCE.getSharedDir();
		SyncEnumTree.saveEnumNode((EnumNode)node, sharedDir);
	}
}
