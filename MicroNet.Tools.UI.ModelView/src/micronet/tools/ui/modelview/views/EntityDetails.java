package micronet.tools.ui.modelview.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class EntityDetails extends Composite {
	
	private Label label;
	
	Button addButton;
	Button removeButton;
	
	EntityNode selectedEntity;

	public EntityDetails(Composite parent, int style) {
		super(parent, style);
		
		setLayout(new FillLayout());
		label = new Label(this, SWT.NONE);
		
		addButton = new Button(this, SWT.PUSH);
		addButton.setText("Add Template");
		
		
		removeButton = new Button(this, SWT.PUSH);
		removeButton.setText("Remove Template");
	}
	
	public void setAddTemplateListener(SelectionListener listener) {
		addButton.addSelectionListener(listener);
	}
	
	public void setRemoveTemplateListener(SelectionListener listener) {
		removeButton.addSelectionListener(listener);
	}

	public void setEntity(EntityNode entity) {
		if (entity == null) {
			label.setText("");
			return;
		}
		label.setText(entity.getName());
		selectedEntity = entity;
		
		
		if (selectedEntity.getParent() == null)
			removeButton.setEnabled(false);
		else
			removeButton.setEnabled(true);
	}

	public EntityNode getSelectedEntity() {
		return selectedEntity;
	}
}
