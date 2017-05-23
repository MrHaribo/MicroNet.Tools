package micronet.tools.launch.utility;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;

import micronet.tools.core.DependencyType;

public final class LaunchDependencyUtility {
	private LaunchDependencyUtility() {
	}
	
	public static void launchActiveMQ() {
		launchDependeny(DependencyType.activemq);
	}
	
	public static void launchCouchbase() {
		launchDependeny(DependencyType.couchbase);
	}
	
	public static void launchDependeny(DependencyType type) {
		ILaunchConfiguration launchConfig = getDependencyLaunchConfiguration(type);
		DebugUITools.launch(launchConfig, "run");
	}

	public static ILaunchConfiguration getDependencyLaunchConfiguration(DependencyType type) {
		try {

			IProject project = AddDependencyUtility.getDependencyServiceProject(type);
			if (project == null)
				project = AddDependencyUtility.addDependencyServiceProject(type);
			
			if (!project.isOpen())
				project.open(null);

			IFile launchConfigFile = project.getFile(type.toString() + ".launch");
			ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
			return manager.getLaunchConfiguration(launchConfigFile);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static ILaunchConfiguration getCouchbaseLaunchConfiguration() {
		try {

			IProject project = AddDependencyUtility.getCouchbaseServiceProject();
			if (project == null)
				project = AddDependencyUtility.addCouchbaseServiceProject();
			
			if (!project.isOpen())
				project.open(null);

			ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
			IFile launchConfigFile = project.getFile("couchbase.launch");
			return manager.getLaunchConfiguration(launchConfigFile);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}
}
