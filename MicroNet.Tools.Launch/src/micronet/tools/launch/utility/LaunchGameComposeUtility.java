package micronet.tools.launch.utility;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;

import micronet.tools.console.Console;

public final class LaunchGameComposeUtility {
	private final static String gameComposeName = "game-compose";

	private LaunchGameComposeUtility() {
	}
	
	public static void launchGame() {
		
		ILaunch launch = LaunchUtility.getLaunch(gameComposeName);
		if (launch != null) {
			try {
				System.out.println("Launch is already there");
				launch.terminate();
				ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
				manager.removeLaunch(launch);
			} catch (Exception e) {
				Console.print("Error launching Game Compose");
				Console.printStackTrace(e);
			}
		}
		
		ILaunchConfiguration buildConfig = getGameComposeConfig();
		DebugUITools.launch(buildConfig, "run");
	}
	
	private static ILaunchConfiguration getGameComposeConfig() {
		try {
			ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
			ILaunchConfigurationType type = manager.getLaunchConfigurationType("org.eclipse.linuxtools.docker.ui.dockerComposeUpLaunchConfigurationType");

			ILaunchConfigurationWorkingCopy workingCopy = type.newInstance(null, gameComposeName);
			workingCopy.setAttribute("dockerConnection", "http://127.0.0.1:2375");
			workingCopy.setAttribute("workingDir", "/");
			workingCopy.setAttribute("workingDirWorkspaceRelativeLocation", true);
			
			ILaunchConfiguration config = workingCopy.doSave();
			return config;
		} catch (Exception e) {
			Console.print("Error getting Game Compose Launch Config");
			Console.printStackTrace(e);
		}
		return null;
	}
}
