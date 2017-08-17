package micronet.tools.core;

import static micronet.tools.core.PreferenceConstants.PREFERENCE_NAME_SERVICE_PROJECT;
import static micronet.tools.core.PreferenceConstants.PREFERENCE_NAME_GLOBAL;
import static micronet.tools.core.PreferenceConstants.PREF_CONTAINER_NAME;
import static micronet.tools.core.PreferenceConstants.PREF_CONTRIBUTE_SHARED_DIR;
import static micronet.tools.core.PreferenceConstants.PREF_ENABLED;
import static micronet.tools.core.PreferenceConstants.PREF_NETWORK;
import static micronet.tools.core.PreferenceConstants.PREF_ALIAS;
import static micronet.tools.core.PreferenceConstants.PREF_PORTS;
import static micronet.tools.core.PreferenceConstants.SPLIT_STRING;

import java.io.File;
import java.nio.file.Path;
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

import micronet.tools.composition.SyncCompose;
import micronet.tools.composition.SyncPom;
import micronet.tools.console.Console;

public class ServiceProject {
	
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
		return SyncPom.isServiceInApplicationPom(this);
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
	
	public String getRelativePath() {
		Path workspacePath = new File(ModelProvider.INSTANCE.getWorkspaceDir()).toPath();
		Path projectPath = new File(getPath().toString()).toPath();
		return workspacePath.relativize(projectPath).toString();
	}

	public String getNatureString() {
		StringJoiner joiner = new StringJoiner(SPLIT_STRING);
		for (Nature nature : natures) {
		    joiner.add(nature.toString());
		}
		return joiner.toString();
	}
	
	public String getPackageName() {
		if (hasNature(Nature.MAVEN)) {
			String groupString = getGroupID().replaceAll("[^a-zA-Z0-9]+", ".");
			String artifactString = getArtifactID().replaceAll("[^a-zA-Z0-9]+", ".");
			if (!groupString.equals("") && !artifactString.equals(""))
				return groupString + "." + artifactString;
		}
		
		String groupID = SyncPom.getMetadataFromApplicationPom(PreferenceConstants.PREF_APP_GROUP_ID);
		String artifactID = getName();
		
		if (groupID != null && !groupID.equals(""))
			return groupID + "." + artifactID;
		return artifactID;
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
		IEclipsePreferences preferences = projectScope.getNode(PREFERENCE_NAME_SERVICE_PROJECT);
		return preferences.getBoolean(PREF_CONTRIBUTE_SHARED_DIR, false);
    }
    
	public void setSharedDirContributionEnabled(boolean isEnabled) {
		try {
			ProjectScope projectScope = new ProjectScope(project);
			IEclipsePreferences preferences = projectScope.getNode(PREFERENCE_NAME_SERVICE_PROJECT);
			preferences.putBoolean(PREF_CONTRIBUTE_SHARED_DIR, isEnabled);
			preferences.flush();
		} catch (Exception e) {
			Console.println("Error saving Preference of " + getName() + ": " + PREF_CONTRIBUTE_SHARED_DIR);
			Console.printStackTrace(e);
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
		IEclipsePreferences preferences = projectScope.getNode(PREFERENCE_NAME_SERVICE_PROJECT);
		return preferences.getBoolean(PREF_ENABLED, false);
	}

	public void setEnabled(boolean isEnabled) {
		try {
			ProjectScope projectScope = new ProjectScope(project);
			IEclipsePreferences preferences = projectScope.getNode(PREFERENCE_NAME_SERVICE_PROJECT);
			preferences.putBoolean(PREF_ENABLED, isEnabled);
			preferences.flush();
		} catch (Exception e) {
			Console.println("Error saving Preference of " + getName() + ": " + PREF_ENABLED);
			Console.printStackTrace(e);
		}
	}
    
    public String getContainerName() {
		ProjectScope projectScope = new ProjectScope(project);
		IEclipsePreferences preferences = projectScope.getNode(PREFERENCE_NAME_SERVICE_PROJECT);
		return preferences.get(PREF_CONTAINER_NAME, getName().toLowerCase());
    }

	public String getNetwork() {
		ProjectScope projectScope = new ProjectScope(project);
		IEclipsePreferences preferences = projectScope.getNode(PREFERENCE_NAME_SERVICE_PROJECT);
		
		ScopedPreferenceStore preferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, PREFERENCE_NAME_GLOBAL);
		String defaultValue = preferenceStore.getString(PreferenceConstants.PREF_DOCKER_NETWORK_NAME);
		
		return preferences.get(PREF_NETWORK, defaultValue);
	}
	
	public void setNetwork(String network) {
		try {
			ProjectScope projectScope = new ProjectScope(project);
			IEclipsePreferences preferences = projectScope.getNode(PREFERENCE_NAME_SERVICE_PROJECT);
			preferences.put(PREF_NETWORK, network);
			preferences.flush();
		} catch (Exception e) {
			Console.println("Error saving Preference of " + getName() + ": " + PREF_NETWORK);
			Console.printStackTrace(e);
		}
	}
	
	public String getAlias() {
		ProjectScope projectScope = new ProjectScope(project);
		IEclipsePreferences preferences = projectScope.getNode(PREFERENCE_NAME_SERVICE_PROJECT);
		return preferences.get(PREF_ALIAS, null);
	}

	public String getPortsRaw() {
		ProjectScope projectScope = new ProjectScope(project);
		IEclipsePreferences preferences = projectScope.getNode(PREFERENCE_NAME_SERVICE_PROJECT);
		String portString = preferences.get(PREF_PORTS, "");
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
		IEclipsePreferences preferences = projectScope.getNode(PREFERENCE_NAME_SERVICE_PROJECT);
		
		StringJoiner joiner = new StringJoiner(SPLIT_STRING);
		for (String port : ports) {
			if (port.equals(""))
				continue;
		    joiner.add(port);
		}
		
		try {
			preferences.put(PREF_PORTS, joiner.toString());
			preferences.flush();
		} catch (Exception e) {
			Console.println("Error saving Preference of " + getName() + ": " + PREF_PORTS);
			Console.printStackTrace(e);
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
