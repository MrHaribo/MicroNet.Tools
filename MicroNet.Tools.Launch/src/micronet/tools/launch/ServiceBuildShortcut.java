package micronet.tools.launch;

import java.util.function.Consumer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.m2e.actions.MavenLaunchConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IEditorPart;

public class ServiceBuildShortcut implements ILaunchShortcut {

	@Override
	public void launch(ISelection selection, String mode) {
		System.out.println("Build selection: " + mode);

		if (selection instanceof TreeSelection) {
			TreeSelection treeSelection = (TreeSelection) selection;
			for (Object selectedObject : treeSelection.toList()) {

				if (selectedObject instanceof IProject) {
					IProject project = (IProject) selectedObject;
					buildProject(project, mode);
				} else if (selectedObject instanceof IJavaProject) {
					IJavaProject javaProject = (IJavaProject) selectedObject;
					IProject project = javaProject.getProject();
					buildProject(project, mode);
				}
			}
		}
	}

	@Override
	public void launch(IEditorPart arg0, String arg1) {
		System.out.println("Not implemented");
	}

	private void buildProject(IProject project, String mode) {
		String buildName = getBuildName(project);
		System.out.println("Building: " + buildName);

		if (isLaunchRunning(buildName)) {
			System.out.println("Launch is already there");
			showWarningMessageBox(buildName + " is currently building.", "Wait for build to end or terminate build");
			return;
		}
		
		waitForLaunchTermination(buildName, launch -> {
			System.out.println("Maven build launch terminated: " + launch.getLaunchConfiguration().getName());
			System.out.println("Start next build step");
			buildContainer(project, mode);
		});
		
		ILaunchConfiguration buildConfig = getMavenBuildConfig(project);
		DebugUITools.launch(buildConfig, mode);
	}
	
	private void buildContainer(IProject project, String mode) {
		String containerBuildName = getBuildContainerName(project);
		System.out.println("Building: " + containerBuildName);
		
		ILaunchConfiguration buildConfig = getContainerBuildConfig(project, mode);
		DebugUITools.launch(buildConfig, mode);
	}
	


	private ILaunchConfiguration getContainerBuildConfig(IProject project, String mode) {
		String containerBuildName = getBuildContainerName(project);
		
		try {
			ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
			ILaunchConfigurationType type = manager.getLaunchConfigurationType("org.eclipse.linuxtools.docker.ui.buildDockerImageLaunchConfigurationType");
			ILaunchConfiguration[] configurations = manager.getLaunchConfigurations(type);

			for (ILaunchConfiguration iLaunchConfiguration : configurations) {
				if (iLaunchConfiguration.getName().equals(containerBuildName))
					return iLaunchConfiguration;
			}
			
			ILaunchConfigurationWorkingCopy workingCopy = type.newInstance(null, containerBuildName);
			workingCopy.setAttribute("dockerConnection", "http://127.0.0.1:2375");
			workingCopy.setAttribute("repoName", project.getName().toLowerCase());
			workingCopy.setAttribute("sourcePathLocation", "/" + project.getName());
			workingCopy.setAttribute("sourcePathWorkspaceRelativeLocation", true);
			
			ILaunchConfiguration config = workingCopy.doSave();
			return config;
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}

	private ILaunchConfiguration getMavenBuildConfig(IProject project) {
		String buildName = getBuildName(project);
		try {
			ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
			ILaunchConfigurationType type = manager
					.getLaunchConfigurationType(MavenLaunchConstants.LAUNCH_CONFIGURATION_TYPE_ID);
			ILaunchConfiguration[] configurations = manager.getLaunchConfigurations(type);

			for (ILaunchConfiguration iLaunchConfiguration : configurations) {
				if (iLaunchConfiguration.getName().equals(buildName))
					return iLaunchConfiguration;
			}

			ILaunchConfigurationWorkingCopy workingCopy = type.newInstance(null, buildName);
			workingCopy.setAttribute(MavenLaunchConstants.ATTR_POM_DIR, "${project_loc:" + project.getName() + "}");
			workingCopy.setAttribute(MavenLaunchConstants.ATTR_GOALS, "install");
			workingCopy.setAttribute(MavenLaunchConstants.ATTR_UPDATE_SNAPSHOTS, true);
			workingCopy.setAttribute(MavenLaunchConstants.ATTR_RUNTIME, "EMBEDDED");
			ILaunchConfiguration config = workingCopy.doSave();
			return config;
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void waitForLaunchTermination(String launchName, Consumer<ILaunch> callback) {
		final ILaunchManager launchMan = DebugPlugin.getDefault().getLaunchManager();
		launchMan.addLaunchListener(new ILaunchesListener2() {

			public void launchesAdded(ILaunch[] arg0) {	}
			public void launchesChanged(ILaunch[] arg0) { }
			public void launchesRemoved(ILaunch[] arg0) { }

			@Override
			public void launchesTerminated(ILaunch[] arg0) {
				for (ILaunch launch : arg0) {
					if (launch.getLaunchConfiguration().getName().equals(launchName)) {
						launchMan.removeLaunchListener(this);
						ILaunch launchCaptcha = launch;
						callback.accept(launchCaptcha);
					}
				}
			}
		});
	}
	
	private boolean isLaunchRunning(String name) {
		final ILaunchManager launchMan = DebugPlugin.getDefault().getLaunchManager();

		for (ILaunch launch : launchMan.getLaunches()) {
			if (!launch.getLaunchConfiguration().getName().equals(name))
				continue;
			if (!launch.isTerminated())
				return true;
		}
		return false;
	}

	private String getBuildName(IProject project) {
		return project.getName() + "Build";
	}
	
	private String getBuildContainerName(IProject project) {
		return project.getName() + "ContainerBuild";
	}
	
	private void showWarningMessageBox(String text, String message) {
		if (Display.getCurrent() == null || Display.getCurrent().getActiveShell() == null)
			return;
		MessageBox dialog = new MessageBox(Display.getCurrent().getActiveShell(), SWT.ICON_WARNING | SWT.OK);
		dialog.setText(text);
		dialog.setMessage(message);
		dialog.open();
	}
}
