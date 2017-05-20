package micronet.tools.launch.utility;

import java.util.function.Consumer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.m2e.actions.MavenLaunchConstants;

import micronet.tools.core.ServiceProject;
import micronet.tools.core.ServiceProject.Nature;

@SuppressWarnings("restriction")
public final class BuildServiceMavenUtility {
	private BuildServiceMavenUtility() {

	}

	public static void buildMavenProject(ServiceProject serviceProject, String mode) {
		buildMavenProject(serviceProject, mode, launch -> { });
	}

	public static void buildMavenProject(ServiceProject serviceProject, String mode, Consumer<ILaunch> endCallback) {
		
		if (!serviceProject.hasNature(Nature.MAVEN))
			return;
		
		IProject project = serviceProject.getProject();
		String buildName = getBuildName(project);
		System.out.println("Building: " + buildName);

		if (LaunchUtility.isLaunchRunning(buildName)) {
			System.out.println("Launch is already there");
			LaunchUtility.showWarningMessageBox(buildName + " is currently building.",
					"Wait for build to end or terminate build");
			return;
		}

		LaunchUtility.waitForLaunchTermination(buildName, launch -> {
			System.out.println("Maven build launch terminated: " + launch.getLaunchConfiguration().getName());
			endCallback.accept(launch);
		});

		ILaunchConfiguration buildConfig = getMavenBuildConfig(project);
		DebugUITools.launch(buildConfig, mode);
	}

	private static ILaunchConfiguration getMavenBuildConfig(IProject project) {
		String buildName = getBuildName(project);
		try {
			ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
			ILaunchConfigurationType type = manager
					.getLaunchConfigurationType(MavenLaunchConstants.LAUNCH_CONFIGURATION_TYPE_ID);
			ILaunchConfiguration[] configurations = manager.getLaunchConfigurations(type);

			for (ILaunchConfiguration iLaunchConfiguration : configurations) {
				if (iLaunchConfiguration.getName().equals(buildName))
					return iLaunchConfiguration;
			}

			ILaunchConfigurationWorkingCopy workingCopy = type.newInstance(null, buildName);
			workingCopy.setAttribute(MavenLaunchConstants.ATTR_POM_DIR, "${project_loc:" + project.getName() + "}");
			workingCopy.setAttribute(MavenLaunchConstants.ATTR_GOALS, "install");
			workingCopy.setAttribute(MavenLaunchConstants.ATTR_UPDATE_SNAPSHOTS, true);
			workingCopy.setAttribute(MavenLaunchConstants.ATTR_RUNTIME, "EMBEDDED");
			ILaunchConfiguration config = workingCopy.doSave();
			return config;
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String getBuildName(IProject project) {
		return project.getName() + "Build";
	}
}
