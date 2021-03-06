package micronet.tools.ui.modelview.views;

import java.util.Map;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import micronet.tools.core.ModelProvider;
import micronet.tools.filesync.SyncTemplateTree;
import micronet.tools.model.nodes.EntityTemplateNode;
import micronet.tools.ui.modelview.actions.TemplateConstCreateAction;
import micronet.tools.ui.modelview.actions.TemplateCreateAction;
import micronet.tools.ui.modelview.actions.TemplateRemoveAction;
import micronet.tools.ui.modelview.actions.TemplateVariableCreateAction;

public class TemplateNodeDetails extends NodeDetails {

	private TemplateCreateAction addChildTemplateAction;
	private TemplateRemoveAction removeChildTemplateAction;
	private TemplateVariableCreateAction addChildVariableAction;
	private TemplateConstCreateAction addChildConstAction;
	
	public TemplateNodeDetails(EntityTemplateNode templateNode, Composite parent, int style) {
		super(templateNode, parent, style, true);
		
		removeChildTemplateAction = new TemplateRemoveAction(getShell(), templateNode);
		addChildTemplateAction = new TemplateCreateAction(getShell(), templateNode); 
		addChildVariableAction = new TemplateVariableCreateAction(getShell(), templateNode);
		addChildConstAction = new TemplateConstCreateAction(getShell(), templateNode);

		Composite detailsContainer = new Composite(this, SWT.NONE);
		detailsContainer.setLayout(new GridLayout(2, false));
		
		Button button = new Button(detailsContainer, SWT.PUSH);
		button.setText("Add Variable");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				addChildVariableAction.run();
			}
		});
		
		button = new Button(detailsContainer, SWT.PUSH);
		button.setText("Add Constant");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				addChildConstAction.run();
			}
		});
		
		button = new Button(detailsContainer, SWT.PUSH);
		button.setText("Add Derived Template");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				addChildTemplateAction.run();
			}
		});
		
		String sharedDir = ModelProvider.INSTANCE.getSharedDir();
		Map<String, Set<String>> templateUsage = SyncTemplateTree.getTemplateUsage(sharedDir);
		
		if (templateUsage.containsKey(templateNode.getName())) {
			Label label = new Label(this, SWT.NONE);
			label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
			label.setText("Used by Templates:");

			String usage = String.join(", ", templateUsage.get(templateNode.getName()));	
			
			label = new Label(this, SWT.NONE);
			label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
			label.setText(usage);
		}
	}

	@Override
	public void setRefreshViewerAction(Action refreshViewerAction) {
		addChildTemplateAction.setRefreshViewerAction(refreshViewerAction, false);
		removeChildTemplateAction.setRefreshViewerAction(refreshViewerAction, true);
		addChildVariableAction.setRefreshViewerAction(refreshViewerAction, true);
		addChildConstAction.setRefreshViewerAction(refreshViewerAction, true);
	}
	
	@Override
	protected void removeNode() {
		removeChildTemplateAction.run();
	}
}
