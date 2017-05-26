package micronet.tools.launch.utility;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;

import micronet.tools.core.ServiceProject;
import micronet.tools.core.ServiceProject.Nature;

public final class BuildServiceContainerUtility {

	private BuildServiceContainerUtility() {
	}
	
	public static InputStream buildContainer(ServiceProject serviceProject, String ... args) {
		
		if (!serviceProject.hasNature(Nature.DOCKER))
			return null;

		IPath projectLocation = serviceProject.getProject().getLocation();
		String projectPath = projectLocation.toOSString();

		List<String> argArray = new ArrayList<>();
		argArray.add("docker");
		argArray.add("build");	
		argArray.add("-t");
		argArray.add(serviceProject.getName().toLowerCase());
		
		for (String arg : args) {
			argArray.add(arg);
		}
		
		argArray.add(".");
		
		ProcessBuilder builder = new ProcessBuilder(argArray);
		builder.directory(new File(projectPath));
		builder.redirectErrorStream(true);

		try {
			Process process = builder.start();
			return process.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
