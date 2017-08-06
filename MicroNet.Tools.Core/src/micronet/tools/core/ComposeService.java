package micronet.tools.core;

public class ComposeService {
	private String image;
	private String build;
	private String[] networks;
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

	public String[] getNetworks() {
		return networks;
	}

	public void setNetworks(String[] networks) {
		this.networks = networks;
	}
}
