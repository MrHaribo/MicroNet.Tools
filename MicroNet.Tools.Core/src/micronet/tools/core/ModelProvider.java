package micronet.tools.core;

import java.io.File;
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
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import micronet.tools.core.ServiceProject.Nature;

public enum ModelProvider {
	INSTANCE;

	Map<String, ServiceProject> serviceProjects = new HashMap<>();
	
	private List<ServicesChangedListener> servicesChangedListeners = new ArrayList<>();
	
	private ModelProvider() {

		refreshServiceProjects();
		notifyServicesChangedListeners();
		
		ResourcesPlugin.getWorkspace().addResourceChangeListener(event -> {
			try {
			IResource res = event.getResource();
				switch (event.getType()) {
					case IResourceChangeEvent.PRE_CLOSE:
						if (res instanceof IProject) {
							System.out.println("Project is about to close: " + res.getFullPath());
							serviceProjects.remove(((IProject)res).getName());
							notifyServicesChangedListeners();
						}
						break;
		            case IResourceChangeEvent.PRE_DELETE:
		            	if (res instanceof IProject) {
							System.out.println("Project is about to be deleted: " + res.getFullPath());
							serviceProjects.remove(((IProject)res).getName());
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
					serviceProjects.remove(((IProject)res).getName());
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
	
	public void refreshServiceProjects() {
		serviceProjects.clear();
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		for (IProject project : workspaceRoot.getProjects()) {
			if(!project.isOpen())
				continue;

			File removeFile = new File(project.getLocation().append("mn_remove").toString());
			if (removeFile.exists()) {
				continue;
			}
			
			addProject(project);
		}
	}
	
	public void buildServiceProjects() {
		try {
			ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	private void addProject(IProject project) {
		if (!project.isOpen() || project.getName().equals("External Plug-in Libraries"))
			return;
		try {
			ServiceProject serviceProject = null;
			if (project.hasNature("org.eclipse.m2e.core.maven2Nature")) {
				serviceProject = new ServiceProject(project, Nature.JAVA, Nature.MAVEN);
			} else if (project.hasNature(JavaCore.NATURE_ID)) {
				serviceProject = new ServiceProject(project, Nature.JAVA);
			} 
			
			if (project.getFile("Dockerfile").exists()) {
				if (serviceProject == null) {
					serviceProject = new ServiceProject(project, Nature.DOCKER);
				} else {
					serviceProject.addNature(Nature.DOCKER);
				}
			}
			
			if (serviceProject != null) {
				serviceProjects.put(project.getName(), serviceProject);
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	public List<ServiceProject> getServiceProjects() {
		return new ArrayList<ServiceProject>(serviceProjects.values());
	}
	
	public ServiceProject getServiceProject(String name) {
		return serviceProjects.get(name);
	}
	
	public List<ServiceProject> getEnabledServiceProjects() {
		List<ServiceProject> result = new ArrayList<>();
		for (ServiceProject project : serviceProjects.values()) {
			if (project.isEnabled())
				result.add(project);
		}
		return result;
	}
	
	public String getSharedDir() {
		String sharedDir = getWorkspaceDir() + "/shared/";
		File sharedDirFile = new File(sharedDir);
		if (!sharedDirFile.exists())
			sharedDirFile.mkdir();
		return sharedDir;
	}
	
	public String getWorkspaceDir() {
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		return workspaceRoot.getLocation().toOSString() + "/";
	}
	
	public void initWorkspace() {
		if (!isSharedDirPresent())
			new File(getSharedDir()).mkdir();
		if (!isApplicationPomPresent())
			SyncPom.updateMetadataInApplicationPom();
	}
	
	private boolean isSharedDirPresent() {
		return new File(getSharedDir()).exists();
	}
	
	private boolean isApplicationPomPresent() {
		return new File(new Path(getWorkspaceDir()).append("pom.xml").toString()).exists();
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
		refreshServiceProjects();
		this.servicesChangedListeners.forEach(listener -> listener.onServicesChanged());
	}
	
	public IPreferenceStore getPreferenceStore() {
		return new ScopedPreferenceStore(InstanceScope.INSTANCE, "MicroNet.Tools.Preferences");
	}
}
