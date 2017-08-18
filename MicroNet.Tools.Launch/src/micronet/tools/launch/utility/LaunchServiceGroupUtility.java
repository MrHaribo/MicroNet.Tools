package micronet.tools.launch.utility;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import micronet.tools.console.Console;
import micronet.tools.core.ServiceProject;
import micronet.tools.core.ServiceProject.Nature;

public final class LaunchServiceGroupUtility {
	
	private final static String launchName = "ServiceGroupLaunch";
	
	private LaunchServiceGroupUtility() {
	}
	
	public static void launchNativeGroup(List<ServiceProject> projects, String mode) {
		try {
			List<IJavaProject> javaProjects = new ArrayList<>();
			for (ServiceProject serviceProject : projects) {
				if (!serviceProject.hasNature(Nature.JAVA))
					continue;
				IJavaProject javaProject = JavaCore.create(serviceProject.getProject());
				javaProjects.add(javaProject);
			}
			launchNativeJavaGroup(javaProjects, mode);
		} catch (Exception e) {
			Console.print("Error launch native Service launch group");
			Console.printStackTrace(e);
		}
	}
	
	public static void launchNativeJavaGroup(List<IJavaProject> javaProjects, String mode) {
		try {
			if (LaunchUtility.isLaunchRunning(launchName)) {
				System.out.println("Launch is already there");
				LaunchUtility.showWarningMessageBox(launchName + " is currently running.", "Multiple Instances concurrent of the complete game not supported;");
				return;
			}
			
			ILaunchConfiguration launchConfig = getNativeLaunchGroupConfiguration(javaProjects, mode);
			DebugUITools.launch(launchConfig, mode);
		} catch (Exception e) {
			Console.print("Error launch native Java launch group");
			Console.printStackTrace(e);
		}
	}

	public static ILaunchConfiguration getNativeLaunchGroupConfiguration(List<IJavaProject> javaProjects, String mode) {
		try {
			ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
			ILaunchConfigurationType type = manager.getLaunchConfigurationType("org.eclipse.debug.core.groups.GroupLaunchConfigurationType");
			ILaunchConfigurationWorkingCopy workingCopy = type.newInstance(null, launchName);
			
			int idx = 0;
			for (IJavaProject javaProject : javaProjects) {
				ILaunchConfiguration servicelaunch = LaunchServiceUtility.getNativeLaunchConfiguration(javaProject);
				
				workingCopy.setAttribute("org.eclipse.debug.core.launchGroup." + idx + ".action", "NONE");
				workingCopy.setAttribute("org.eclipse.debug.core.launchGroup." + idx + ".adoptIfRunning", false);
				workingCopy.setAttribute("org.eclipse.debug.core.launchGroup." + idx + ".enabled", true);
				workingCopy.setAttribute("org.eclipse.debug.core.launchGroup." + idx + ".mode", mode);
				workingCopy.setAttribute("org.eclipse.debug.core.launchGroup." + idx + ".name", servicelaunch.getName());
				idx++;
			}
			
			ILaunchConfiguration config = workingCopy.doSave();
			return config;
		} catch (Exception e) {
			Console.print("Error getting native launch group configuration");
			Console.printStackTrace(e);
		}
		return null;
	}
}
