package micronet.tools.launch;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IEditorPart;

import micronet.tools.launch.utility.LaunchServiceUtility;

public class ServiceLaunchShortcut implements ILaunchShortcut {

	@Override
	public void launch(ISelection selection, String mode) {
		System.out.println("Launch selection: " + mode);
		try {
			if (selection instanceof TreeSelection) {
				TreeSelection treeSelection = (TreeSelection) selection;
				
				for (Object selectedObject : treeSelection.toList()) {
					IJavaProject javaProject = null;
					
					if (selectedObject instanceof IJavaProject) {
						javaProject = (IJavaProject) selectedObject;
					} else if (selectedObject instanceof IProject) {
						IProject project = (IProject) selectedObject;
						if (project.hasNature(JavaCore.NATURE_ID))
							javaProject = JavaCore.create(project);
					}
					
					if (javaProject == null)
						throw new Exception("Unknown Project Type");
	
					String projectName = javaProject.getElementName();
					System.out.println("Launching: " + projectName);
					
					LaunchServiceUtility.launchNative(javaProject, mode);
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void launch(IEditorPart editor, String mode) {
		System.out.println("Editor: " + mode);
	}
}
