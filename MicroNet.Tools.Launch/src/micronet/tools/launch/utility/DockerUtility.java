package micronet.tools.launch.utility;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import micronet.tools.console.Console;
import micronet.tools.core.ModelProvider;
import micronet.tools.core.PreferenceConstants;

public class DockerUtility {

	public static boolean useDockerToolbox() {
		return ModelProvider.INSTANCE.getPreferenceStore().getBoolean(PreferenceConstants.PREF_USE_DOCKER_TOOLBOX);
	}

	public static String getDockerToolboxPath() {
		return ModelProvider.INSTANCE.getPreferenceStore().getString(PreferenceConstants.PREF_DOCKER_TOOLBOX_PATH);
	}

	public static String getDockerCommand() {
		return useDockerToolbox() ? getDockerToolboxPath() + "/docker" : "docker";
	}

	public static String getDockerComposeCommand() {
		return useDockerToolbox() ? getDockerToolboxPath() + "/docker-compose" : "docker-compose";
	}
	
	public static void testNetwork(String networkName, BiConsumer<Boolean, String> resultCallback) {
		try {
			String dockerCommand = DockerUtility.getDockerCommand();
	
			ProcessBuilder builder = getProcessBuilder();
			if (builder == null) {
				resultCallback.accept(false, "Error running docker command: " + dockerCommand);
				return;
			}
			builder.command(dockerCommand, "network", "inspect", networkName);
			
			runDockerProcessAsync(builder, result->{
				resultCallback.accept(true, result);
			});
		} catch (Exception e) {
			Console.print("Error testing Docker Network " + networkName);
			Console.printStackTrace(e);
			resultCallback.accept(false, "Error testing Docker Network " + networkName);
		}
	}
	
	public static void createNetwork(String networkName, BiConsumer<Boolean, String> resultCallback) {
		try {
			String dockerCommand = DockerUtility.getDockerCommand();
	
			ProcessBuilder builder = getProcessBuilder();
			if (builder == null) {
				resultCallback.accept(false, "Error running docker command: " + dockerCommand);
				return;
			}
			builder.command(dockerCommand, "network", "create", "--driver", "bridge", networkName);
			
			runDockerProcessAsync(builder, result->{
				resultCallback.accept(true, result);
			});
		} catch (Exception e) {
			Console.print("Error creating Docker Network " + networkName);
			Console.printStackTrace(e);
			resultCallback.accept(false, "Error creating Docker Network " + networkName + ":" + e);
		}
	}

	public static void testDocker(BiConsumer<Boolean, String> resultCallback) {
		try {
			String dockerCommand = DockerUtility.getDockerCommand();
	
			List<String> argArray = new ArrayList<>();
			argArray.add(dockerCommand);
			argArray.add("info");
	
			ProcessBuilder builder = getProcessBuilder();
			if (builder == null) {
				resultCallback.accept(false, "Error running docker command: " + dockerCommand);
				return;
			}
			
			builder.command(argArray);
			
			runDockerProcessAsync(builder, result->{
				resultCallback.accept(true, result);
			});
		} catch (Exception e) {
			Console.print("Error testing Docker Installation");
			Console.printStackTrace(e);
			resultCallback.accept(false, "Error testing Docker Installation: " + e);
		}
	}
	
	private static void runDockerProcessAsync(ProcessBuilder builder, Consumer<String> resultCallback) {
		new Thread(() -> {
			try {
				Process process = builder.start();

				int tries = 0;
				
				while (!Thread.interrupted() && tries < 25) {
					byte[] buffer = new byte[process.getInputStream().available()];
					process.getInputStream().read(buffer);
					String result = new String(buffer);
					
					System.out.println(result);
					
					if (result.length() <= 0) {
						tries++;
						Thread.sleep(100);
						continue;
					}
					
					resultCallback.accept(result);
					break;
				}
			} catch (Exception e) {
				Console.print("Error running Docker Process");
				Console.printStackTrace(e);
			}
		}).start();
	}

	public static Map<String, String> getDockerMachineEnvironmentVariables(String dockerToolboxPath) {

		List<String> argArray = new ArrayList<>();
		argArray.add(dockerToolboxPath + "/docker-machine");
		argArray.add("env");

		ProcessBuilder builder = new ProcessBuilder(argArray);

		try {
			Process process = builder.start();
			Map<String, String> envVariables = parseEnvVariables(process.getInputStream());
			System.out.print(envVariables);
			return envVariables;
		} catch (Exception e) {
			Console.print("Error retrieving Docker Env Variables");
			Console.printStackTrace(e);
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
		} catch (Exception e) {
			Console.print("Error parsing Docker Env Variables");
			Console.printStackTrace(e);
		}

		return result;
	}
	
	private static ProcessBuilder getProcessBuilder() {
		ProcessBuilder builder = new ProcessBuilder();
		builder.redirectErrorStream(true);

		if (DockerUtility.useDockerToolbox()) {
			String dockerToolboxPath = DockerUtility.getDockerToolboxPath();
			Map<String, String> dockerMachineEnvironmentVariables = DockerUtility.getDockerMachineEnvironmentVariables(dockerToolboxPath);
			if (dockerMachineEnvironmentVariables == null)
				return null;
			
			for (Map.Entry<String, String> envVar : dockerMachineEnvironmentVariables.entrySet()) {
				builder.environment().put(envVar.getKey(), envVar.getValue());
			}
		}
		return builder;
	}
}
