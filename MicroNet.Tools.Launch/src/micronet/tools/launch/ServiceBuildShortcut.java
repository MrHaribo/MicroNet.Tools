package micronet.tools.launch;

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IEditorPart;

import micronet.tools.launch.utility.BuildUtility;

public class ServiceBuildShortcut implements ILaunchShortcut {

	@Override
	public void launch(ISelection selection, String mode) {
		System.out.println("Build selection: " + mode);

		if (selection instanceof TreeSelection) {
			TreeSelection treeSelection = (TreeSelection) selection;
			for (Object selectedObject : treeSelection.toList()) {

				if (selectedObject instanceof IProject) {
					IProject project = (IProject) selectedObject;
					BuildUtility.fullBuild(project, mode);
				} else if (selectedObject instanceof IJavaProject) {
					IJavaProject javaProject = (IJavaProject) selectedObject;
					IProject project = javaProject.getProject();
					BuildUtility.fullBuild(project, mode);
				}
			}
		}
	}

	@Override
	public void launch(IEditorPart arg0, String arg1) {
		System.out.println("Not implemented");
	}
}
