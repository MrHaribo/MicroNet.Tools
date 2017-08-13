package micronet.tools.core;

import java.io.File;
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
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.IMavenProjectRegistry;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
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

	public boolean isInGamePom() {
		return SyncPom.getServicesFromApplicationPom().contains(project.getName());
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
    
    public boolean isSharedDirContributionEnabled() {
		ProjectScope projectScope = new ProjectScope(project);
		IEclipsePreferences preferences = projectScope.getNode(PREFERENCE_NAME);
		return preferences.getBoolean("contribute.shared.dir", false);
    }
    
	public void setSharedDirContributionEnabled(boolean isEnabled) {
		try {
			ProjectScope projectScope = new ProjectScope(project);
			IEclipsePreferences preferences = projectScope.getNode(PREFERENCE_NAME);
			preferences.putBoolean("contribute.shared.dir", isEnabled);
			preferences.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}
	
	public String getContributedSharedDir() {
		File contributedSharedDir = project.getLocation().append("shared_contribution").toFile();
		if (contributedSharedDir.exists())
			return contributedSharedDir.getPath() + "/";
		return null;
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
    
    public String getContainerName() {
		ProjectScope projectScope = new ProjectScope(project);
		IEclipsePreferences preferences = projectScope.getNode(PREFERENCE_NAME);
		return preferences.get("container.name", getName().toLowerCase());
    }

	public String getNetwork() {
		ProjectScope projectScope = new ProjectScope(project);
		IEclipsePreferences preferences = projectScope.getNode(PREFERENCE_NAME);
		
		ScopedPreferenceStore preferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, "MicroNet.Tools.Preferences");
		String defaultValue = preferenceStore.getString(PreferenceConstants.DOCKER_NETWORK_NAME);
		
		return preferences.get("network", defaultValue);
	}
	
	public void setNetwork(String network) {
		try {
			ProjectScope projectScope = new ProjectScope(project);
			IEclipsePreferences preferences = projectScope.getNode(PREFERENCE_NAME);
			preferences.put("network", network);
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
