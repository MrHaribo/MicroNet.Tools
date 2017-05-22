package micronet.tools.ui.serviceexplorer.views;

import java.net.URL;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import micronet.tools.core.ModelProvider;
import micronet.tools.core.ServiceProject;
import micronet.tools.core.ServiceProject.Nature;
import micronet.tools.core.SyncCompose;
import micronet.tools.core.SyncPom;
import micronet.tools.launch.utility.BuildGameMavenUtility;
import micronet.tools.launch.utility.BuildUtility;
import micronet.tools.launch.utility.LaunchDependencyUtility;
import micronet.tools.launch.utility.LaunchServiceGroupUtility;
import micronet.tools.launch.utility.LaunchServiceUtility;

/**
 * This sample class demonstrates how to plug-in a new workbench view. The view
 * shows data obtained from the model. The sample creates a dummy model on the
 * fly, but a real implementation would connect to the model available either in
 * this or another plug-in (e.g. the workspace). The view is connected to the
 * model using a content provider.
 * <p>
 * The view uses a label provider to define how model objects should be
 * presented in the view. Each view can present the same model objects using
 * different labels and icons, if needed. Alternatively, a single label provider
 * can be shared between views in order to ensure that objects of the same type
 * are presented in the same way everywhere.
 * <p>
 */

public class ServiceExplorer extends ViewPart implements Listener {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "micronet.tools.ui.serviceexplorer.views.ServiceExplorer";

	private TableViewer viewer;

	private Action dependencyCreateActiveMQ;
	private Action dependencyCreateCouchbase;
	private Action dependencyCreatePostgres;
	private Action dependencyRunActiveMQ;
	private Action dependencyRunCouchbase;

	private Action addLinks;
	private Action addPorts;
	
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
	private final ImageDescriptor IMG_COUCHBASE = getImageDescriptor("couchbase.png");
	private final ImageDescriptor IMG_ACTIVEMQ = getImageDescriptor("activemq.png");
	private final ImageDescriptor IMG_POSTGRESQL = getImageDescriptor("postgreesql.png");

	private ServiceProject selectedProject = null;

