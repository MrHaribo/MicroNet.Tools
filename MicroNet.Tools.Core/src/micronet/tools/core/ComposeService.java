package micronet.tools.core;

public class ComposeService {
	private String image;
	private String build;
	private String network_mode;
	private String[] links;
	private String[] ports;

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getBuild() {
		return build;
	}

	public void setBuild(String build) {
		this.build = build;
	}

	public String[] getLinks() {
		return links;
	}

	public void setLinks(String[] links) {
		this.links = links;
	}

	public String[] getPorts() {
		return ports;
	}

	public void setPorts(String[] ports) {
		this.ports = ports;
	}

	public String getNetwork_mode() {
		return network_mode;
	}

	public void setNetwork_mode(String network_mode) {
		this.network_mode = network_mode;
	}
}
