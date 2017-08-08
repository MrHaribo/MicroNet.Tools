package micronet.tools.core;

import org.eclipse.ui.IStartup;

public class StartHook  implements IStartup {

	@Override
	public void earlyStartup() {
		ModelProvider.INSTANCE.initWorkspace();
	}
}
