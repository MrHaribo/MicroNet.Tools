package micronet.tools.launch.utility;

import java.io.InputStream;
import java.io.PrintStream;


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
			
			String launchName = launch.getLaunchConfiguration().getName();
			PrintStream printStream = Console.reuseConsole(launchName);
			
			printStream.print("----------------------------------------------\n");
			printStream.print("|          Staring Container Build           |\n");
			printStream.print("----------------------------------------------\n");
			
			Console.printStream(containerStream, printStream);
		});
	}
}
