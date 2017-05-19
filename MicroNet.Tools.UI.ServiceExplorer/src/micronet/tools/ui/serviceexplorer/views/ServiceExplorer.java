package micronet.tools.ui.serviceexplorer.views;


import java.net.URL;
import java.util.List;

import javax.swing.text.View;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import micronet.tools.core.ModelProvider;
import micronet.tools.core.ServiceProject;
import micronet.tools.core.SyncPom;
import micronet.tools.launch.utility.BuildGameMavenUtility;
import micronet.tools.launch.utility.BuildUtility;
import micronet.tools.launch.utility.LaunchServiceGroupUtility;
import micronet.tools.launch.utility.LaunchServiceUtility;


/**
 * This sample class demonstrates how to plug-in a new
 * workbench view. The view shows data obtained from the
 * model. The sample creates a dummy model on the fly,
 * but a real implementation would connect to the model
 * available either in this or another plug-in (e.g. the workspace).
 * The view is connected to the model using a content provider.
 * <p>
 * The view uses a label provider to define how model
 * objects should be presented in the view. Each
 * view can present the same model objects using
 * different labels and icons, if needed. Alternatively,
 * a single label provider can be shared between views
 * in order to ensure that objects of the same type are
 * presented in the same way everywhere.
 * <p>
 */

public class ServiceExplorer extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "micronet.tools.ui.serviceexplorer.views.ServiceExplorer";

	private TableViewer viewer;
	
	private Action debugService;
	private Action runService;
	private Action buildService;
	
	private Action nativeDebugEnabledServices;
	private Action nativeRunEnabledServices;

	private Action generateGamePom;
	private Action generateGameCompose;

	private Action buildGamePom;
	private Action buildGameCompose;
	
	private Action localRunGameCompose;
	private Action localRunGameSwarm;
	
	// fields for your class
	// assumes that you have these two icons
	// in the "icons" folder
	private final ImageDescriptor IMG_CHECKED = getImageDescriptor("checked.png");
	private final ImageDescriptor IMG_UNCHECKED = getImageDescriptor("unchecked.png");
	
	private final ImageDescriptor IMG_DEBUG = getImageDescriptor("debug.png");
	private final ImageDescriptor IMG_RUN = getImageDescriptor("run.png");
	
	private final ImageDescriptor IMG_LAUNCH_GROUP = getImageDescriptor("launch_group.png");
	private final ImageDescriptor IMG_DOCKER = getImageDescriptor("docker.png");
	private final ImageDescriptor IMG_MAVEN = getImageDescriptor("maven.png");
	private final ImageDescriptor IMG_NATIVE_JAVA = getImageDescriptor("native_java.png");
	private final ImageDescriptor IMG_MICRO_NET = getImageDescriptor("micronet_icon.png");
	 
	//SWT.CHECK
	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			return getText(obj);
		}
		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}
		public Image getImage(Object obj) {
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}
	}

