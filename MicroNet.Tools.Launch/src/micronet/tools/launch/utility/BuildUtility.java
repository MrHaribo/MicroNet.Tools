package micronet.tools.launch.utility;

import java.io.InputStream;

import org.eclipse.ui.IWorkbenchWindow;

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

			IWorkbenchWindow[] workbenchWindows = micronet.tools.console.Activator.getDefault().getWorkbench()
					.getWorkbenchWindows();
			workbenchWindows[0].getActivePage();

			if (workbenchWindows[0].getActivePage() != null) {
				workbenchWindows[0].getShell().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						Console.createConsole(project.getName() + "Build", containerStream, workbenchWindows[0].getActivePage());
					}
				});
			}
		});
	}
}
