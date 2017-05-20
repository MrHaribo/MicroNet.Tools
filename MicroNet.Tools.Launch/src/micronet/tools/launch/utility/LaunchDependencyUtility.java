package micronet.tools.launch.utility;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.osgi.framework.Bundle;

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
			ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();

			IProject couchbaseProject = AddDependencyUtility.getCouchbaseServiceProject();
			if (couchbaseProject == null)
				couchbaseProject = AddDependencyUtility.addCouchbaseServiceProject();
			
			if (!couchbaseProject.isOpen())
				couchbaseProject.open(null);

			IFile launchConfigFile = couchbaseProject.getFile("couchbase.launch");
			if (launchConfigFile.exists())
				return manager.getLaunchConfiguration(launchConfigFile);
			
			Bundle bundle = Platform.getBundle("MicroNet.Tools.Core");
			InputStream stream = FileLocator.openStream(bundle, new Path("resources/reference-couchbase.launch"), false);

			launchConfigFile.create( stream, true, null );
			stream.close();
			return manager.getLaunchConfiguration(launchConfigFile);
		} catch (CoreException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