	// SWT.CHECK
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
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(),
				"MicroNet.Tools.UI.ServiceExplorer.viewer");
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

		viewer.getTable().addListener(SWT.MenuDetect, this);

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
		String[] titles = { "Enabled", "Service Name", "Version", "Nature", "Game Pom", "Game Compose", "Links" };
		int[] bounds = { 60, 200, 150, 150, 80, 80, 120 };

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

		// second column is for the version
		col = createTableViewerColumn(titles[3], bounds[3], 3);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				ServiceProject p = (ServiceProject) element;
				return p.getNatureString();
			}
		});

		// second column is for the version
		col = createTableViewerColumn(titles[4], bounds[4], 4);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (((ServiceProject) element).isInGamePom()) {
					return "x";
				} else {
					return "";
				}
			}
		});

		// second column is for the version
		col = createTableViewerColumn(titles[5], bounds[5], 5);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (((ServiceProject) element).isInGameCompose()) {
					return "x";
				} else {
					return "";
				}
			}
		});
		
		// second column is for the version
		col = createTableViewerColumn(titles[6], bounds[6], 6);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				ServiceProject p = (ServiceProject) element;
				return p.getLinksRaw();
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
				if (selectedProject != null) {
					ServiceExplorer.this.fillContextMenu(manager);
				}
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
		manager.add(buildGamePom);
		manager.add(new Separator());
		manager.add(generateGameCompose);
		manager.add(buildGameCompose);
		manager.add(new Separator());
		manager.add(localRunGameCompose);
		manager.add(localRunGameSwarm);
		manager.add(new Separator());
		
        MenuManager subMenu = new MenuManager("Dependencies", null);
        subMenu.add(dependencyCreateActiveMQ);
        subMenu.add(dependencyCreateCouchbase);
        subMenu.add(dependencyCreatePostgres);
        manager.add(subMenu);
		
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(debugService);
		manager.add(runService);
		manager.add(buildService);
		manager.add(new Separator());
		manager.add(addLinks);
		manager.add(addPorts);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(nativeDebugEnabledServices);
		manager.add(nativeRunEnabledServices);
		manager.add(dependencyRunActiveMQ);
		manager.add(dependencyRunCouchbase);
	}

	private void makeActions() {
		createDependencyActions();
		createServiceActions();
		createNativeActions();
		createLanuchGroupActions();
		createGenerateGameActions();
		createBuildGameActions();
		createRunGameActions();

	}

	
	private void createServiceActions() {
		addLinks = new Action() {
			public void run() {
				if (selectedProject != null) {
					showLinkSelectionDialog(selectedProject);
				}
				
			}
		};
		addLinks.setText("Add Links...");
		addLinks.setToolTipText("Adds Links to other services to the service.");
		addLinks.setImageDescriptor(IMG_DOCKER);
		
		addPorts = new Action() {
			public void run() {
				ListSelectionDialog dlg = new ListSelectionDialog(
						Display.getCurrent().getActiveShell(),
						new String[] { "Enabled", "Service Name", "Version", "Nature", "Game Pom", "Game Compose" },
			       		new BaseWorkbenchContentProvider(),
			       		new WorkbenchLabelProvider(),
						"Select the resources to save:");
		        //dlg.setInitialSelections(null);
		        dlg.setTitle("Save Resources");
		        dlg.open();
			}
		};
		addPorts.setText("Add Ports...");
		addPorts.setToolTipText("Specifies exposed ports of the service.");
		addPorts.setImageDescriptor(IMG_DOCKER);
	}

	private void createDependencyActions() {
		dependencyCreateActiveMQ = new Action() {
			public void run() {
				showMessage("Add AMQ");
			}
		};
		dependencyCreateActiveMQ.setText("Add ActiveMQ");
		dependencyCreateActiveMQ.setToolTipText("Adds the ActiveMQ dependency to the workspace.");
		dependencyCreateActiveMQ.setImageDescriptor(IMG_ACTIVEMQ);
		
		dependencyCreateCouchbase = new Action() {
			public void run() {
				showMessage("Add Couchbase");
			}
		};
		dependencyCreateCouchbase.setText("Add Couchbase");
		dependencyCreateCouchbase.setToolTipText("Adds the Couchbase dependency to the workspace.");
		dependencyCreateCouchbase.setImageDescriptor(IMG_COUCHBASE);
		
		dependencyCreatePostgres = new Action() {
			public void run() {
				showMessage("Add Postgres");
			}
		};
		dependencyCreatePostgres.setText("Add PostgreSQL Service");
		dependencyCreatePostgres.setToolTipText("Adds a PostgreSQL instance to the workspace.");
		dependencyCreatePostgres.setImageDescriptor(IMG_POSTGRESQL);
		
		dependencyRunActiveMQ = new Action() {
			public void run() {
				showMessage("Run ActiveMQ");
			}
		};
		dependencyRunActiveMQ.setText("Run ActiveMQ as a service");
		dependencyRunActiveMQ.setToolTipText("Run ActiveMQ as a service in a docker container.");
		dependencyRunActiveMQ.setImageDescriptor(IMG_ACTIVEMQ);
		
		dependencyRunCouchbase = new Action() {
			public void run() {
				LaunchDependencyUtility.launchCouchbase();
				showMessage("Couchbase docker container started");
			}
		};
		dependencyRunCouchbase.setText("Run Couchbase as a service");
		dependencyRunCouchbase.setToolTipText("Run Couchbase as a service in a docker container.");
		dependencyRunCouchbase.setImageDescriptor(IMG_COUCHBASE);
	}

	private void createNativeActions() {
		buildService = new Action() {
			public void run() {
				if (selectedProject != null) {
					BuildUtility.fullBuild(selectedProject, "run");
				}
			}
		};
		buildService.setText("Build Service");
		buildService.setToolTipText("Builds the selected service using Maven and Docker.");
		buildService.setImageDescriptor(IMG_MICRO_NET);

		runService = new Action() {
			public void run() {
				if (selectedProject != null) {
					if (!selectedProject.hasNature(Nature.JAVA))
						showMessage(selectedProject.getName() + " is not a Java Project.");
					else
						LaunchServiceUtility.launchNative(selectedProject, "run");
				}
			}
		};
		runService.setText("Run Service Native");
		runService.setToolTipText("Runs the selected service as native Java application");
		runService.setImageDescriptor(IMG_RUN);

		debugService = new Action() {
			public void run() {
				if (selectedProject != null) {
					if (!selectedProject.hasNature(Nature.JAVA))
						showMessage(selectedProject.getName() + " is not a Java Project.");
					else
						LaunchServiceUtility.launchNative(selectedProject, "debug");
				}
			}
		};
		debugService.setText("Debug Service Native");
		debugService.setToolTipText("Debugs the selected service as native Java application");
		debugService.setImageDescriptor(IMG_DEBUG);
	}

	private void createLanuchGroupActions() {
		nativeDebugEnabledServices = new Action() {
			public void run() {
				// showMessage("Debug Enabled Services executed");
				List<ServiceProject> enabledProjects = ModelProvider.INSTANCE.getEnabledServiceProjects();
				LaunchServiceGroupUtility.launchNativeGroup(enabledProjects, "debug");
			}
		};
		nativeDebugEnabledServices.setText("Debug Enabled Services Native");
		nativeDebugEnabledServices.setToolTipText("Debugs the enabled services as native Java applications.");
		nativeDebugEnabledServices.setImageDescriptor(IMG_DEBUG);

		nativeRunEnabledServices = new Action() {
			public void run() {
				// showMessage("Run Enabled Services executed");
				List<ServiceProject> enabledProjects = ModelProvider.INSTANCE.getEnabledServiceProjects();
				LaunchServiceGroupUtility.launchNativeGroup(enabledProjects, "run");
			}
		};
		nativeRunEnabledServices.setText("Run Enabled Services Native");
		nativeRunEnabledServices.setToolTipText("Runs the enabled services as native Java applications.");
		nativeRunEnabledServices.setImageDescriptor(IMG_RUN);
	}

	private void createGenerateGameActions() {
		generateGamePom = new Action() {
			public void run() {
				List<ServiceProject> enabledProjects = ModelProvider.INSTANCE.getEnabledServiceProjects();
				SyncPom.updateGamePom(enabledProjects);
				ModelProvider.INSTANCE.refreshServiceProjects();
				showMessage("Game Pom has been generated from Enabled Services.");
			}
		};
		generateGamePom.setText("Generate Game Pom");
		generateGamePom.setToolTipText("Generates (or updates) the Game Pom File (pom.xml) from the enabled services.");
		generateGamePom.setImageDescriptor(IMG_MAVEN);

		generateGameCompose = new Action() {
			public void run() {
				List<ServiceProject> enabledProjects = ModelProvider.INSTANCE.getEnabledServiceProjects();
				SyncCompose.updateGameCompose(enabledProjects);
				ModelProvider.INSTANCE.refreshServiceProjects();
				showMessage("Generate Game Compose from Enabled Services executed");
			}
		};
		generateGameCompose.setText("Generate Game Compose");
		generateGameCompose.setToolTipText(
				"Generates (or updates) the Game Compose File (docker-compose.xml) from the enabled services.");
		generateGameCompose.setImageDescriptor(IMG_DOCKER);
	}

	private void createBuildGameActions() {
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
				showMessage("Building the Game Pom File using compose (Shortcut Not yet implemented, launch from command line in workspace directory: docker-compose build).");
			}
		};
		buildGameCompose.setText("Build Game Compose");
		buildGameCompose.setToolTipText("Builds the Game Compose File using the \"docker-compose build\" command.");
		buildGameCompose.setImageDescriptor(IMG_DOCKER);
	}

	private void createRunGameActions() {
		localRunGameCompose = new Action() {
			public void run() {
				showMessage("Game Compose File started as a local compose application.");
			}
		};
		localRunGameCompose.setText("Run Game Local Compose");
		localRunGameCompose.setToolTipText("Runs the Game Compose File (docker-compose.xml) as a local compose application.");
		localRunGameCompose.setImageDescriptor(IMG_DOCKER);

		localRunGameSwarm = new Action() {
			public void run() {
				showMessage("Run Game Compose File as a local swarm deployment (Shortcut Not yet implemented, launch from command line in workspace directory: docker stack deploy).");
			}
		};
		localRunGameSwarm.setText("Run Game Local Swarm");
		localRunGameSwarm.setToolTipText("Deploys the Game Compose File (docker-compose.xml) in the local docker swarm. Swarm mode must be enabled.");
		localRunGameSwarm.setImageDescriptor(IMG_DOCKER);
	}

	@Override
	public void handleEvent(Event event) {
		Table table = viewer.getTable();

		// calculate click offset within table area
		Point point = Display.getDefault().map(null, table, new Point(event.x, event.y));
		ViewerCell cell = viewer.getCell(point);
		if (cell != null) {
			selectedProject = (ServiceProject) cell.getElement();
		} else {
			selectedProject = null;
		}
	}

	private void showMessage(String message) {
		MessageDialog.openInformation(viewer.getControl().getShell(), "Service Explorer", message);
	}

	private boolean promptQuestion(String title, String message) {
		return MessageDialog.openQuestion(viewer.getControl().getShell(), title, message);
	}

	private String promptName(String title, String message) {
		InputDialog dlg = new InputDialog(Display.getCurrent().getActiveShell(), title, message, "YourDatabaseName", null);
		if (dlg.open() == Window.OK) {
			return dlg.getValue();
		}
		return null;
	}
	
	private void showLinkSelectionDialog(ServiceProject serviceProject) {
		AddLinksDialog dialog = new AddLinksDialog(viewer.getControl().getShell(), serviceProject);
        // get the new values from the dialog
        if (dialog.open() == Window.OK) {
        	serviceProject.setLinks(dialog.getCurrentLinks());
        }
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
