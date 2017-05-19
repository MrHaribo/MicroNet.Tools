package micronet.tools.ui.serviceexplorer.views;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;

import micronet.tools.core.ServiceProject;


public class ServiceEnablingSupport extends EditingSupport {

    private final TableViewer viewer;

    public ServiceEnablingSupport(TableViewer viewer) {
        super(viewer);
        this.viewer = viewer;
    }

    @Override
    protected CellEditor getCellEditor(Object element) {
        return new CheckboxCellEditor(null, SWT.CHECK | SWT.READ_ONLY);

    }

    @Override
    protected boolean canEdit(Object element) {
        return true;
    }

    @Override
    protected Object getValue(Object element) {
        ServiceProject service = (ServiceProject) element;
        return service.isEnabled();

    }

    @Override
    protected void setValue(Object element, Object value) {
    	ServiceProject service = (ServiceProject) element;
    	service.setEnabled((Boolean) value);
        viewer.update(element, null);
    }
}