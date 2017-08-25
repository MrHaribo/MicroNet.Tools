package micronet.tools.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
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
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.Bundle;

import micronet.tools.composition.SyncPom;
import micronet.tools.console.Console;
import micronet.tools.core.ServiceProject.Nature;

public enum ModelProvider {
	INSTANCE;

	Map<String, ServiceProject> serviceProjects = Collections.synchronizedMap(new HashMap<>());
	
	private List<ServicesChangedListener> servicesChangedListeners = new ArrayList<>();
	private List<Runnable> modelChangedListeners = new ArrayList<>();
	
	private ModelProvider() {

		refreshServiceProjects();
		notifyServicesChangedListeners();
		
		ResourcesPlugin.getWorkspace().addResourceChangeListener(event -> {
			try {
			IResource res = event.getResource();
				switch (event.getType()) {
					case IResourceChangeEvent.PRE_CLOSE:
						if (res instanceof IProject) {
							serviceProjects.remove(((IProject)res).getName());
							notifyServicesChangedListeners();
						}
						break;
		            case IResourceChangeEvent.PRE_DELETE:
		            	if (res instanceof IProject) {
							serviceProjects.remove(((IProject)res).getName());
							notifyServicesChangedListeners();
		            	}
		            	break;
		            case IResourceChangeEvent.POST_CHANGE:
		            	event.getDelta().accept(new DeltaPrinter());
		            	break;
		            case IResourceChangeEvent.PRE_BUILD:
		            	event.getDelta().accept(new DeltaPrinter());
		            	break;
		            case IResourceChangeEvent.POST_BUILD:
		            	event.getDelta().accept(new DeltaPrinter());
		            	break;
				}
			} catch (Exception e) {
				Console.println("Error in Resources Changed Listener");
				Console.printStackTrace(e);
			}
		},  IResourceChangeEvent.PRE_CLOSE
	      | IResourceChangeEvent.PRE_DELETE
	      | IResourceChangeEvent.PRE_BUILD
	      | IResourceChangeEvent.POST_BUILD
	      | IResourceChangeEvent.POST_CHANGE);
		
		getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				switch (event.getProperty()) {
				case PreferenceConstants.PREF_APP_GROUP_ID:
					SyncPom.updateMetadataInApplicationPom(PreferenceConstants.PREF_APP_GROUP_ID, event.getNewValue().toString());
					break;
				case PreferenceConstants.PREF_APP_ARTIFACT_ID:
					SyncPom.updateMetadataInApplicationPom(PreferenceConstants.PREF_APP_ARTIFACT_ID, event.getNewValue().toString());
					break;
				case PreferenceConstants.PREF_APP_VERSION:
					SyncPom.updateMetadataInApplicationPom(PreferenceConstants.PREF_APP_VERSION, event.getNewValue().toString());
					break;
				}
				
			}
		});
	}

	class DeltaPrinter implements IResourceDeltaVisitor {
		public boolean visit(IResourceDelta delta) {
			IResource res = delta.getResource();
			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				if (res instanceof IProject) {
					addProject((IProject)res);
					notifyServicesChangedListeners();
		        }
				break;
			case IResourceDelta.REMOVED:
				if (res instanceof IProject) {
					serviceProjects.remove(((IProject)res).getName());
					notifyServicesChangedListeners();
				}
				break;
			case IResourceDelta.CHANGED:
				if (res instanceof IProject) {
					if (!serviceProjects.containsKey((IProject)res))
						addProject((IProject)res);
					notifyServicesChangedListeners();
				}
				break;
			}
			return true;
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
		} catch (Exception e) {
			Console.println("Error Building Workspace Projects");
			Console.printStackTrace(e);
		}
	}
	
	private void addProject(IProject project) {
		if (!project.isOpen() || project.getName().equals("External Plug-in Libraries"))
			return;
		try {
			List<Nature> natures = new ArrayList<>();
			if (project.hasNature("org.eclipse.m2e.core.maven2Nature")) {
				natures.add(Nature.MAVEN);
			}
			if (project.hasNature(JavaCore.NATURE_ID)) {
				natures.add(Nature.JAVA);
			} 
			if (project.getFile("Dockerfile").exists()) {
				natures.add(Nature.DOCKER);
			}
			
			if (natures.size() > 0) {
				ServiceProject serviceProject = new ServiceProject(project, natures.toArray(new Nature[natures.size()]));
				serviceProjects.put(project.getName(), serviceProject);
			}
		} catch (Exception e) {
			Console.println("Error Adding Project" + project.getName());
			Console.printStackTrace(e);
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
			SyncPom.createApplicationPom();
		syncArchetypeCatalog();
	}
	
	private void syncArchetypeCatalog() {
		try {
			File file = new File(getWorkspaceDir() + ".metadata/.plugins/org.eclipse.m2e.core/archetypesInfo.xml");
			if (file.exists())
				return;
			
			Bundle bundle = Platform.getBundle("com.github.mrharibo.micronet.tools.core");
			InputStream stream = FileLocator.openStream(bundle, new Path("resources/reference-archetypesInfo.xml"), false);
	
			byte[] buffer = new byte[stream.available()];
			stream.read(buffer);
	
			OutputStream outStream = new FileOutputStream(file);
			outStream.write(buffer);
			outStream.close();
			
			Platform.getBundle("org.eclipse.m2e.core").stop();
			Platform.getBundle("org.eclipse.m2e.core").start();
			
		} catch (Exception e) {
			Console.println("Error Syncing Archetype Catalog");
			Console.printStackTrace(e);
		}
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
	
	public void registerModelChangedListener(Runnable listener) {
		this.modelChangedListeners.add(listener);
	}
	public void unregisterModelChangedListener(Runnable listener) {
		this.modelChangedListeners.remove(listener);
	}
	public void notifyModelChangedListeners() {
		this.modelChangedListeners.forEach(listener -> listener.run());
	}
	
	public IPreferenceStore getPreferenceStore() {
		return new ScopedPreferenceStore(InstanceScope.INSTANCE, PreferenceConstants.PREFERENCE_NAME_GLOBAL);
	}
}
