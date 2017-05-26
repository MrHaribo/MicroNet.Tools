package micronet.tools.launch.utility;

import micronet.tools.core.ServiceProject;

public final class BuildUtility {
	private BuildUtility() {
		
	}

	public static void buildMaven(ServiceProject project, String mode) {
		BuildServiceMavenUtility.buildMavenProject(project, mode, launch -> { });
	}
	
	public static void buildFull(ServiceProject project, String mode) {
		BuildServiceMavenUtility.buildMavenProject(project, mode, launch -> {
			BuildServiceContainerUtility.buildContainer(project);
		});
	}
}
