package micronet.tools.ui.modelview.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import micronet.tools.core.ModelProvider;
import micronet.tools.filesync.SyncTemplateTree;
import micronet.tools.model.nodes.EntityTemplateNode;
import micronet.tools.model.nodes.EntityVariableNode;
import micronet.tools.model.nodes.PrefabVariableNode;

public class TemplateVariableConstNodeDetails extends TemplateVariableNodeDetails {

	EntityVariableNode variableNode;
	
	public TemplateVariableConstNodeDetails(EntityVariableNode variableNode, Composite parent, int style) {
		super(variableNode, parent, style);
		
		this.variableNode = variableNode;
		
		Button button = new Button (this, SWT.NONE);
		button.setText("Save Constant");
		button.setLayoutData(new GridData(SWT.LEFT, SWT.TOP));
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String sharedDir = ModelProvider.INSTANCE.getSharedDir();
				SyncTemplateTree.saveTemplateTree((EntityTemplateNode) variableNode.getParent(), sharedDir);
			}
		});
	}

	@Override
	protected void variableDetailsChanged() {
		variableNode.removeChild(variableNode.getChildren().get(0));
		PrefabVariableNode prefabNode = new PrefabVariableNode(variableNode.getName(), variableNode.getVariabelDescription());
		variableNode.addChild(prefabNode);

		super.refreshViewer();
	}
	
}
