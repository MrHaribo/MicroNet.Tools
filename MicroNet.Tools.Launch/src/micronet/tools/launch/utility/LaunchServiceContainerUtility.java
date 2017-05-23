package micronet.tools.launch.utility;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

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

import micronet.tools.core.ServiceProject;
import micronet.tools.core.ServiceProject.Nature;

public final class LaunchServiceContainerUtility {

	private LaunchServiceContainerUtility() {
	}
	
	public static void launchContainer(ServiceProject serviceProject, String mode) {
		
		if (!serviceProject.hasNature(Nature.DOCKER))
			return;
		
		IProject project = serviceProject.getProject();
		System.out.println("Launching: " + serviceProject.getName());
		
		ILaunch launch = LaunchUtility.getLaunch(serviceProject.getName());
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
		
		ILaunchConfiguration launchConfig = getContainerLaunchConfig(project, mode);
		DebugUITools.launch(launchConfig, mode);
	}
	
	private static ILaunchConfiguration getContainerLaunchConfig(IProject project, String mode) {
		String containerBuildName = getBuildContainerName(project);
		
		try {
			ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
			ILaunchConfigurationType type = manager.getLaunchConfigurationType("org.eclipse.linuxtools.docker.ui.runDockerImageLaunchConfigurationType");

			//TODO: This is not working (most likely because of missing image id)
			ILaunchConfigurationWorkingCopy workingCopy = type.newInstance(null, containerBuildName);
			workingCopy.setAttribute("allocatePseudoTTY", true);
			workingCopy.setAttribute("autoRemove", true);
			workingCopy.setAttribute("connectionName", "http://127.0.0.1:2375");
			workingCopy.setAttribute("creationDate", new Date().toString());
			workingCopy.setAttribute("entryPoint", "");
			workingCopy.setAttribute("envVariables", new ArrayList<String>());
			workingCopy.setAttribute("imageName", project.getName().toLowerCase() + ":latest");
			workingCopy.setAttribute("interactive", false);
			workingCopy.setAttribute("labels", new HashMap<String, String>());
			workingCopy.setAttribute("links", new ArrayList<String>());
			workingCopy.setAttribute("privileged", false);
			workingCopy.setAttribute("publishAllPorts", true);
			workingCopy.setAttribute("publishedPorts", new ArrayList<String>());
			workingCopy.setAttribute("volumes", new ArrayList<String>());
			workingCopy.setAttribute("command", "");
			workingCopy.setAttribute("imageId", "");
			ILaunchConfiguration config = workingCopy.doSave();
			return config;
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/*
	 
	<stringAttribute key="command" value="/bin/sh -c './wait-for-it.sh activemq:61616 -- java -cp ./target/classes:./target/lib/* ServiceImpl '"/>
	
	<stringAttribute key="imageId" value="sha256:b2fb76a2d55d3f90cf7d015606104aab4a9467990aa5bbee509834e86faae61a"/>
	
	 */

	private static String getBuildContainerName(IProject project) {
		return project.getName() + "ContainerBuild";
	}
}
