package micronet.tools.launch.utility;

import java.util.List;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;

import micronet.tools.console.Console;
import micronet.tools.core.ServiceProject;

public final class LaunchServiceGroupUtility {
	
	private final static String launchName = "ServiceGroupLaunch";
	
	private LaunchServiceGroupUtility() {
	}
	
	public static void launchNativeGroup(List<ServiceProject> projects, String mode) {
		try {
			ILaunchConfiguration launchConfig = getNativeLaunchGroupConfiguration(projects, mode);
			if (launchConfig != null)
				DebugUITools.launch(launchConfig, mode);
		} catch (Exception e) {
			Console.print("Error launch native Java launch group");
			Console.printStackTrace(e);
		}
	}

	private static ILaunchConfiguration getNativeLaunchGroupConfiguration(List<ServiceProject> projects, String mode) {
		try {
			ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
			ILaunchConfigurationType type = manager.getLaunchConfigurationType("org.eclipse.debug.core.groups.GroupLaunchConfigurationType");
			ILaunchConfigurationWorkingCopy workingCopy = type.newInstance(null, launchName);
			
			if (workingCopy == null) {
				Console.print("Eclipse Oxygen is needed to use Launch Groups");
				return null;
			}
			
			int idx = 0;
			for (ServiceProject project : projects) {
				ILaunchConfiguration servicelaunch = LaunchServiceUtility.getNativeLaunchConfiguration(project);
				if (servicelaunch == null)
					continue;
				
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
