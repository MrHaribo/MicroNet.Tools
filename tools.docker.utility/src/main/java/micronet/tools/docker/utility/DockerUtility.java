package micronet.tools.docker.utility;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import com.fasterxml.jackson.databind.deser.std.MapEntryDeserializer;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.ProgressHandler;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;
import com.spotify.docker.client.messages.ProgressMessage;

public class DockerUtility {
	
	public enum DockerStatus {
		OK, ERROR;
	}
	
	public static void removeContainer(String id) {
		try {
			DockerClient docker = DefaultDockerClient.fromEnv().build();
			docker.removeContainer(id);
			docker.close();
		} catch (DockerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DockerCertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void stopContainer(String id) {
		try {
			DockerClient docker = DefaultDockerClient.fromEnv().build();
			docker.killContainer(id);
			docker.close();
		} catch (DockerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DockerCertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void startContainer(String id) {
		try {
			DockerClient docker = DefaultDockerClient.fromEnv().build();
			docker.startContainer(id);
			docker.close();
		} catch (DockerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DockerCertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static String createContainer(String name, List<String> links, List<String> ports) {
		try {
			if (links == null)
				links = new ArrayList<>();
			
			//<host port, container port>
			Map<String, String> portMap = createPortMap(ports);
			final String[] portArray = (String[]) portMap.values().toArray(new String[portMap.values().size()]);
		

			DockerClient docker = DefaultDockerClient.fromEnv().build();
			// Bind container ports to host ports
			final Map<String, List<PortBinding>> portBindings = new HashMap<>();
			for (Map.Entry<String, String> port : portMap.entrySet()) {
			    List<PortBinding> hostPorts = new ArrayList<>();
			    hostPorts.add(PortBinding.of("0.0.0.0", port.getKey()));
			    portBindings.put(port.getValue(), hostPorts);
			}

			final HostConfig hostConfig = HostConfig.builder()
					.portBindings(portBindings)
					.links(links)
					.build();

			// Create container with exposed ports
			final ContainerConfig containerConfig = ContainerConfig.builder()
			    .hostConfig(hostConfig)
			    .image(name)
			    .exposedPorts(portArray)
			    .build();

			final ContainerCreation creation = docker.createContainer(containerConfig, name);
			docker.close();
			return creation.id();
		} catch (DockerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DockerCertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private static Map<String, String> createPortMap(List<String> ports) {
		Map<String, String> portMap = new HashMap<>();
		if (ports != null) {
			for (String portString : ports) {
				
				
				String[] portCombination = portString.split(":");
				
				if (portCombination[0].contains("-")) {
					
					String hostPortRange = portCombination[0];
					String containerPortRange = portCombination[1];
					
					String[] hostTokens = hostPortRange.split("-");
					String[] containerTokens = containerPortRange.split("-");
					
					int hostStartPort = Integer.parseInt(hostTokens[0]);
					int hostEndPort = Integer.parseInt(hostTokens[1]);
					int containerStartPort = Integer.parseInt(containerTokens[0]);
					int containerEndPort = Integer.parseInt(containerTokens[1]);
					
					if (hostEndPort - hostStartPort != containerEndPort - containerStartPort)
						throw new IllegalArgumentException("Exposed Ports cannot be parsed");
					
					int i = 0;
					for (int hostPort = hostStartPort; hostPort <= hostEndPort; hostPort++) {
						int containerPort = containerStartPort + i++;
						portMap.put(Integer.toString(hostPort), Integer.toString(containerPort));
					}
					
				} else {
					String hostPort = portCombination[0];
					String containerPort = portCombination[1];
					portMap.put(hostPort, containerPort);
				}
			}
		}
		return portMap;
	}
	
	public static void buildImage(Path dir, String name, PrintStream infoOut, BiConsumer<DockerStatus, String> callback) {
		try {
			DockerClient docker = DefaultDockerClient.fromEnv().build();

			docker.build(dir, name, new ProgressHandler() {
				@Override
				public void progress(ProgressMessage message) throws DockerException {
					if (message.stream() != null)
						infoOut.print(message.stream());
					if (message.progress() != null)
						infoOut.print(message.progress() + "\r");
					final String imageId = message.buildImageId();
					if (imageId != null) {
						callback.accept(DockerStatus.OK, imageId);
					}
				}
			});
			docker.close();
		} catch (DockerCertificateException e) {
			callback.accept(DockerStatus.OK, "DockerCertificateException");
			e.printStackTrace(infoOut);
		} catch (DockerException e) {
			callback.accept(DockerStatus.OK, "DockerException");
			e.printStackTrace(infoOut);
		} catch (InterruptedException e) {
			callback.accept(DockerStatus.OK, "InterruptedException");
			e.printStackTrace(infoOut);
		} catch (IOException e) {
			callback.accept(DockerStatus.OK, "IOException");
			e.printStackTrace(infoOut);
		}
	}
}
