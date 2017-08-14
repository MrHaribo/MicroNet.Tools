package micronet.tools.filesync;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Semaphore;

import micronet.network.NetworkConstants;
import micronet.serialization.Serialization;
import micronet.tools.api.ListenerAPI;
import micronet.tools.api.ParameterAPI;
import micronet.tools.api.ServiceAPI;
import micronet.tools.codegen.CodegenConstants;

public class SyncParameterCodes {
	
	private static Semaphore semaphore = new Semaphore(1);

	public static Set<String> getUsedParameters(ServiceAPI apiDescription) {
		Set<String> requiredParameters = new HashSet<String>();
		for (ListenerAPI listener : apiDescription.getListeners()) {
			if (listener.getRequestParameters() != null) {
				for (ParameterAPI parameter : listener.getRequestParameters()) {
					requiredParameters.add(parameter.getCode());
				}
			}
			if (listener.getResponseParameters() != null) {
				for (ParameterAPI parameter : listener.getResponseParameters()) {
					requiredParameters.add(parameter.getCode());
				}
			}
		}
		return requiredParameters;
	}
	
	public static Map<String, Set<String>> getAllRequiredParameters(List<ServiceAPI> completeAPI) {
		Map<String, Set<String>> requiredParameters = new HashMap<>();
		for (ServiceAPI serviceAPI : completeAPI) {
			for (ListenerAPI listenerAPI : serviceAPI.getListeners()) {
				if (listenerAPI.getRequestParameters() == null)
					continue;
				for (ParameterAPI paramAPI : listenerAPI.getRequestParameters()) {
					if (!requiredParameters.containsKey(paramAPI.getCode()))
						requiredParameters.put(paramAPI.getCode(), new TreeSet<>());
					requiredParameters.get(paramAPI.getCode()).add(serviceAPI.getServiceName());
				}
			}
		}
		return requiredParameters;
	}
	
	public static Map<String, Set<String>> getAllProvidedParameters(List<ServiceAPI> completeAPI) {
		Map<String, Set<String>> providedParameters = new HashMap<>();
		for (ServiceAPI serviceAPI : completeAPI) {
			for (ListenerAPI listenerAPI : serviceAPI.getListeners()) {
				if (listenerAPI.getResponseParameters() == null)
					continue;
				for (ParameterAPI paramAPI : listenerAPI.getResponseParameters()) {
					if (!providedParameters.containsKey(paramAPI.getCode()))
						providedParameters.put(paramAPI.getCode(), new TreeSet<>());
					providedParameters.get(paramAPI.getCode()).add(serviceAPI.getServiceName());
				}
			}
		}
		return providedParameters;
	}
	
	public static Set<String> getAllParameterCodes(String sharedDir) {
		Set<String> allParameterCodes = new TreeSet<String>();
		allParameterCodes.addAll(getBuiltInParameterCodes());
		allParameterCodes.addAll(getUserParameterCodes(sharedDir));
		return allParameterCodes;
	}
	
	public static Set<String> getBuiltInParameterCodes() {
		String[] networkConstants = Arrays.stream(NetworkConstants.class.getEnumConstants()).map(Enum::name).toArray(String[]::new);
		return new TreeSet<String>(Arrays.asList(networkConstants));
	}
	
	public static Set<String> getUserParameterCodes(String sharedDir) {
		return readParameters(sharedDir);
	}
	
	public static void contributeParameters(Set<String> contributedCodes, String sharedDir) {
		File parameterCodeFile = getParameterCodeFile(sharedDir);
		Set<String> builtInParameterCodes = getBuiltInParameterCodes();
		contributedCodes.removeAll(builtInParameterCodes);
		
	    try {
	        semaphore.acquire();

	        try {
	        	String data = new String(Files.readAllBytes(parameterCodeFile.toPath()), StandardCharsets.UTF_8);
				String[] codeArray = Serialization.deserialize(data, String[].class);
				
				Set<String> existingParameterCodes = new TreeSet<String>(Arrays.asList(codeArray));

				existingParameterCodes.addAll(contributedCodes);
				codeArray = existingParameterCodes.toArray(new String[existingParameterCodes.size()]);
				data = Serialization.serializePretty(codeArray);
				
				Files.write(parameterCodeFile.toPath(), data.getBytes());
	        } catch (IOException e) {
				e.printStackTrace();
			}
	    } catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
	        semaphore.release();
	    }
	}
	
	public static void removeParameters(Set<String> removedCodes, String sharedDir) {
		File parameterCodeFile = getParameterCodeFile(sharedDir);
		Set<String> builtInParameterCodes = getBuiltInParameterCodes();
		removedCodes.removeAll(builtInParameterCodes);
		
	    try {
	        semaphore.acquire();

	        try {
	        	String data = new String(Files.readAllBytes(parameterCodeFile.toPath()), StandardCharsets.UTF_8);
				String[] codeArray = Serialization.deserialize(data, String[].class);
				
				Set<String> existingParameterCodes = new TreeSet<String>(Arrays.asList(codeArray));

				existingParameterCodes.removeAll(removedCodes);
				codeArray = existingParameterCodes.toArray(new String[existingParameterCodes.size()]);
				data = Serialization.serializePretty(codeArray);
				
				Files.write(parameterCodeFile.toPath(), data.getBytes());
	        } catch (IOException e) {
				e.printStackTrace();
			}
	    } catch (InterruptedException e1) {
			e1.printStackTrace();
		} finally {
	        semaphore.release();
	    }
	}
	
	private static Set<String> readParameters(String sharedDir) {
		File parameterCodeFile = getParameterCodeFile(sharedDir);
		
	    try {
	        semaphore.acquire();

	        try {
	        	String data = new String(Files.readAllBytes(parameterCodeFile.toPath()), StandardCharsets.UTF_8);
				String[] codeArray = Serialization.deserialize(data, String[].class);
				
				if (codeArray == null)
					return new TreeSet<>();
				return new TreeSet<String>(Arrays.asList(codeArray));
	        } catch (IOException e) {
				e.printStackTrace();
			}
	    } catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
	        semaphore.release();
	    }
	    return new TreeSet<>();
	}
	
	private static File getParameterCodeFile(String sharedDir) {
		try {
			File parameterCodeFile = new File(sharedDir + CodegenConstants.PARAMETER_CODE);
			if (!parameterCodeFile.exists()) {
				String data = Serialization.serializePretty(getBuiltInParameterCodes());
				Files.write(parameterCodeFile.toPath(), data.getBytes());
			}
			return parameterCodeFile;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
