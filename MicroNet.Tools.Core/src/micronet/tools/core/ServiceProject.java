package micronet.tools.core;

import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;

import org.eclipse.core.resources.IProject;

public class ServiceProject {
	
	public enum Nature {
		MAVEN,
	    DOCKER, 
	    JAVA;
	}
	
	private IProject project;
	private String version;
	private boolean enabled;
	private boolean isInGamePom;
	private boolean isInGameCompose;
    private Set<Nature> natures = new HashSet<>();

	public ServiceProject(IProject project, String version) {
		this.project = project;
		this.version = version;
	}
    
	public ServiceProject(IProject project, String version, Nature... natures) {
		this(project, version);
		for (Nature nature : natures) {
			this.natures.add(nature);
		}
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

	public boolean isInGamePom() {
		return isInGamePom;
	}

	public void setInGamePom(boolean isInGamePom) {
		this.isInGamePom = isInGamePom;
	}

	public boolean isInGameCompose() {
		return isInGameCompose;
	}

	public void setInGameCompose(boolean isInGameCompose) {
		this.isInGameCompose = isInGameCompose;
	}

	@Override
	public String toString() {
		return getName() + " " + version;
	}

	public IProject getProject() {
		return project;
	}

	public String getNatureString() {
		StringJoiner joiner = new StringJoiner(",");
		for (Nature nature : natures) {
		    joiner.add(nature.toString());
		}
		return joiner.toString();
	}
	
    public void addNature(Nature nature) {
    	natures.add(nature);
    }
    
    public void removeNature(Nature nature) {
    	natures.remove(nature);
    }
    
    public boolean hasNature(Nature nature) {
    	return natures.contains(nature);
    }
}
