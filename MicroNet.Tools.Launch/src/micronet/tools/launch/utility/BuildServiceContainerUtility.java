package micronet.tools.launch.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;

import micronet.tools.core.ModelProvider;
import micronet.tools.core.ServiceProject;
import micronet.tools.core.ServiceProject.Nature;
import micronet.tools.core.preferences.PreferenceConstants;

public final class BuildServiceContainerUtility {

	private BuildServiceContainerUtility() {
	}
	
	public static InputStream buildContainer(ServiceProject serviceProject, String ... args) {
		
		if (!serviceProject.hasNature(Nature.DOCKER))
			return null;

		IPath projectLocation = serviceProject.getProject().getLocation();
		String projectPath = projectLocation.toOSString();
		
		boolean useDockerToolbox = ModelProvider.INSTANCE.getPreferenceStore().getBoolean(PreferenceConstants.P_USE_DOCKER_TOOLBOX);
		String dockerCommand = "docker";
		if (useDockerToolbox) 
			dockerCommand = ModelProvider.INSTANCE.getPreferenceStore().getString(PreferenceConstants.P_DOCKER_TOOLBOX_PATH) + "/docker";
		
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
		
		if (useDockerToolbox)  {
			Map<String, String> dockerMachineEnvironmentVariables = getDockerMachineEnvironmentVariables();
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
	
	public static void main(String[] args) {
		
		
		List<String> argArray = new ArrayList<>();
		argArray.add("docker");
		argArray.add("image");	
		argArray.add("ls");
		
		ProcessBuilder builder = new ProcessBuilder(argArray);
		builder.directory(new File("C:\\Program Files\\Docker Toolbox"));
		builder.redirectErrorStream(true);

		Map<String, String> dockerMachineEnvironmentVariables = getDockerMachineEnvironmentVariables();

		for (Map.Entry<String, String> envVar : dockerMachineEnvironmentVariables.entrySet()) {
			builder.environment().put(envVar.getKey(), envVar.getValue());
		}
		
		try {
			Process process = builder.start();
			printStream(process.getInputStream(), System.out);
		} catch (IOException e) {
		
		}
	}

	private static Map<String, String> getDockerMachineEnvironmentVariables() {
		
		
		List<String> argArray = new ArrayList<>();
		argArray.add("C:\\Program Files\\Docker Toolbox\\docker-machine");
		argArray.add("env");	
		
		ProcessBuilder builder = new ProcessBuilder(argArray);
		
		try {
			Process process = builder.start();
			Map<String, String> envVariables = parseEnvVariables(process.getInputStream());
			System.out.print(envVariables);
			return envVariables;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Map<String, String> parseEnvVariables(InputStream inStream) {
		Map<String, String> result = new HashMap<>();

		BufferedReader input = new BufferedReader(new InputStreamReader(inStream));
		String line = null;
		try {
			while ((line = input.readLine()) != null) {
				if (line.contains("REM") || !line.contains("DOCKER"))
					continue;
				
				String[] tokens = line.split("=");
				
				int keyStart = tokens[0].indexOf("DOCKER");
				result.put(tokens[0].substring(keyStart), tokens[1]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	public static void printStream(InputStream inStream, PrintStream outStream) {
		new Thread() {
			public void run() {
				BufferedReader input = new BufferedReader(new InputStreamReader(inStream));
				String line = null;
				try {
					while ((line = input.readLine()) != null) {
						outStream.println(line);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
}
