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
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import micronet.tools.core.DependencyType;

public class AddDependencyUtility {
	private final static String couchbaseServiceName = "couchbase";
	//private final static String activemqServiceName = "activemq";
	
	public static void addActiveMQ() {
		addDependency(DependencyType.activemq);
	}
	
	public static void addCouchbase() {
		addDependency(DependencyType.couchbase);
	}
	
	public static void addDependency(DependencyType type) {
		if (getDependencyServiceProject(type) != null)
			return;
		addDependencyServiceProject(type);
	}
	
	public static IProject getDependencyServiceProject(DependencyType type) {
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		for (IProject project : workspaceRoot.getProjects()) {
			if (project.getName().toLowerCase().equals(type.toString()))
				return project;
		}
		return null;
	}
	
	public static IProject addDependencyServiceProject(DependencyType type) {
		try {	
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IProject project = root.getProject(type.toString());
			project.create(null);
			project.open(null);
						
			Bundle bundle = Platform.getBundle("MicroNet.Tools.Core");
			IPath resourcePath = new Path("resources").append(type.toString());
			
			String launchName = type.toString() + ".launch";
			copyFile(bundle, project, resourcePath.append(launchName), launchName);
			copyFile(bundle, project, resourcePath.append("Dockerfile"), "Dockerfile");
			
			IFolder settingsFolder = project.getFolder(".settings");
			settingsFolder.create(false, true, null);
			copyFile(bundle, project, resourcePath.append("com.github.mrharibo.micronet.preferences.prefs"), ".settings/com.github.mrharibo.micronet.preferences.prefs");
			
			return project;
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
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
			settingsFolder.create(false, true, null);
			copyFile(bundle, project, new Path("resources/couchbase/com.github.mrharibo.micronet.preferences.prefs"), ".settings/com.github.mrharibo.micronet.preferences.prefs");
			
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
	
	private static void copyFile(Bundle bundle, IProject project, IPath sourcePath, String destinationPath) {
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
}
