package micronet.tools.ui.modelview.views;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import org.eclipse.jface.action.Action;
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
import micronet.tools.filesync.SyncEnumTree;
import micronet.tools.filesync.SyncTemplateTree;
import micronet.tools.model.ModelConstants;
import micronet.tools.model.nodes.EnumNode;
import micronet.tools.ui.modelview.actions.EnumRemoveAction;

public class EnumNodeDetails extends NodeDetails {

	private EnumRemoveAction removeEnumAction;
	
	private String editString = "Edit";
	private String saveString = "Save";
	private Text textField;
	
	private EnumNode enumNode;
	
	public EnumNodeDetails(EnumNode enumNode, Composite parent, int style) {
		super(enumNode, parent, style, true);
		
		this.enumNode = enumNode;
		
		removeEnumAction = new EnumRemoveAction(getShell(), enumNode);
		
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
		
		StringJoiner joiner = new StringJoiner(",\n");
		for (String enumConstant : enumNode.getEnumConstants())
			joiner.add(enumConstant);
		textField.setText(joiner.toString());
		
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
		
		String sharedDir = ModelProvider.INSTANCE.getSharedDir();
		Map<String, Set<String>> enumUsage = SyncTemplateTree.getEnumUsage(sharedDir);
		
		if (enumUsage.containsKey(enumNode.getName())) {
			label = new Label(this, SWT.NONE);
			label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
			label.setText("Used by Templates:");

			String usage = String.join(", ", enumUsage.get(enumNode.getName()));	
			
			label = new Label(this, SWT.NONE);
			label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
			label.setText(usage);
		}
	}
	
	@Override
	protected void removeNode() {
		removeEnumAction.run();
	}
	
	@Override
	public void setRefreshViewerAction(Action refreshViewerAction) {
		removeEnumAction.setRefreshViewerAction(refreshViewerAction, true);
	}
	
	private void saveEnumNode() {
		String text = textField.getText().replaceAll("\\s+","");
		
		List<String> enumTokens = Arrays.asList(text.split(","));
		enumTokens.remove("");
		
		for (String enumToken : enumTokens) {
			if (!ModelConstants.isValidJavaIdentifier(enumToken))
				MessageDialog.openInformation(textField.getShell(), "Invalid identifier", enumToken + "is not a valid enum constant identifier.");
		}
		
		enumNode.setEnumConstants(enumTokens);
		
		String sharedDir = ModelProvider.INSTANCE.getSharedDir();
		SyncEnumTree.saveEnumNode(enumNode, sharedDir);
	}
}
