package micronet.tools.launch.utility;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;

import micronet.tools.console.Console;
import micronet.tools.core.ServiceProject;
import micronet.tools.core.ServiceProject.Nature;

public final class BuildServiceContainerUtility {

	private BuildServiceContainerUtility() {
	}
	
	public static InputStream buildContainer(ServiceProject serviceProject, String ... args) {
		try {
			if (!serviceProject.hasNature(Nature.DOCKER))
				return null;
	
			IPath projectLocation = serviceProject.getProject().getLocation();
			String projectPath = projectLocation.toOSString();
			
			String dockerCommand = DockerUtility.getDockerCommand();
			
			List<String> argArray = new ArrayList<>();
			argArray.add(dockerCommand);
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
			Console.print("Error building Service Container " + serviceProject.getName());
			Console.printStackTrace(e);
			return null;
		}
	}
}
