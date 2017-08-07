package micronet.tools.launch.utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.internal.loader.ModuleClassLoader.GenerationProtectionDomain;
import org.eclipse.swt.widgets.Display;

import micronet.tools.core.ModelProvider;
import micronet.tools.core.PreferenceConstants;

public class DockerUtility {

	public static boolean useDockerToolbox() {
		return ModelProvider.INSTANCE.getPreferenceStore().getBoolean(PreferenceConstants.P_USE_DOCKER_TOOLBOX);
	}

	public static String getDockerToolboxPath() {
		return ModelProvider.INSTANCE.getPreferenceStore().getString(PreferenceConstants.P_DOCKER_TOOLBOX_PATH);
	}

	public static String getDockerCommand() {
		return useDockerToolbox() ? getDockerToolboxPath() + "/docker" : "docker";
	}

	public static String getDockerComposeCommand() {
		return useDockerToolbox() ? getDockerToolboxPath() + "/docker-compose" : "docker-compose";
	}
	
	public static void testNetwork(String networkName, Consumer<String> resultCallback) {
		String dockerCommand = DockerUtility.getDockerCommand();

		ProcessBuilder builder = getProcessBuilder();
		builder.command(dockerCommand, "network", "inspect", networkName);
		
		runDockerProcessAsync(builder, result->{
			resultCallback.accept(result);
		});
	}
	
	public static void createNetwork(String networkName, Consumer<String> resultCallback) {
		String dockerCommand = DockerUtility.getDockerCommand();

		ProcessBuilder builder = getProcessBuilder();
		builder.command(dockerCommand, "network", "create", "--driver", "bridge", networkName);
		
		runDockerProcessAsync(builder, result->{
			resultCallback.accept(result);
		});
	}

	public static void testDocker(Consumer<Boolean> resultCallback) {

		String dockerCommand = DockerUtility.getDockerCommand();

		List<String> argArray = new ArrayList<>();
		argArray.add(dockerCommand);
		argArray.add("info");

		ProcessBuilder builder = getProcessBuilder();
		builder.command(argArray);
		
		runDockerProcessAsync(builder, result->{
			boolean res = !result.contains("error") && !result.contains("Error");
			resultCallback.accept(res);
		});
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
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
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
	
	private static ProcessBuilder getProcessBuilder() {
		ProcessBuilder builder = new ProcessBuilder();
		builder.redirectErrorStream(true);

		if (DockerUtility.useDockerToolbox()) {
			String dockerToolboxPath = DockerUtility.getDockerToolboxPath();
			Map<String, String> dockerMachineEnvironmentVariables = DockerUtility
					.getDockerMachineEnvironmentVariables(dockerToolboxPath);
			for (Map.Entry<String, String> envVar : dockerMachineEnvironmentVariables.entrySet()) {
				builder.environment().put(envVar.getKey(), envVar.getValue());
			}
		}
		return builder;
	}
}