/**
	 * The constructor.
	 */
	public ServiceExplorer() {
	}

	public void createPartControl(Composite parent) {
        GridLayout layout = new GridLayout(2, false);
        parent.setLayout(layout);
        Label searchLabel = new Label(parent, SWT.NONE);
        searchLabel.setText("Search: ");
        final Text searchText = new Text(parent, SWT.BORDER | SWT.SEARCH);
        searchText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
        createViewer(parent);
        
		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "MicroNet.Tools.UI.ServiceExplorer.viewer");
		getSite().setSelectionProvider(viewer);
		makeActions();
		hookContextMenu();
		contributeToActionBars();
    }
	
	private void createViewer(Composite parent) {
        viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
        createColumns(parent, viewer);
        final Table table = viewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        viewer.setContentProvider(new ArrayContentProvider());
        // get the content for the viewer, setInput will call getElements in the
        // contentProvider
        viewer.setInput(ModelProvider.INSTANCE.getServiceProjects());
        // make the selection available to other views
        getSite().setSelectionProvider(viewer);
        
		ModelProvider.INSTANCE.registerServicesChangedListener(() -> {
			Display.getDefault().asyncExec(() -> {
				viewer.setInput(ModelProvider.INSTANCE.getServiceProjects());
				viewer.refresh();
			});
		});
        
        // define layout for the viewer
        GridData gridData = new GridData();
        gridData.verticalAlignment = GridData.FILL;
        gridData.horizontalSpan = 2;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        viewer.getControl().setLayoutData(gridData);
    }
	
	public TableViewer getViewer() {
        return viewer;
    }
	
	// create the columns for the table
    private void createColumns(final Composite parent, final TableViewer viewer) {
        String[] titles = { "Enabled", "Service Name", "Version", "Type" };
        int[] bounds = { 60, 200, 150, 100 };

        
        // the status enabled
        TableViewerColumn col = createTableViewerColumn(titles[0], bounds[0], 0, SWT.CENTER);
        col.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return null;
            }

            @Override
            public Image getImage(Object element) {
                if (((ServiceProject) element).isEnabled()) {
                    return IMG_CHECKED.createImage();
                } else {
                    return IMG_UNCHECKED.createImage();
                }
            }
        });
        col.setEditingSupport(new ServiceEnablingSupport(viewer));
        
        
        // first column is for the service name
        col = createTableViewerColumn(titles[1], bounds[1], 1);
        col.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                ServiceProject p = (ServiceProject) element;
                return p.getName();
            }
        });

        // second column is for the version
        col = createTableViewerColumn(titles[2], bounds[2], 2);
        col.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                ServiceProject p = (ServiceProject) element;
                return p.getVersion();
            }
        });

    }
    
    private TableViewerColumn createTableViewerColumn(String title, int bound, final int colNumber) {
    	return createTableViewerColumn(title, bound, colNumber, SWT.NONE);
    }
    private TableViewerColumn createTableViewerColumn(String title, int bound, final int colNumber, int style) {
        final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, style);
        final TableColumn column = viewerColumn.getColumn();
        column.setText(title);
        column.setWidth(bound);
        column.setResizable(true);
        column.setMoveable(true);
        return viewerColumn;
    }

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				ServiceExplorer.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}
	
	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(nativeDebugEnabledServices);
		manager.add(nativeRunEnabledServices);
		manager.add(new Separator());
		manager.add(generateGamePom);
		manager.add(generateGameCompose);
		manager.add(new Separator());
		manager.add(buildGamePom);
		manager.add(buildGameCompose);
		manager.add(new Separator());
		manager.add(localRunGameCompose);
		manager.add(localRunGameSwarm);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(debugService);
		manager.add(runService);
		manager.add(buildService);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(nativeDebugEnabledServices);
		manager.add(nativeRunEnabledServices);
	}

	private void makeActions() {
		buildService = new Action() {
			public void run() {
				//showMessage("Build Service executed: " + getSelectedObject());
				ServiceProject serviceProject = (ServiceProject)getSelectedObject();
				BuildUtility.fullBuild(serviceProject.getProject(), "run");
			}
		};
		buildService.setText("Build Service");
		buildService.setToolTipText("Builds the selected service using Maven and Docker.");
		buildService.setImageDescriptor(IMG_MICRO_NET);
		
		runService = new Action() {
			public void run() {
				//showMessage("Run Service executed: " + getSelectedObject());
				ServiceProject serviceProject = (ServiceProject)getSelectedObject();
				LaunchServiceUtility.launchNative(serviceProject.getProject(), "run");
			}
		};
		runService.setText("Run Service Native");
		runService.setToolTipText("Runs the selected service as native Java application");
		runService.setImageDescriptor(IMG_RUN);
		
		debugService = new Action() {
			public void run() {
				//showMessage("Debug Service executed: " + getSelectedObject());
				ServiceProject serviceProject = (ServiceProject)getSelectedObject();
				LaunchServiceUtility.launchNative(serviceProject.getProject(), "debug");
			}
		};
		debugService.setText("Debug Service Native");
		debugService.setToolTipText("Debugs the selected service as native Java application");
		debugService.setImageDescriptor(IMG_DEBUG);
		
		
		nativeDebugEnabledServices = new Action() {
			public void run() {
				//showMessage("Debug Enabled Services executed");
				List<IProject> enabledProjects = ModelProvider.INSTANCE.getEnabledServiceProjects();
				LaunchServiceGroupUtility.launchNativeGroup(enabledProjects, "debug");
			}
		};
		nativeDebugEnabledServices.setText("Debug Enabled Services Native");
		nativeDebugEnabledServices.setToolTipText("Debugs the enabled services as native Java applications.");
		nativeDebugEnabledServices.setImageDescriptor(IMG_DEBUG);

		nativeRunEnabledServices = new Action() {
			public void run() {
				//showMessage("Run Enabled Services executed");
				List<IProject> enabledProjects = ModelProvider.INSTANCE.getEnabledServiceProjects();
				LaunchServiceGroupUtility.launchNativeGroup(enabledProjects, "run");
			}
		};
		nativeRunEnabledServices.setText("Run Enabled Services Native");
		nativeRunEnabledServices.setToolTipText("Runs the enabled services as native Java applications.");
		nativeRunEnabledServices.setImageDescriptor(IMG_RUN);

		generateGamePom = new Action() {
			public void run() {
				List<IProject> enabledProjects = ModelProvider.INSTANCE.getEnabledServiceProjects();
				SyncPom.updateGamePom(enabledProjects);
				showMessage("Game Pom has been generated from Enabled Services.");
			}
		};
		generateGamePom.setText("Generate Game Pom");
		generateGamePom.setToolTipText("Generates (or updates) the Game Pom File (pom.xml) from the enabled services.");
		generateGamePom.setImageDescriptor(IMG_MAVEN);
		
		generateGameCompose = new Action() {
			public void run() {
				showMessage("Generate Game Compose from Enabled Services executed");
			}
		};
		generateGameCompose.setText("Generate Game Compose");
		generateGameCompose.setToolTipText("Generates (or updates) the Game Compose File (docker-compose.xml) from the enabled services.");
		generateGameCompose.setImageDescriptor(IMG_DOCKER);
		
		buildGamePom = new Action() {
			public void run() {
				
				if (promptQuestion("Update Game Pom", "So you want to update the Game Pom before executing the build?"))
					generateGamePom.run();
				BuildGameMavenUtility.buildGame();
			}
		};
		buildGamePom.setText("Build Game Pom");
		buildGamePom.setToolTipText("Builds the Game Pom File using Maven.");
		buildGamePom.setImageDescriptor(IMG_MAVEN);
		
		buildGameCompose = new Action() {
			public void run() {
				//showMessage("Building the Game Pom File using compose.");
				//SyncPom.getServicesFromGamePom();
			}
		};
		buildGameCompose.setText("Build Game Compose");
		buildGameCompose.setToolTipText("Builds the Game Compose File using the \"docker-compose build\" command.");
		buildGameCompose.setImageDescriptor(IMG_DOCKER);
		
		localRunGameCompose = new Action() {
			public void run() {
				showMessage("Run the Game Compose File as a local compose application.");
			}
		};
		localRunGameCompose.setText("Run Game Local Compose");
		localRunGameCompose.setToolTipText("Runs the Game Compose File (docker-compose.xml) as a local compose application.");
		localRunGameCompose.setImageDescriptor(IMG_DOCKER);
		
		localRunGameSwarm = new Action() {
			public void run() {
				showMessage("Run Game Compose File as a local swarm deployment.");
			}
		};
		localRunGameSwarm.setText("Run Game Local Swarm");
		localRunGameSwarm.setToolTipText("Deploys the Game Compose File (docker-compose.xml) in the local docker swarm. Swarm mode must be enabled.");
		localRunGameSwarm.setImageDescriptor(IMG_DOCKER);

	}
	
	private Object getSelectedObject() {
		ISelection selection = viewer.getSelection();
		return ((IStructuredSelection)selection).getFirstElement();
	}

	private void showMessage(String message) {
		MessageDialog.openInformation(
			viewer.getControl().getShell(),
			"Service Explorer",
			message);
	}
	
	private boolean promptQuestion(String title, String message) {
		return MessageDialog.openQuestion(
			viewer.getControl().getShell(),
			title,
			message);
	}
	
	private static ImageDescriptor getImageDescriptor(String file) {
	    // assume that the current class is called View.java
	    Bundle bundle = FrameworkUtil.getBundle(ServiceExplorer.class);
	    URL url = FileLocator.find(bundle, new Path("icons/" + file), null);
	    return ImageDescriptor.createFromURL(url);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}
