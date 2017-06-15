package micronet.tools.launch.utility;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;

public final class BuildGameComposeUtility {
	
	public static InputStream buildGame() {
		
		String dockerComposeCommand = DockerUtility.getDockerComposeCommand();
		
		List<String> argArray = new ArrayList<>();
		argArray.add(dockerComposeCommand);
		argArray.add("build");
		
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		String composePath = workspaceRoot.getLocation().toOSString();
		
		ProcessBuilder builder = new ProcessBuilder(argArray);
		builder.directory(new File(composePath));
		builder.redirectErrorStream(true);

		if (DockerUtility.useDockerToolbox())  {
			String dockerToolboxPath = DockerUtility.getDockerToolboxPath();
			Map<String, String> dockerMachineEnvironmentVariables = DockerUtility.getDockerMachineEnvironmentVariables(dockerToolboxPath);
			for (Map.Entry<String, String> envVar : dockerMachineEnvironmentVariables.entrySet()) {
				builder.environment().put(envVar.getKey(), envVar.getValue());
			}
		}
		
		try {
			Process process = builder.start();
			return process.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
