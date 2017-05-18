package micronet.tools.launch.utility;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;

public final class BuildServiceContainerUtility {

	private BuildServiceContainerUtility() {
	}
	
	public static void buildContainer(IProject project, String mode) {
		String containerBuildName = getBuildContainerName(project);
		System.out.println("Building: " + containerBuildName);
		
		ILaunch launch = LaunchUtility.getLaunch(containerBuildName);
		if (launch != null) {
			try {
				System.out.println("Launch is already there");
				launch.terminate();
				ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
				manager.removeLaunch(launch);
			} catch (DebugException e) {
				e.printStackTrace();
			}
		}
		
		ILaunchConfiguration buildConfig = getContainerBuildConfig(project, mode);
		DebugUITools.launch(buildConfig, mode);
	}
	
	private static ILaunchConfiguration getContainerBuildConfig(IProject project, String mode) {
		String containerBuildName = getBuildContainerName(project);
		
		try {
			ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
			ILaunchConfigurationType type = manager.getLaunchConfigurationType("org.eclipse.linuxtools.docker.ui.buildDockerImageLaunchConfigurationType");

			ILaunchConfigurationWorkingCopy workingCopy = type.newInstance(null, containerBuildName);
			workingCopy.setAttribute("dockerConnection", "http://127.0.0.1:2375");
			workingCopy.setAttribute("repoName", project.getName().toLowerCase());
			workingCopy.setAttribute("sourcePathLocation", "/" + project.getName());
			workingCopy.setAttribute("sourcePathWorkspaceRelativeLocation", true);
			
			ILaunchConfiguration config = workingCopy.doSave();
			return config;
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String getBuildContainerName(IProject project) {
		return project.getName() + "ContainerBuild";
	}
}
