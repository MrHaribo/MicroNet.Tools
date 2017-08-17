package micronet.tools.launch.utility;

import java.util.function.Consumer;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.m2e.actions.MavenLaunchConstants;

import micronet.tools.console.Console;

@SuppressWarnings("restriction")
public final class BuildGameMavenUtility {
	private final static String buildName = "BuildGame";
	
	private BuildGameMavenUtility() {

	}

	public static void buildGame() {
		buildGame(launch -> {});
	}
	
	public static void buildGame(Consumer<ILaunch> endCallback) {
		try {
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
	
			ILaunchConfiguration buildConfig = getMavenGameBuildConfig();
			DebugUITools.launch(buildConfig, "run");
		} catch (Exception e) {
			Console.print("Error building Game Pom");
			Console.printStackTrace(e);
		}
	}

	private static ILaunchConfiguration getMavenGameBuildConfig() {
		try {
			ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
			ILaunchConfigurationType type = manager.getLaunchConfigurationType(MavenLaunchConstants.LAUNCH_CONFIGURATION_TYPE_ID);

			IWorkspaceRoot myWorkspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
			String pomFilepath = myWorkspaceRoot.getLocation().toOSString();
			
			ILaunchConfigurationWorkingCopy workingCopy = type.newInstance(null, buildName);
			workingCopy.setAttribute(MavenLaunchConstants.ATTR_POM_DIR, pomFilepath);
			workingCopy.setAttribute(MavenLaunchConstants.ATTR_GOALS, "clean install");
			workingCopy.setAttribute(MavenLaunchConstants.ATTR_UPDATE_SNAPSHOTS, true);
			workingCopy.setAttribute(MavenLaunchConstants.ATTR_RUNTIME, "EMBEDDED");
			ILaunchConfiguration config = workingCopy.doSave();
			return config;
		} catch (Exception e) {
			Console.print("Error getting Game Pom Build Config");
			Console.printStackTrace(e);
		}
		return null;
	}
}
