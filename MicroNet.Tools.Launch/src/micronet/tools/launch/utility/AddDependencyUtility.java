package micronet.tools.launch.utility;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

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

	public static void addActiveMQ() {
		Map<String, String> files = new HashMap<>();
		files.put("Dockerfile", "Dockerfile");
		addDependency(DependencyType.activemq, files);
	}

	public static void addCouchbase() {
		Map<String, String> files = new HashMap<>();
		files.put("Dockerfile", "Dockerfile");
		files.put("init_couchbase.sh", "init_couchbase.sh");
		files.put("start_couchbase.sh", "start_couchbase.sh");
		addDependency(DependencyType.couchbase, files);
	}

	public static void addDependency(DependencyType type, Map<String, String> additionalFiles) {
		if (getDependencyServiceProject(type) != null)
			return;
		addDependencyServiceProject(type, additionalFiles);
	}

	public static IProject getDependencyServiceProject(DependencyType type) {
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		for (IProject project : workspaceRoot.getProjects()) {
			if (project.getName().toLowerCase().equals(type.toString()))
				return project;
		}
		return null;
	}

	public static IProject addDependencyServiceProject(DependencyType type, Map<String, String> files) {
		try {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IProject project = root.getProject(type.toString());
			project.create(null);
			project.open(null);

			Bundle bundle = Platform.getBundle("MicroNet.Tools.Core");
			IPath resourcePath = new Path("resources").append(type.toString());

			for (Map.Entry<String, String> file : files.entrySet()) {
				copyFile(bundle, project, resourcePath.append(file.getKey()), file.getValue());
			}

			IFolder settingsFolder = project.getFolder(".settings");
			settingsFolder.create(false, true, null);
			copyFile(bundle, project, resourcePath.append(
					"com.github.mrharibo.micronet.preferences.prefs"),
					".settings/com.github.mrharibo.micronet.preferences.prefs");

			return project;
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static void copyFile(Bundle bundle, IProject project, IPath sourcePath, String destinationPath) {
		try {
			InputStream stream = FileLocator.openStream(bundle, sourcePath, false);
			IFile launchConfigFile = project.getFile(destinationPath);
			launchConfigFile.create(stream, true, null);
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
}
