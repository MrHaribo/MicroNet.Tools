package micronet.tools.launch.utility;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import micronet.tools.core.ServiceProject;
import micronet.tools.core.ServiceProject.Nature;

public final class LaunchServiceContainerUtility {

	private LaunchServiceContainerUtility() {
	}

	public static InputStream launchContainer(ServiceProject serviceProject, String ... args) {

		if (!serviceProject.hasNature(Nature.DOCKER))
			return null;

		List<String> argArray = new ArrayList<>();
		argArray.add("docker");
		argArray.add("run");
		
		for (String port : serviceProject.getPorts()) {
			argArray.add("-p");
			argArray.add(port);
		}
		
		for (String arg : args) {
			argArray.add(arg);
		}
		
		argArray.add(serviceProject.getName().toLowerCase());
		
		ProcessBuilder builder = new ProcessBuilder(argArray);
		builder.redirectErrorStream(true);

		try {
			Process process = builder.start();
			return process.getInputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
}
