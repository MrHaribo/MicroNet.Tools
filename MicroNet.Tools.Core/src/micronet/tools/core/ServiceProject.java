package micronet.tools.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.service.prefs.BackingStoreException;

public class ServiceProject {
	
	public enum Nature {
		MAVEN,
	    DOCKER, 
	    JAVA,
	    DEPENDENCY;
	}
	
	private IProject project;
	private String version;
    private Set<Nature> natures = new HashSet<>();
    private List<String> ports = new ArrayList<>();

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
	
	public void setVersion(String version) {
		this.version = version;
	}
	
	public boolean isEnabled() {
		ProjectScope projectScope = new ProjectScope(project);
		IEclipsePreferences preferences = projectScope.getNode("com.github.mrharibo.micronet.preferences");
		return preferences.getBoolean("enabled", false);
	}

	public void setEnabled(boolean isEnabled) {
		try {
			ProjectScope projectScope = new ProjectScope(project);
			IEclipsePreferences preferences = projectScope.getNode("com.github.mrharibo.micronet.preferences");
			preferences.putBoolean("enabled", isEnabled);
			preferences.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}

	public boolean isInGamePom() {
		return SyncPom.getServicesFromGamePom().contains(project.getName());
	}


	public boolean isInGameCompose() {
		return SyncCompose.isServiceInCompose(this);
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

	public String getLinksRaw() {
		ProjectScope projectScope = new ProjectScope(project);
		IEclipsePreferences preferences = projectScope.getNode("com.github.mrharibo.micronet.preferences");
		String linkString = preferences.get("links", "");
		return linkString;
	}
    
	public List<String> getLinks() {
		String linkString = getLinksRaw();
		List<String> result = new ArrayList<>(Arrays.asList(linkString.split(",")));
		result.remove("");
		return result;
	}

	public void setLinks(List<String> links) {
		ProjectScope projectScope = new ProjectScope(project);
		IEclipsePreferences preferences = projectScope.getNode("com.github.mrharibo.micronet.preferences");
		
		StringJoiner joiner = new StringJoiner(",");
		for (String link : links) {
			if (link.equals(""))
				continue;
		    joiner.add(link.toLowerCase());
		}
		
		try {
			preferences.put("links", joiner.toString());
			preferences.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}

	public String getPortsRaw() {
		ProjectScope projectScope = new ProjectScope(project);
		IEclipsePreferences preferences = projectScope.getNode("com.github.mrharibo.micronet.preferences");
		String portString = preferences.get("ports", "");
		return portString;
	}
	
	public List<String> getPorts() {
		String portString = getPortsRaw();
		List<String> result = new ArrayList<>(Arrays.asList(portString.split(",")));
		result.remove("");
		return result;
	}

	public void setPorts(List<String> ports) {
		ProjectScope projectScope = new ProjectScope(project);
		IEclipsePreferences preferences = projectScope.getNode("com.github.mrharibo.micronet.preferences");
		
		StringJoiner joiner = new StringJoiner(",");
		for (String port : ports) {
			if (port.equals(""))
				continue;
		    joiner.add(port);
		}
		
		try {
			preferences.put("ports", joiner.toString());
			preferences.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}
}
