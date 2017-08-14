package micronet.tools.launch.utility;

import java.io.InputStream;
import java.io.PrintStream;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import micronet.tools.console.Console;
import micronet.tools.core.ServiceProject;

public final class BuildUtility {
	private BuildUtility() {

	}

	public static void buildMaven(ServiceProject project, String mode) {
		BuildServiceMavenUtility.buildMavenProject(project, mode, launch -> {
		});
	}

	public static void buildFull(ServiceProject project, String mode) {
		BuildServiceMavenUtility.buildMavenProject(project, mode, launch -> {
			InputStream containerStream = BuildServiceContainerUtility.buildContainer(project);
			if (containerStream == null)
				return;
			
			PrintStream printStream = Console.reuseConsole(launch);
			
			if (printStream != null) {
				printStream.print("----------------------------------------------\n");
				printStream.print("|          Staring Container Build           |\n");
				printStream.print("----------------------------------------------\n");
				
				Console.printStream(containerStream, printStream);
			} else {
				IWorkbenchWindow win = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				if (win == null || win.getActivePage() == null)
					return;
				
				Console.createConsole(project.getName() + " Container Build", containerStream, win.getActivePage());
			}
			
		});
	}
	
	public static void buildGame() {
		BuildGameMavenUtility.buildGame(launch -> {
			InputStream buildStream = BuildGameComposeUtility.buildGame();
			if (buildStream == null)
				return;
			
			PrintStream printStream = Console.reuseConsole(launch);
			
			if (printStream != null) {
				printStream.print("----------------------------------------------\n");
				printStream.print("|         Staring Game Compose Build         |\n");
				printStream.print("----------------------------------------------\n");
				
				Console.printStream(buildStream, printStream);
			} else {
				IWorkbenchWindow win = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				if (win == null || win.getActivePage() == null)
					return;
				
				Console.createConsole("Game Compose Build", buildStream, win.getActivePage());
			}
		});
	}
}
