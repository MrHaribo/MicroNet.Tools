package micronet.tools.composition;

import java.util.HashMap;
import java.util.Map;

public class ComposeFile {
	private String version;
	private Map<String, ComposeService> services = new HashMap<>();
	private Map<String, Object> networks = new HashMap<>();
	
	public ComposeFile() {
	}
	
	public ComposeFile(String version) {
		this.version = version;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public Map<String, ComposeService> getServices() {
		return services;
	}
	public void setServices(Map<String, ComposeService> services) {
		this.services = services;
	}

	public Map<String, Object> getNetworks() {
		return networks;
	}

	public void setNetworks(Map<String, Object> networks) {
		this.networks = networks;
	}
}
