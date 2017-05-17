package micronet.tools.launch;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IEditorPart;

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
					
					ILaunchConfiguration launchConfig = getLaunchConfiguration(javaProject);
					DebugUITools.launch(launchConfig, mode);
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

	private ILaunchConfiguration getLaunchConfiguration(IJavaProject project) {
		String projectName = project.getElementName();
		try {
			ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
			ILaunchConfigurationType type = manager.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
			ILaunchConfiguration[] configurations = manager.getLaunchConfigurations(type);

			for (ILaunchConfiguration iLaunchConfiguration : configurations) {
				if (iLaunchConfiguration.getName().equals(project))
					return iLaunchConfiguration;
			}

			ILaunchConfigurationWorkingCopy workingCopy = type.newInstance(null, projectName);
			workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, project.getElementName());
			workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, "ServiceImpl");
			ILaunchConfiguration config = workingCopy.doSave();
			return config;
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}
}
