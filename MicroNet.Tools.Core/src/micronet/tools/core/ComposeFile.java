package micronet.tools.core;

import java.util.HashMap;
import java.util.Map;

public class ComposeFile {
	private String version;
	private Map<String, ComposeService> services = new HashMap<>();
	
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
}
