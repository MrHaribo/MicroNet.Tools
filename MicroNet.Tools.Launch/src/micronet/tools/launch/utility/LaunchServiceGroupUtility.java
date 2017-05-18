package micronet.tools.launch.utility;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

public final class LaunchServiceGroupUtility {
	
	private final static String launchName = "ServiceGroupLaunch";
	
	private LaunchServiceGroupUtility() {
	}
	
	public static void launchNativeGroup(List<IProject> projects, String mode) {
		List<IJavaProject> javaProjects = new ArrayList<>();
		for (IProject project : projects) {
			try {
				if (!project.hasNature(JavaCore.NATURE_ID))
					continue;
				IJavaProject javaProject = JavaCore.create(project);
				javaProjects.add(javaProject);
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		launchNativeJavaGroup(javaProjects, mode);
	}
	
	public static void launchNativeJavaGroup(List<IJavaProject> javaProjects, String mode) {
		
		if (LaunchUtility.isLaunchRunning(launchName)) {
			System.out.println("Launch is already there");
			LaunchUtility.showWarningMessageBox(launchName + " is currently running.", "Multiple Instances concurrent of the complete game not supported;");
			return;
		}
		
		ILaunchConfiguration launchConfig = getNativeLaunchGroupConfiguration(javaProjects, mode);
		DebugUITools.launch(launchConfig, mode);
	}

	private static ILaunchConfiguration getNativeLaunchGroupConfiguration(List<IJavaProject> javaProjects, String mode) {

		try {
			ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
			ILaunchConfigurationType type = manager.getLaunchConfigurationType("org.eclipse.cdt.launch.launchGroup");
			ILaunchConfigurationWorkingCopy workingCopy = type.newInstance(null, launchName);
			
			int idx = 0;
			for (IJavaProject javaProject : javaProjects) {
				ILaunchConfiguration servicelaunch = LaunchServiceUtility.getNativeLaunchConfiguration(javaProject);
				
				workingCopy.setAttribute("org.eclipse.cdt.launch.launchGroup." + idx + ".action", "NONE");
				workingCopy.setAttribute("org.eclipse.cdt.launch.launchGroup." + idx + ".enabled", "true");
				workingCopy.setAttribute("org.eclipse.cdt.launch.launchGroup." + idx + ".mode", mode);
				workingCopy.setAttribute("org.eclipse.cdt.launch.launchGroup." + idx + ".name", servicelaunch.getName());
				idx++;
			}
			
			ILaunchConfiguration config = workingCopy.doSave();
			return config;
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}
}
