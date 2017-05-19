package micronet.tools.ui.serviceexplorer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.IMavenProjectRegistry;

import micronet.launch.script.SyncPom;

public enum ModelProvider {
	INSTANCE;

	Map<IProject, ServiceProject> serviceProjects = new HashMap<>();
	
	private List<ServicesChangedListener> servicesChangedListeners = new ArrayList<>();
	
	private ModelProvider() {

		refreshServiceProjects();
		
		ResourcesPlugin.getWorkspace().addResourceChangeListener(event -> {
			try {
			IResource res = event.getResource();
				switch (event.getType()) {
					case IResourceChangeEvent.PRE_CLOSE:
						if (res instanceof IProject) {
							System.out.println("Project is about to close: " + res.getFullPath());
							serviceProjects.remove((IProject)res);
							notifyServicesChangedListeners();
						}
						break;
		            case IResourceChangeEvent.PRE_DELETE:
		            	if (res instanceof IProject) {
							System.out.println("Project is about to be deleted: " + res.getFullPath());
							serviceProjects.remove((IProject)res);
							notifyServicesChangedListeners();
		            	}
		            	break;
		            case IResourceChangeEvent.POST_CHANGE:
		            	System.out.println("Resources have changed.");
		            	event.getDelta().accept(new DeltaPrinter());
		            	break;
		            case IResourceChangeEvent.PRE_BUILD:
		            	System.out.println("Build about to run.");
		            	event.getDelta().accept(new DeltaPrinter());
		            	break;
		            case IResourceChangeEvent.POST_BUILD:
		            	System.out.println("Build complete.");
		            	event.getDelta().accept(new DeltaPrinter());
		            	break;
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		},  IResourceChangeEvent.PRE_CLOSE
	      | IResourceChangeEvent.PRE_DELETE
	      | IResourceChangeEvent.PRE_BUILD
	      | IResourceChangeEvent.POST_BUILD
	      | IResourceChangeEvent.POST_CHANGE);
	}

	class DeltaPrinter implements IResourceDeltaVisitor {
		public boolean visit(IResourceDelta delta) {
			IResource res = delta.getResource();
			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				if (res instanceof IProject) {
					System.out.println("Project was added: " + res.getFullPath());
					addProject((IProject)res);
					notifyServicesChangedListeners();
		        }
				break;
			case IResourceDelta.REMOVED:
				if (res instanceof IProject) {
					System.out.println("Project was removed: " + res.getFullPath());
					serviceProjects.remove((IProject)res);
					notifyServicesChangedListeners();
				}
				break;
			case IResourceDelta.CHANGED:
				if (res instanceof IProject) {
					System.out.println("Project has changed: " + res.getFullPath());
					if (!serviceProjects.containsKey((IProject)res))
						addProject((IProject)res);
					notifyServicesChangedListeners();
				}
				break;
			}
			return true; // visit the children
		}
	}
	
	private void refreshServiceProjects() {
		serviceProjects.clear();
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		for (IProject project : workspaceRoot.getProjects()) {
			if(!project.isOpen())
				continue;
			addProject(project);
		}
		
		List<String> enabledServices = SyncPom.getServicesFromGamePom();
		for (ServiceProject project : serviceProjects.values()) {
			boolean projectEnabled = enabledServices.contains(project.getName());
			project.setEnabled(projectEnabled);
		}
	}
	
	private void addProject(IProject project) {
		try {
			if (project.hasNature("org.eclipse.m2e.core.maven2Nature")) {
				IMavenProjectRegistry projectManager = MavenPlugin.getMavenProjectRegistry();
				IMavenProjectFacade mavenProjectFacade = projectManager.getProject(project);
				String version = mavenProjectFacade.getArtifactKey().getVersion();
				serviceProjects.put(project, new ServiceProject(project, version));
			} else if (project.hasNature(JavaCore.NATURE_ID)) {
				serviceProjects.put(project, new ServiceProject(project, null));
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	public List<ServiceProject> getServiceProjects() {
		return new ArrayList<ServiceProject>(serviceProjects.values());
	}
	
	public List<IProject> getEnabledServiceProjects() {
		List<IProject> result = new ArrayList<IProject>();
		for (ServiceProject project : serviceProjects.values()) {
			if (project.isEnabled())
				result.add(project.getProject());
		}
		return result;
	}

	public interface ServicesChangedListener {
		public void onServicesChanged();
	}
	public void registerServicesChangedListener(ServicesChangedListener listener) {
		this.servicesChangedListeners.add(listener);
	}
	public void unregisterServicesChangedListener(ServicesChangedListener listener) {
		this.servicesChangedListeners.remove(listener);
	}
	protected void notifyServicesChangedListeners() {
		this.servicesChangedListeners.forEach(listener -> listener.onServicesChanged());
	}
}
