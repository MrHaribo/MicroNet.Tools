package micronet.tools.launch.utility;

import org.eclipse.core.resources.IProject;

public final class BuildUtility {
	private BuildUtility() {
		
	}
	
	public static void fullBuild(IProject project, String mode) {
		BuildServiceMavenUtility.buildMavenProject(project, mode, launch -> {
			BuildServiceContainerUtility.buildContainer(project, mode);
		});
	}
}
