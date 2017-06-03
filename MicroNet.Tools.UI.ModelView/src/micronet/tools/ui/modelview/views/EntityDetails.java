package micronet.tools.ui.modelview.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import micronet.tools.ui.modelview.EntityNode;

public class EntityDetails extends Composite {
	
	private Label label;
	
	Button addChildTemplateButton;
	Button addChildVariableButton;
	Button removeNodeButton;
	
	EntityNode selectedEntity;

	public EntityDetails(Composite parent, int style) {
		super(parent, style);
		
		setLayout(new FillLayout());
		label = new Label(this, SWT.NONE);
		
		addChildTemplateButton = new Button(this, SWT.PUSH);
		addChildTemplateButton.setText("Add Child Template");
		
		addChildVariableButton = new Button(this, SWT.PUSH);
		addChildVariableButton.setText("Add Variable");
		
		removeNodeButton = new Button(this, SWT.PUSH);
		removeNodeButton.setText("Remove");
	}
	
	public void setAddTemplateListener(SelectionListener listener) {
		addChildTemplateButton.addSelectionListener(listener);
	}
	
	public void setAddVariableListener(SelectionListener listener) {
		addChildVariableButton.addSelectionListener(listener);
	}
	
	public void setRemoveNodeListener(SelectionListener listener) {
		removeNodeButton.addSelectionListener(listener);
	}

	public void setEntity(EntityNode entity) {
		if (entity == null) {
			label.setText("");
			return;
		}
		label.setText(entity.getName());
		selectedEntity = entity;
		
		
		if (selectedEntity.getParent() == null)
			removeNodeButton.setEnabled(false);
		else
			removeNodeButton.setEnabled(true);
	}

	public EntityNode getSelectedEntity() {
		return selectedEntity;
	}
}
