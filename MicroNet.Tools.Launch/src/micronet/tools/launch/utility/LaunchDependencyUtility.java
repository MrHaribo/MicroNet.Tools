package micronet.tools.launch.utility;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;

import micronet.tools.core.ServiceProject;
import micronet.tools.core.ServiceProject.Nature;

public final class LaunchDependencyUtility {
	private LaunchDependencyUtility() {
	}
	
	public static void launchNative(ServiceProject project, String mode) {
		if (!project.hasNature(Nature.DEPENDENCY))
			return;
		//TODO: not yet implemented
	}
	
	public static void launchCouchbase() {
		ILaunchConfiguration launchConfig = getCouchbaseLaunchConfiguration();
		DebugUITools.launch(launchConfig, "run");
	}

	public static ILaunchConfiguration getCouchbaseLaunchConfiguration() {
		try {

			IProject couchbaseProject = AddDependencyUtility.getCouchbaseServiceProject();
			if (couchbaseProject == null)
				couchbaseProject = AddDependencyUtility.addCouchbaseServiceProject();
			
			if (!couchbaseProject.isOpen())
				couchbaseProject.open(null);

			ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
			IFile launchConfigFile = couchbaseProject.getFile("couchbase.launch");
			return manager.getLaunchConfiguration(launchConfigFile);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}
}
