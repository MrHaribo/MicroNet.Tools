package micronet.tools.core;

/**
 * Constant definitions for plug-in preferences
 */
public class PreferenceConstants {

	public static final String SPLIT_STRING = ",";
	public static final String PREFERENCE_NAME_SERVICE_PROJECT = "com.github.mrharibo.micronet.preferences";
	public static final String PREFERENCE_NAME_GLOBAL = "MicroNet.Tools.Preferences";

	//Global Settings
	public static final String PREF_DOCKER_TOOLBOX_PATH = "dockerToolboxPathPreference";
	public static final String PREF_USE_DOCKER_TOOLBOX = "useDockerToolboxPreference";
	public static final String PREF_DOCKER_NETWORK_NAME = "dockerNetworkName";
	public static final String PREF_APP_GROUP_ID = "appGroupID";
	public static final String PREF_APP_ARTIFACT_ID = "appArtifactID";
	public static final String PREF_APP_VERSION = "appVersion";

	//Project Specific Settings
	public static final String PREF_PORTS = "ports";
	public static final String PREF_NETWORK = "network";
	public static final String PREF_ALIAS = "alias";
	public static final String PREF_CONTAINER_NAME = "container.name";
	public static final String PREF_ENABLED = "enabled";
	public static final String PREF_CONTRIBUTE_SHARED_DIR = "contribute.shared.dir";
}
