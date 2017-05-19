package micronet.tools.core;

import org.eclipse.core.resources.IProject;

public class ServiceProject {
	private IProject project;
	private String version;
	private boolean enabled;

	public ServiceProject(IProject project, String version) {
		this.project = project;
		this.version = version;
	}

	public String getName() {
		return project != null ? project.getName() : "UNKNOWN";
	}

	public String getVersion() {
		return version;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public void setEnabled(boolean isEnabled) {
		this.enabled = isEnabled;
	}

	@Override
	public String toString() {
		return getName() + " " + version;
	}

	public IProject getProject() {
		return project;
	}

}
