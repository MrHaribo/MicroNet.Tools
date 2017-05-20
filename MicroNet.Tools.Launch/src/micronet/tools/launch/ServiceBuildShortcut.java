package micronet.tools.launch;

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IEditorPart;

import micronet.tools.core.ModelProvider;
import micronet.tools.core.ServiceProject;
import micronet.tools.launch.utility.BuildUtility;

public class ServiceBuildShortcut implements ILaunchShortcut {

	@Override
	public void launch(ISelection selection, String mode) {
		System.out.println("Build selection: " + mode);

		if (selection instanceof TreeSelection) {
			TreeSelection treeSelection = (TreeSelection) selection;
			for (Object selectedObject : treeSelection.toList()) {

				ServiceProject serviceProject = null;
				if (selectedObject instanceof IProject) {
					IProject project = (IProject) selectedObject;
					serviceProject = ModelProvider.INSTANCE.getServiceProject(project.getName());
					BuildUtility.fullBuild(serviceProject, mode);
				} else if (selectedObject instanceof IJavaProject) {
					IJavaProject javaProject = (IJavaProject) selectedObject;
					IProject project = javaProject.getProject();
					serviceProject = ModelProvider.INSTANCE.getServiceProject(project.getName());
				}
				
				if (serviceProject != null) {
					BuildUtility.fullBuild(serviceProject, mode);
				}
			}
		}
	}

	@Override
	public void launch(IEditorPart arg0, String arg1) {
		System.out.println("Not implemented");
	}
}
