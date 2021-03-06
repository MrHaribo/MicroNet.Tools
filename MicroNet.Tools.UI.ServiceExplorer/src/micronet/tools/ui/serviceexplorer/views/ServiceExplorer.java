package micronet.tools.ui.serviceexplorer.views;

import java.io.InputStream;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.ViewPart;

import micronet.tools.composition.SyncCompose;
import micronet.tools.composition.SyncPom;
import micronet.tools.console.Console;
import micronet.tools.core.Icons;
import micronet.tools.core.ModelProvider;
import micronet.tools.core.ServiceProject;
import micronet.tools.core.ServiceProject.Nature;
import micronet.tools.launch.utility.BuildGameComposeUtility;
import micronet.tools.launch.utility.BuildGameMavenUtility;
import micronet.tools.launch.utility.BuildServiceContainerUtility;
import micronet.tools.launch.utility.BuildUtility;
import micronet.tools.launch.utility.LaunchGameComposeUtility;
import micronet.tools.launch.utility.LaunchServiceContainerUtility;
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
	private ServiceTableComperator comparator;

	private Action setNetwork;
	private Action addPorts;
	private Action addEnvArgs;
	
	private Action debugService;
	private Action runService;
	private Action runServiceContainer;
	private Action buildServiceFull;
	private Action buildServiceMaven;
	private Action buildServiceContainer;
	private Action showServiceConfig;
	
	private Action refreshServicesAction;

	private Action nativeDebugEnabledServices;
	private Action nativeRunEnabledServices;

	private Action generateGamePom;
	private Action generateGameCompose;

	private Action buildGamePom;
	private Action buildGameCompose;
	private Action buildGame;

	private Action localRunGameCompose;
	private Action localRunGameSwarm;

	private Action showConsole;
	private Action showPreferences;

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
		
		// To support the search
		ServiceFilter filter = new ServiceFilter();
        searchText.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent ke) {
                filter.setSearchText(searchText.getText());
                viewer.refresh();
            }

        });
        viewer.addFilter(filter);
        
        comparator = new ServiceTableComperator();
        viewer.setComparator(comparator);

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
				if (viewer.getControl().isDisposed())
					return;
				//TODO: Concurrent Modification
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
		String[] titles = { "Enabled", "Service Name", "Version", "Nature", "Game Pom", "Game Compose", "Network", "Ports" };
		int[] bounds = { 60, 200, 150, 150, 80, 80, 120, 120 };

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
					return Icons.IMG_CHECKED.createImage();
				} else {
					return Icons.IMG_UNCHECKED.createImage();
				}
			}
		});
		col.setEditingSupport(new ServiceEnablingSupport(viewer));

		col = createTableViewerColumn(titles[1], bounds[1], 1);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				ServiceProject p = (ServiceProject) element;
				return p.getName();
			}
		});

		col = createTableViewerColumn(titles[2], bounds[2], 2);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				ServiceProject p = (ServiceProject) element;
				return p.getVersion();
			}
		});

		col = createTableViewerColumn(titles[3], bounds[3], 3);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				ServiceProject p = (ServiceProject) element;
				return p.getNatureString();
			}
		});

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
		
		col = createTableViewerColumn(titles[6], bounds[6], 6);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				ServiceProject p = (ServiceProject) element;
				return p.getNetwork();
			}
		});
		
		col = createTableViewerColumn(titles[7], bounds[7], 7);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				ServiceProject p = (ServiceProject) element;
				return p.getPortsRaw();
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
		column.addSelectionListener(getSelectionAdapter(column, colNumber));
		return viewerColumn;
	}
	
	private SelectionAdapter getSelectionAdapter(final TableColumn column,
            final int index) {
        SelectionAdapter selectionAdapter = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                comparator.setColumn(index);
                int dir = comparator.getDirection();
                viewer.getTable().setSortDirection(dir);
                viewer.getTable().setSortColumn(column);
                viewer.refresh();
            }
        };
        return selectionAdapter;
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
		manager.add(generateGameCompose);
		manager.add(new Separator());
		manager.add(buildGame);
		MenuManager individualBuildMenu = new MenuManager("Individual Build Steps");
		individualBuildMenu.add(buildGamePom);
		individualBuildMenu.add(buildGameCompose);
		manager.add(individualBuildMenu);
		manager.add(new Separator());
		manager.add(localRunGameCompose);
		manager.add(localRunGameSwarm);
		manager.add(new Separator());
		manager.add(showConsole);
		manager.add(showPreferences);
		
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(buildServiceFull);
		MenuManager individualBuildMenu = new MenuManager("Individual Build Steps");
		individualBuildMenu.add(buildServiceMaven);
		individualBuildMenu.add(buildServiceContainer);
		manager.add(individualBuildMenu);

		manager.add(new Separator());
		
		if (selectedProject != null && selectedProject.hasNature(Nature.JAVA)) {
			manager.add(debugService);
			manager.add(runService);
		}
		manager.add(runServiceContainer);
		
		manager.add(new Separator());
		manager.add(setNetwork);
		manager.add(addPorts);
		if (selectedProject != null && selectedProject.hasNature(Nature.JAVA)) {
			manager.add(addEnvArgs);
		}
		
		manager.add(new Separator());
		manager.add(showServiceConfig);
		
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(nativeDebugEnabledServices);
		manager.add(nativeRunEnabledServices);
		manager.add(refreshServicesAction);
		manager.add(showConsole);
	}

	private void makeActions() {
		createServiceActions();
		createLanuchGroupActions();
		createGenerateGameActions();
		createBuildGameActions();
		createRunGameActions();
		createSettingsActions();
	}

	
	private void createServiceActions() {
		
		refreshServicesAction = new Action() {
			public void run() {
				ModelProvider.INSTANCE.buildServiceProjects();
			}
		};
		refreshServicesAction.setText("Rebuild Services");
		refreshServicesAction.setToolTipText("Rebuild all service Projects in the Workspace");
		refreshServicesAction.setImageDescriptor(Icons.IMG_BUILD);
		
		buildServiceFull = new Action() {
			public void run() {
				if (selectedProject != null) {
					BuildUtility.buildFull(selectedProject, "run");
				}
			}
		};
		buildServiceFull.setText("Full Service Build");
		buildServiceFull.setToolTipText("Builds the selected service using Maven and Docker.");
		buildServiceFull.setImageDescriptor(Icons.IMG_MICRONET);
		
		buildServiceMaven = new Action() {
			public void run() {
				if (selectedProject != null) {
					BuildUtility.buildMaven(selectedProject, "run");
				}
			}
		};
		buildServiceMaven.setText("Build Maven");
		buildServiceMaven.setToolTipText("Builds the selected service using Maven.");
		buildServiceMaven.setImageDescriptor(Icons.IMG_MAVEN);
		
		buildServiceContainer = new Action() {
			public void run() {
				if (selectedProject != null) {
					InputStream containerStream = BuildServiceContainerUtility.buildContainer(selectedProject);
					if (containerStream == null)
						showMessage("Error starting container: " + selectedProject.getName());
					
					IWorkbenchPage page = getSite().getPage();
					Console.createConsole(selectedProject.getName() + "Build", containerStream, page, Icons.IMG_DOCKER);
				}
			}
		};
		buildServiceContainer.setText("Build Docker");
		buildServiceContainer.setToolTipText("Builds the selected service using Docker.");
		buildServiceContainer.setImageDescriptor(Icons.IMG_DOCKER);

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
		runService.setImageDescriptor(Icons.IMG_RUN);

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
		debugService.setImageDescriptor(Icons.IMG_DEBUG);
		
		runServiceContainer = new Action() {
			public void run() {
				if (selectedProject != null) {
					if (!selectedProject.hasNature(Nature.DOCKER)) {
						showMessage(selectedProject.getName() + " does not have the Docker Nature.");
					} else {
						InputStream containerStream = LaunchServiceContainerUtility.launchContainer(selectedProject);
						if (containerStream == null)
							showMessage("Error starting container: " + selectedProject.getName());
						
						IWorkbenchPage page = getSite().getPage();
						Console.createConsole(selectedProject.getName(), containerStream, page, Icons.IMG_DOCKER);
					}
				}
			}
		};
		runServiceContainer.setText("Run Service Container");
		runServiceContainer.setToolTipText("Runs the selected service as a Docker Container");
		runServiceContainer.setImageDescriptor(Icons.IMG_DOCKER);
		
		setNetwork = new Action() {
			public void run() {
				if (selectedProject != null) {
					showNetworkSelectionDialog(selectedProject);
				}
			}
		};
		setNetwork.setText("Set Network...");
		setNetwork.setToolTipText("Set the Docker Network for the Service.");
		setNetwork.setImageDescriptor(Icons.IMG_DOCKER);
		
		addPorts = new Action() {
			public void run() {
				if (selectedProject != null) {
					showPortSelectionDialog(selectedProject);
				}
			}
		};
		addPorts.setText("Add Ports...");
		addPorts.setToolTipText("Specifies exposed ports of the service.");
		addPorts.setImageDescriptor(Icons.IMG_DOCKER);
		
		addEnvArgs = new Action() {
			public void run() {
				if (selectedProject != null) {
					showEnvArgDialog(selectedProject);
				}
			}
		};
		addEnvArgs.setText("Add Native Env Args...");
		addEnvArgs.setToolTipText("Add environment arguments for native launches.");
		addEnvArgs.setImageDescriptor(Icons.IMG_NATIVE_JAVA);
		
		showServiceConfig = new Action() {
			public void run() {
				if (selectedProject != null) {
					try {
						IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(selectedProject.getName() + "/.settings/com.github.mrharibo.micronet.preferences.prefs"));
						IWorkbenchPage page = getSite().getPage();
						IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(file.getName());
						page.openEditor(new FileEditorInput(file), desc.getId());
					} catch (Exception e) {
						Console.println("Error opening Service Config");
						Console.printStackTrace(e);
					}
				}
			}
		};
		showServiceConfig.setText("Edit Service Config");
		showServiceConfig.setToolTipText("Opens the Service's MicroNet Serrings File.");
		showServiceConfig.setImageDescriptor(Icons.IMG_PROPERTIES);
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
		nativeDebugEnabledServices.setImageDescriptor(Icons.IMG_DEBUG);

		nativeRunEnabledServices = new Action() {
			public void run() {
				// showMessage("Run Enabled Services executed");
				List<ServiceProject> enabledProjects = ModelProvider.INSTANCE.getEnabledServiceProjects();
				LaunchServiceGroupUtility.launchNativeGroup(enabledProjects, "run");
			}
		};
		nativeRunEnabledServices.setText("Run Enabled Services Native");
		nativeRunEnabledServices.setToolTipText("Runs the enabled services as native Java applications.");
		nativeRunEnabledServices.setImageDescriptor(Icons.IMG_RUN);
	}

	private void createGenerateGameActions() {
		generateGamePom = new Action() {
			public void run() {
				List<ServiceProject> enabledProjects = ModelProvider.INSTANCE.getEnabledServiceProjects();
				SyncPom.updateServicesInApplicationPom(enabledProjects);
				ModelProvider.INSTANCE.refreshServiceProjects();
				viewer.refresh();
				showMessage("Game Pom has been generated from Enabled Services.");
			}
		};
		generateGamePom.setText("Generate Game Pom");
		generateGamePom.setToolTipText("Generates (or updates) the Game Pom File (pom.xml) from the enabled services.");
		generateGamePom.setImageDescriptor(Icons.IMG_MAVEN);

		generateGameCompose = new Action() {
			public void run() {
				List<ServiceProject> enabledProjects = ModelProvider.INSTANCE.getEnabledServiceProjects();
				SyncCompose.updateGameCompose(enabledProjects);
				ModelProvider.INSTANCE.refreshServiceProjects();
				viewer.refresh();
				showMessage("Game Compose has been generated from Enabled Services");
			}
		};
		generateGameCompose.setText("Generate Game Compose");
		generateGameCompose.setToolTipText(
				"Generates (or updates) the Game Compose File (docker-compose.xml) from the enabled services.");
		generateGameCompose.setImageDescriptor(Icons.IMG_DOCKER);
	}

	private void createBuildGameActions() {
		buildGamePom = new Action() {
			public void run() {
				BuildGameMavenUtility.buildGame();
			}
		};
		buildGamePom.setText("Build Game Pom");
		buildGamePom.setToolTipText("Builds the Game Pom File using Maven.");
		buildGamePom.setImageDescriptor(Icons.IMG_MAVEN);

		buildGameCompose = new Action() {
			public void run() {
				InputStream buildStream = BuildGameComposeUtility.buildGame();
				if (buildStream == null)
					showMessage("Error starting game-compose-build");
				
				IWorkbenchPage page = getSite().getPage();
				Console.createConsole("game-compose Build", buildStream, page);
			}
		};
		buildGameCompose.setText("Build Game Compose");
		buildGameCompose.setToolTipText("Builds the Game Compose File using the \"docker-compose build\" command.");
		buildGameCompose.setImageDescriptor(Icons.IMG_DOCKER);
		
		buildGame = new Action() {
			public void run() {
				BuildUtility.buildGame();
			}
		};
		buildGame.setText("Build Full Game");
		buildGame.setToolTipText("Builds the Game by performing a Maven Build followed by a Compose build of all services.");
		buildGame.setImageDescriptor(Icons.IMG_MICRONET);
	}

	private void createRunGameActions() {
		localRunGameCompose = new Action() {
			public void run() {
				LaunchGameComposeUtility.launchGame();
				showMessage("Game Compose File started as a local compose application.");
			}
		};
		localRunGameCompose.setText("Run Game Local Compose");
		localRunGameCompose.setToolTipText("Runs the Game Compose File (docker-compose.xml) as a local compose application.");
		localRunGameCompose.setImageDescriptor(Icons.IMG_DOCKER);

		localRunGameSwarm = new Action() {
			public void run() {
				showMessage("Run Game Compose File as a local swarm deployment (Shortcut Not yet implemented, launch from command line in workspace directory: docker stack deploy).");
			}
		};
		localRunGameSwarm.setText("Run Game Local Swarm");
		localRunGameSwarm.setToolTipText("Deploys the Game Compose File (docker-compose.xml) in the local docker swarm. Swarm mode must be enabled.");
		localRunGameSwarm.setImageDescriptor(Icons.IMG_DOCKER);
		localRunGameSwarm.setEnabled(false);
	}
	
	private void createSettingsActions() {
		showConsole = new Action() {
			public void run() {
				Console.showGlobalConsole(getSite().getPage());
			}
		};
		showConsole.setText("MicroNet Console");
		showConsole.setToolTipText("Opens the MicroNet Console which provides framework information.");
		showConsole.setImageDescriptor(Icons.IMG_TERMINAL);
		
		showPreferences = new Action() {
			public void run() {
				PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(viewer.getControl().getShell(), "micronet.tools.preferences.MicroNetSettings", null, null);
				if (pref != null)
					pref.open();
				
				Console.println("hiho");
			}
		};
		showPreferences.setText("MicroNet Preferences");
		showPreferences.setToolTipText("Opens the MicroNet Preferences to configure the workspace.");
		showPreferences.setImageDescriptor(Icons.IMG_PROPERTIES);
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
	
	private void showNetworkSelectionDialog(ServiceProject serviceProject) {
		InputDialog dlg = new InputDialog(viewer.getControl().getShell(), "Select Network", 
				"Select the Docker network you want to connect the container to.", serviceProject.getNetwork(), null);
		if (dlg.open() == Window.OK) {
			String network = dlg.getValue();
			if (network != null) {
				serviceProject.setNetwork(network);
			}
        }
	}
	
	private void showPortSelectionDialog(ServiceProject serviceProject) {
		AddPortsDialog dialog = new AddPortsDialog(viewer.getControl().getShell(), serviceProject);
        // get the new values from the dialog
        if (dialog.open() == Window.OK) {
        	serviceProject.setPorts(dialog.getPorts());
        }
	}
	
	private void showEnvArgDialog(ServiceProject serviceProject) {
		AddEnvArgsDialog dialog = new AddEnvArgsDialog(viewer.getControl().getShell(), serviceProject);
        // get the new values from the dialog
        if (dialog.open() == Window.OK) {
        	serviceProject.setEnvArgs(dialog.getEnvArgs());
        }
	}
	
	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}
