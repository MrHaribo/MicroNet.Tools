package micronet.tools.launch.utility;

import micronet.tools.core.ServiceProject;

public final class BuildUtility {
	private BuildUtility() {
		
	}
	
	public static void fullBuild(ServiceProject project, String mode) {
		BuildServiceMavenUtility.buildMavenProject(project, mode, launch -> {
			BuildServiceContainerUtility.buildContainer(project, mode);
		});
	}
}
