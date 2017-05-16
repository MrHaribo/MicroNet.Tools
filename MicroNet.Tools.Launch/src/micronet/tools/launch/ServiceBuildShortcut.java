package micronet.tools.launch;

import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IEditorPart;

public class ServiceBuildShortcut implements ILaunchShortcut {

	@Override
	public void launch(ISelection selection, String mode) {
		System.out.println("Build selection: " + mode);

		if (selection instanceof TreeSelection) {

			TreeSelection treeSelection = (TreeSelection) selection;

			for (Object selectedObject : treeSelection.toList()) {
				if (selectedObject instanceof IJavaProject) {
					IJavaProject javaProject = (IJavaProject) selectedObject;

					String projectName = javaProject.getElementName();
					System.out.println("Building: " + projectName);


				}
			}
		}
	}

	@Override
	public void launch(IEditorPart editor, String mode) {
		System.out.println("Editor: " + mode);
	}



	// try {
	// ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
	//
	// ILaunchConfigurationType type =
	// manager.getLaunchConfigurationType("org.eclipse.jdt.launching.localJavaApplication");
	// ILaunchConfiguration[] lcs = manager.getLaunchConfigurations(type);
	//
	// for (ILaunchConfiguration iLaunchConfiguration : lcs) {
	// if (iLaunchConfiguration.getName().equals("Test PThread")) {
	// ILaunchConfigurationWorkingCopy t =
	// iLaunchConfiguration.getWorkingCopy();
	// ILaunchConfiguration config = t.doSave();
	// if (config != null) {
	// // config.launch(ILaunchManager.RUN_MODE, null);
	// DebugUITools.launch(config, ILaunchManager.DEBUG_MODE);
	// }
	// }
	// }
	// } catch (CoreException e) {
	//
	// }
}
