package micronet.tools.api;

public class ServiceAPI {
	private String serviceName;
	private String serviceUri;
	private String description;
	private ListenerAPI[] listeners;

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getServiceUri() {
		return serviceUri;
	}

	public void setServiceUri(String serviceUri) {
		this.serviceUri = serviceUri;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public ListenerAPI[] getListeners() {
		return listeners;
	}

	public void setListeners(ListenerAPI[] listeners) {
		this.listeners = listeners;
	}
}
