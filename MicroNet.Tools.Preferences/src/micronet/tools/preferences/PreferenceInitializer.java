package micronet.tools.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import micronet.tools.core.ModelProvider;
import micronet.tools.core.PreferenceConstants;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		
		IPreferenceStore store = ModelProvider.INSTANCE.getPreferenceStore();
		store.setDefault(PreferenceConstants.USE_DOCKER_TOOLBOX, false);
		store.setDefault(PreferenceConstants.DOCKER_NETWORK_NAME, "bridge");
		store.setDefault(PreferenceConstants.APP_GROUP_ID, "MyGame");
		store.setDefault(PreferenceConstants.APP_ARTIFACT_ID, "MyGameApp");
		store.setDefault(PreferenceConstants.APP_VERSION, "0.0.1-SNAPSHOT");
		
	}

}
