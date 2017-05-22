package micronet.tools.launch.utility;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

public class AddDependencyUtility {
	private final static String couchbaseServiceName = "couchbase";
	
	
	public static void addCouchbase() {
		if (getCouchbaseServiceProject() != null)
			return;
		addCouchbaseServiceProject();
	}
	
	public static IProject addCouchbaseServiceProject() {
		try {	
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IProject project = root.getProject(couchbaseServiceName);
			project.create(null);
			project.open(null);
			
			
						
			Bundle bundle = Platform.getBundle("MicroNet.Tools.Core");
			
			copyFile(bundle, project, new Path("resources/couchbase/reference-couchbase.launch"), "couchbase.launch");
			copyFile(bundle, project, new Path("resources/couchbase/reference-Dockerfile"), "Dockerfile");
			
			IFolder settingsFolder = project.getFolder(".settings");
			settingsFolder.create(true, true, null);
			copyFile(bundle, project, new Path("resources/couchbase/.settings/com.github.mrharibo.micronet.preferences.prefs"), ".settings/com.github.mrharibo.micronet.preferences.prefs");
		
			
			return project;
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static void copyFile(Bundle bundle, IProject project, Path sourcePath, String destinationPath) {
		try {
			InputStream stream = FileLocator.openStream(bundle, sourcePath, false);
			IFile launchConfigFile = project.getFile(destinationPath);
			launchConfigFile.create( stream, true, null );
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		}
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
