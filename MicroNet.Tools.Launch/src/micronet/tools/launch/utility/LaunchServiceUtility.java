package micronet.tools.launch.utility;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;

import micronet.tools.console.Console;
import micronet.tools.core.ServiceProject;
import micronet.tools.core.ServiceProject.Nature;

public final class LaunchServiceUtility {
	private LaunchServiceUtility() {
	}
	
	public static void launchNative(ServiceProject project, String mode) {
		try {
			if (!project.hasNature(Nature.JAVA))
				return;
			IJavaProject javaProject = JavaCore.create(project.getProject());
			launchNative(javaProject, mode);
		} catch (Exception e) {
			Console.print("Error launching native Service");
			Console.printStackTrace(e);
		} 
	}
	
	public static void launchNative(IJavaProject javaProject, String mode) {
		try {
			ILaunchConfiguration launchConfig = getNativeLaunchConfiguration(javaProject);
			DebugUITools.launch(launchConfig, mode);
		} catch (Exception e) {
			Console.print("Error launching native Java Project");
			Console.printStackTrace(e);
		}
	}

	public static ILaunchConfiguration getNativeLaunchConfiguration(IJavaProject project) {
		String projectName = project.getElementName();
		try {
			ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
			ILaunchConfigurationType type = manager.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);

			ILaunchConfigurationWorkingCopy workingCopy = type.newInstance(null, projectName);
			workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, project.getElementName());
			workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, "ServiceImpl");
			ILaunchConfiguration config = workingCopy.doSave();
			return config;
		} catch (Exception e) {
			Console.print("Error getting native launch configuration");
			Console.printStackTrace(e);
		}
		return null;
	}
}
