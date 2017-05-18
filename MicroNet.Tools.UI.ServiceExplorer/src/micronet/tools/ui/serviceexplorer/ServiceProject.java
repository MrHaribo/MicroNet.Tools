package micronet.tools.ui.serviceexplorer;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.eclipse.core.resources.IProject;

public class ServiceProject {
	private String name;
	private String version;
	private boolean enabled;
	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

	public ServiceProject() {
	}

	public ServiceProject(IProject project, String version) {
		super();
		this.name = project.getName();
		this.version = version;
	}

	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}

	public String getName() {
		return name;
	}

	public String getVersion() {
		return version;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setName(String name) {
		propertyChangeSupport.firePropertyChange("name", this.name, this.name = name);
	}

	public void setVersion(String version) {
		propertyChangeSupport.firePropertyChange("version", this.version, this.version = version);
	}

	public void setEnabled(boolean isEnabled) {
		propertyChangeSupport.firePropertyChange("enabled", this.enabled, this.enabled = isEnabled);
	}

	@Override
	public String toString() {
		return name + " " + version;
	}

}
