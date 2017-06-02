package micronet.tools.ui.serviceexplorer.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import micronet.tools.core.ServiceProject;

public class ServiceFilter extends ViewerFilter {

    private String searchString;

    public void setSearchText(String s) {
        // ensure that the value can be used for matching
        this.searchString = ".*" + s + ".*";
    }

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        if (searchString == null || searchString.length() == 0) {
            return true;
        }
        ServiceProject serviceProject = (ServiceProject) element;
        if (serviceProject.getName().matches(searchString)) {
            return true;
        }
        if (serviceProject.getName().matches(searchString)) {
            return true;
        }

        return false;
    }
}