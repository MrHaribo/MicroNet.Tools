package micronet.tools.launch.utility;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import micronet.tools.console.Console;
import micronet.tools.core.ServiceProject;
import micronet.tools.core.ServiceProject.Nature;

public final class LaunchServiceContainerUtility {

	private LaunchServiceContainerUtility() {
	}

	public static InputStream launchContainer(ServiceProject serviceProject, String ... args) {

		try {
			if (!serviceProject.hasNature(Nature.DOCKER))
				return null;
	
			String dockerCommand = DockerUtility.getDockerCommand();
			
			List<String> argArray = new ArrayList<>();
			argArray.add(dockerCommand);
			argArray.add("run");
			
			for (String port : serviceProject.getPorts()) {
				argArray.add("-p");
				argArray.add(port);
			}
			
			argArray.add("--name=" + serviceProject.getContainerName());
			argArray.add("--network=" + serviceProject.getNetwork());
			
			if (serviceProject.getAlias() != null) {
				argArray.add("--network-alias=" + serviceProject.getAlias());
			}
			
			for (String arg : args) {
				argArray.add(arg);
			}
			
			argArray.add(serviceProject.getName().toLowerCase());
			
			ProcessBuilder builder = new ProcessBuilder(argArray);
			builder.redirectErrorStream(true);
			
			if (DockerUtility.useDockerToolbox())  {
				String dockerToolboxPath = DockerUtility.getDockerToolboxPath();
				Map<String, String> dockerMachineEnvironmentVariables = DockerUtility.getDockerMachineEnvironmentVariables(dockerToolboxPath);
				for (Map.Entry<String, String> envVar : dockerMachineEnvironmentVariables.entrySet()) {
					builder.environment().put(envVar.getKey(), envVar.getValue());
				}
			}

			Process process = builder.start();
			return process.getInputStream();
		} catch (Exception e) {
			Console.print("Error launching Service container " + serviceProject.getName());
			Console.printStackTrace(e);
			return null;
		}
	}
}
