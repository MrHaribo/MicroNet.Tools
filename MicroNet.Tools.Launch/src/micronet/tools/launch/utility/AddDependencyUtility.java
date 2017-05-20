package micronet.tools.launch.utility;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

public class AddDependencyUtility {
	private final static String couchbaseServiceName = "couchbase";
	
	public static IProject addCouchbaseServiceProject() {
		try {	
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IProject project = root.getProject(couchbaseServiceName);
			project.create(null);
			project.open(null);
			return project;
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static IProject getCouchbaseServiceProject() {
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		for (IProject project : workspaceRoot.getProjects()) {
			if (project.getName().toLowerCase().equals(couchbaseServiceName))
				return project;
		}
		return null;
	}
	
	
}
