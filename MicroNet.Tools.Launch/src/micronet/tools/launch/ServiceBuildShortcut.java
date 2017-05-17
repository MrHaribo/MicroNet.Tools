package micronet.tools.launch;

import java.util.List;

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

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerCertificateException;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.ListContainersParam;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.messages.Container;

public class ServiceBuildShortcut implements ILaunchShortcut {

	
	@Override
	public void launch(ISelection selection, String mode) {
		System.out.println("Build selection: " + mode);

		if (selection instanceof TreeSelection) {
			TreeSelection treeSelection = (TreeSelection) selection;
			for (Object selectedObject : treeSelection.toList()) {
				
				if (selectedObject instanceof IProject) {
					IProject project = (IProject) selectedObject;
					System.out.println("Building: " + project.getName());
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
		
		final ILaunchManager launchMan = DebugPlugin.getDefault().getLaunchManager();
		
		for (ILaunch launch : launchMan.getLaunches()) {
			if (!launch.getLaunchConfiguration().getName().equals(buildName)) 
				continue;
			
			if (!launch.isTerminated()) {
				System.out.println("Launch is already there");
				showWarningMessageBox(buildName + " is currently building.", "Wait for build to end or terminate build");
				return;
			}
		}
		
		launchMan.addLaunchListener(new ILaunchesListener2()
		{
			@Override
			public void launchesAdded(ILaunch[] arg0) { }

			@Override
			public void launchesChanged(ILaunch[] arg0) { }

			@Override
			public void launchesRemoved(ILaunch[] arg0) { }

			@Override
			public void launchesTerminated(ILaunch[] arg0) {
				for (ILaunch launch : arg0) {
					System.out.println("Launch terminated: " + launch.getLaunchConfiguration().getName());
					if (launch.getLaunchConfiguration().getName().equals(buildName)) {
						System.out.println("Start next build step");
						launchMan.removeLaunchListener(this);
					}
				}
			}
		});
		
		
		ILaunchConfiguration buildConfig = getBuildConfig(project);
		DebugUITools.launch(buildConfig, mode);
		
		//buildContainer();
	}
	
	private void showWarningMessageBox(String text, String message) {
		MessageBox dialog = new MessageBox(Display.getCurrent().getActiveShell(), SWT.ICON_WARNING | SWT.OK);
		dialog.setText(text);
		dialog.setMessage(message);
		dialog.open();
	}
	
	private void buildContainer() {
		try {
			// Create a client based on DOCKER_HOST and DOCKER_CERT_PATH env vars
			final DockerClient docker = DefaultDockerClient.fromEnv().build();
			
			// List all containers. Only running containers are shown by default.
			final List<Container> containers = docker.listContainers(ListContainersParam.allContainers());
			
			System.out.println(containers.size());
		} catch (DockerCertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DockerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private ILaunchConfiguration getBuildConfig(IProject project) {
		String buildName = getBuildName(project);
		try {
			ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
			ILaunchConfigurationType type = manager.getLaunchConfigurationType(MavenLaunchConstants.LAUNCH_CONFIGURATION_TYPE_ID);
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

	private String getBuildName(IProject project) {
		return project.getName() + "Build";
	}
}
