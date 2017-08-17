package micronet.tools.launch.utility;

import java.io.InputStream;
import java.io.PrintStream;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import micronet.tools.console.Console;
import micronet.tools.core.Icons;
import micronet.tools.core.ServiceProject;

public final class BuildUtility {
	private BuildUtility() {

	}

	public static void buildMaven(ServiceProject project, String mode) {
		try {
			BuildServiceMavenUtility.buildMavenProject(project, mode, launch -> { });
		} catch (Exception e) {
			Console.print("Error building Service Pom " + project.getName());
			Console.printStackTrace(e);
		}
	}

	public static void buildFull(ServiceProject project, String mode) {
		try {
			BuildServiceMavenUtility.buildMavenProject(project, mode, launch -> {
				
				try {
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
						
						Console.createConsole(project.getName() + " Container Build", containerStream, win.getActivePage(), Icons.IMG_DOCKER);
					}
				} catch (Exception e) {
					Console.print("Error building Full Service Pom Step " + project.getName());
					Console.printStackTrace(e);
				}
			});
		} catch (Exception e) {
			Console.print("Error building Full Service Container Step " + project.getName());
			Console.printStackTrace(e);
		}
	}
	
	public static void buildGame() {
		try {
			BuildGameMavenUtility.buildGame(launch -> {
				
				try {
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
						
						Console.createConsole("Game Compose Build", buildStream, win.getActivePage(), Icons.IMG_DOCKER);
					}
				} catch (Exception e) {
					Console.print("Error building Full Game Pom Step");
					Console.printStackTrace(e);
				}
			});
		} catch (Exception e) {
			Console.print("Error building Full Game Container Step");
			Console.printStackTrace(e);
		}
	}
}
