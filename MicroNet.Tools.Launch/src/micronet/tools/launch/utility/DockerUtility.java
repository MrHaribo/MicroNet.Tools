package micronet.tools.launch.utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import micronet.tools.core.ModelProvider;
import micronet.tools.core.preferences.PreferenceConstants;

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
}
