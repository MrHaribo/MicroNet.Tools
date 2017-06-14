package micronet.tools.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.IMavenProjectRegistry;
import org.osgi.service.prefs.BackingStoreException;

public class ServiceProject {
	
	private static final String SPLIT_STRING = ",";
	private static final String PREFERENCE_NAME = "com.github.mrharibo.micronet.preferences";

	public enum Nature {
		MAVEN,
	    DOCKER, 
	    JAVA;
	}
	
	private IProject project;
    private Set<Nature> natures = new HashSet<>();

	public ServiceProject(IProject project) {
		this.project = project;
	}
    
	public ServiceProject(IProject project, Nature... natures) {
		this(project);
		for (Nature nature : natures) {
			this.natures.add(nature);
		}
	}

	public String getName() {
		return project != null ? project.getName() : "UNKNOWN";
	}

	public boolean isEnabled() {
		ProjectScope projectScope = new ProjectScope(project);
		IEclipsePreferences preferences = projectScope.getNode(PREFERENCE_NAME);
		return preferences.getBoolean("enabled", false);
	}

	public void setEnabled(boolean isEnabled) {
		try {
			ProjectScope projectScope = new ProjectScope(project);
			IEclipsePreferences preferences = projectScope.getNode(PREFERENCE_NAME);
			preferences.putBoolean("enabled", isEnabled);
			preferences.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}
	
	public String getNetworkMode() {
		ProjectScope projectScope = new ProjectScope(project);
		IEclipsePreferences preferences = projectScope.getNode(PREFERENCE_NAME);
		return preferences.get("network_mode", null);
	}

	public void setNetworkMode(String networkMode) {
		try {
			ProjectScope projectScope = new ProjectScope(project);
			IEclipsePreferences preferences = projectScope.getNode(PREFERENCE_NAME);
			preferences.put("network_mode", networkMode);
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
		return getName() + " " + getVersion();
	}

	public IProject getProject() {
		return project;
	}
	
	public IPath getPath() {
		return project.getLocation();
	}

	public String getNatureString() {
		StringJoiner joiner = new StringJoiner(SPLIT_STRING);
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
		IEclipsePreferences preferences = projectScope.getNode(PREFERENCE_NAME);
		String linkString = preferences.get("links", "");
		return linkString;
	}
    
	public List<String> getLinks() {
		String linkString = getLinksRaw();
		List<String> result = new ArrayList<>(Arrays.asList(linkString.split(SPLIT_STRING)));
		result.remove("");
		return result;
	}

	public void setLinks(List<String> links) {
		ProjectScope projectScope = new ProjectScope(project);
		IEclipsePreferences preferences = projectScope.getNode(PREFERENCE_NAME);
		
		StringJoiner joiner = new StringJoiner(SPLIT_STRING);
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
		IEclipsePreferences preferences = projectScope.getNode(PREFERENCE_NAME);
		String portString = preferences.get("ports", "");
		return portString;
	}
	
	public List<String> getPorts() {
		String portString = getPortsRaw();
		List<String> result = new ArrayList<>(Arrays.asList(portString.split(SPLIT_STRING)));
		result.remove("");
		return result;
	}

	public void setPorts(List<String> ports) {
		ProjectScope projectScope = new ProjectScope(project);
		IEclipsePreferences preferences = projectScope.getNode(PREFERENCE_NAME);
		
		StringJoiner joiner = new StringJoiner(SPLIT_STRING);
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

	public void setRequiredParameters(Set<String> requiredParameters) {
		ProjectScope projectScope = new ProjectScope(project);
		IEclipsePreferences preferences = projectScope.getNode(PREFERENCE_NAME);
		
		StringJoiner joiner = new StringJoiner(SPLIT_STRING);
		for (String param : requiredParameters) {
			if (param.equals(""))
				continue;
		    joiner.add(param);
		}
		
		try {
			preferences.put("parameters.required", joiner.toString());
			preferences.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}
	
	public Set<String> getRequiredParameters() {
		ProjectScope projectScope = new ProjectScope(project);
		IEclipsePreferences preferences = projectScope.getNode(PREFERENCE_NAME);
		String parameterString = preferences.get("parameters.required", "");

		Set<String> result = new HashSet<>(Arrays.asList(parameterString.split(SPLIT_STRING)));
		result.remove("");
		return result;
	}
	
	public String getVersion() {
		if (!hasNature(Nature.MAVEN))
			return "";
		IMavenProjectRegistry projectManager = MavenPlugin.getMavenProjectRegistry();
		IMavenProjectFacade mavenProjectFacade = projectManager.getProject(project.getProject());
		if (mavenProjectFacade == null || mavenProjectFacade.getArtifactKey() == null)
			return "";
		return mavenProjectFacade.getArtifactKey().getVersion();
	}
	
	public String getArtifactID() {
		if (!hasNature(Nature.MAVEN))
			return "";
		IMavenProjectRegistry projectManager = MavenPlugin.getMavenProjectRegistry();
		IMavenProjectFacade mavenProjectFacade = projectManager.getProject(project.getProject());
		if (mavenProjectFacade == null || mavenProjectFacade.getArtifactKey() == null)
			return "";
		return mavenProjectFacade.getArtifactKey().getArtifactId();
	}
	
	public String getGroupID() {
		if (!hasNature(Nature.MAVEN))
			return "";
		IMavenProjectRegistry projectManager = MavenPlugin.getMavenProjectRegistry();
		IMavenProjectFacade mavenProjectFacade = projectManager.getProject(project.getProject());
		if (mavenProjectFacade == null || mavenProjectFacade.getArtifactKey() == null)
			return "";
		return mavenProjectFacade.getArtifactKey().getGroupId();
	}
}
