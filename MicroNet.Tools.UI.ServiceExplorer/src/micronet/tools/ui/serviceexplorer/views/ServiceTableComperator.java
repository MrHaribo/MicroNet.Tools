package micronet.tools.ui.serviceexplorer.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;

import micronet.tools.core.ServiceProject;

public class ServiceTableComperator extends ViewerComparator {
    private int propertyIndex;
    private static final int DESCENDING = 1;
    private int direction = DESCENDING;

    public ServiceTableComperator() {
        this.propertyIndex = 0;
        direction = DESCENDING;
    }

    public int getDirection() {
        return direction == 1 ? SWT.DOWN : SWT.UP;
    }

    public void setColumn(int column) {
        if (column == this.propertyIndex) {
            // Same column as last sort; toggle the direction
            direction = 1 - direction;
        } else {
            // New column; do an ascending sort
            this.propertyIndex = column;
            direction = DESCENDING;
        }
    }

    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {
        ServiceProject p1 = (ServiceProject) e1;
        ServiceProject p2 = (ServiceProject) e2;
        int rc = p1.getName().compareTo(p2.getName());
        
        int tmp1;
    	int tmp2;
        
        switch (propertyIndex) {
        case 0:
        	tmp1 = p1.isEnabled() ? 1 : 0;
        	tmp2 = p2.isEnabled() ? 1 : 0;
        	rc = tmp2 - tmp1;
        case 1:
        	rc = p1.getName().toLowerCase().compareTo(p2.getName().toLowerCase());
            break;
        case 2:
            rc = p1.getVersion().compareTo(p2.getVersion());
            break;
        case 3:
        	rc = p1.getNatureString().compareTo(p2.getNatureString());
            break;
        case 4:
        	tmp1 = p1.isInGamePom() ? 1 : 0;
        	tmp2 = p2.isInGamePom() ? 1 : 0;
        	rc = tmp2 - tmp1;
            break;
        case 5:
        	tmp1 = p1.isInGameCompose() ? 1 : 0;
        	tmp2 = p2.isInGameCompose() ? 1 : 0;
        	rc = tmp2 - tmp1;
            break;
        case 6:
        	rc = p1.getNetwork().compareTo(p2.getNetwork());
            break;
        case 7:
        	rc = p1.getPortsRaw().compareTo(p2.getPortsRaw());
            break;
        default:
            rc = 0;
        }

        // If descending order, flip the direction
        if (direction == DESCENDING) {
            rc = -rc;
        }
        return rc;
    }

}
