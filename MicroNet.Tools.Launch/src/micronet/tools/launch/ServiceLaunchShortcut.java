package micronet.tools.launch;

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IEditorPart;

import micronet.tools.console.Console;
import micronet.tools.core.ModelProvider;
import micronet.tools.core.ServiceProject;
import micronet.tools.launch.utility.LaunchServiceUtility;

public class ServiceLaunchShortcut implements ILaunchShortcut {

	@Override
	public void launch(ISelection selection, String mode) {
		try {
			if (selection instanceof TreeSelection) {
				TreeSelection treeSelection = (TreeSelection) selection;
				
				for (Object selectedObject : treeSelection.toList()) {
					ServiceProject serviceProject = null;
					if (selectedObject instanceof IProject) {
						IProject project = (IProject) selectedObject;
						serviceProject = ModelProvider.INSTANCE.getServiceProject(project.getName());
					} else if (selectedObject instanceof IJavaProject) {
						IJavaProject project = (IJavaProject) selectedObject;
						serviceProject = ModelProvider.INSTANCE.getServiceProject(project.getProject().getName());
					}
					if (serviceProject == null)
						return;
					LaunchServiceUtility.launchNative(serviceProject, mode);
				}
			}
		} catch (Exception e) {
			Console.print("Error launching service native");
			Console.printStackTrace(e);
		}
	}

	@Override
	public void launch(IEditorPart editor, String mode) {
	}
}
