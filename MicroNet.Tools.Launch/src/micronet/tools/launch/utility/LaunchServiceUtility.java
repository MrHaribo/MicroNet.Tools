package micronet.tools.launch.utility;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;

import micronet.tools.core.ServiceProject;
import micronet.tools.core.ServiceProject.Nature;

public final class LaunchServiceUtility {
	private LaunchServiceUtility() {
	}
	
	public static void launchNative(ServiceProject project, String mode) {
		if (!project.hasNature(Nature.JAVA))
			return;
		IJavaProject javaProject = JavaCore.create(project.getProject());
		launchNative(javaProject, mode);
	}
	
	public static void launchNative(IJavaProject javaProject, String mode) {
		ILaunchConfiguration launchConfig = getNativeLaunchConfiguration(javaProject);
		DebugUITools.launch(launchConfig, mode);
	}

	public static ILaunchConfiguration getNativeLaunchConfiguration(IJavaProject project) {
		String projectName = project.getElementName();
		try {
			ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
			ILaunchConfigurationType type = manager
					.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
			ILaunchConfiguration[] configurations = manager.getLaunchConfigurations(type);

			for (ILaunchConfiguration iLaunchConfiguration : configurations) {
				if (iLaunchConfiguration.getName().equals(project))
					return iLaunchConfiguration;
			}

			ILaunchConfigurationWorkingCopy workingCopy = type.newInstance(null, projectName);
			workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, project.getElementName());
			workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, "ServiceImpl");
			ILaunchConfiguration config = workingCopy.doSave();
			return config;
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}
}
