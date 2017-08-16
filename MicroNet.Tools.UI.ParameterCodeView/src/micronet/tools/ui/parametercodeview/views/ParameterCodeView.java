package micronet.tools.ui.parametercodeview.views;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import micronet.tools.api.ServiceAPI;
import micronet.tools.console.Console;
import micronet.tools.core.Icons;
import micronet.tools.core.ModelProvider;
import micronet.tools.filesync.SyncParameterCodes;
import micronet.tools.filesync.SyncServiceAPI;
import micronet.tools.ui.parametercodeview.WatchDir;
import micronet.tools.ui.parametercodeview.WatchDir.DirChangedListener;

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

public class ParameterCodeView extends ViewPart implements DirChangedListener {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "micronet.tools.ui.parametercodeview.views.SampleView";

	private TableViewer viewer;
	private Action addParameterCodeAction;
	private Action removeParameterCodeAction;
	
	private Action refreshServicesAction;
	private Action showConsole;
	
	private Map<String, Set<String>> requiredParameters;
	private Map<String, Set<String>> providedParameters;

	/**
	 * The constructor.
	 */
	public ParameterCodeView() {
		System.out.println("Test");
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(true);

		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setInput(new String[] { "One", "Two", "Three" });
		
		TableViewerColumn column = new TableViewerColumn(viewer, SWT.LEFT);
		column.getColumn().setText("ParameterCode");
		column.getColumn().setWidth(100);
		column.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public Image getImage(Object element) {
				
				if (SyncParameterCodes.getBuiltInParameterCodes().contains(element))
						return Icons.IMG_PARAM_GREY.createImage();
				return Icons.IMG_PARAM.createImage();
			}
	        @Override
	        public String getText(Object element) {
	            return element.toString();
	        }
	    });
		
		column = new TableViewerColumn(viewer, SWT.LEFT);
		column.getColumn().setText("Required By");
		column.getColumn().setWidth(150);
		column.setLabelProvider(new ColumnLabelProvider() {
	        @Override
	        public String getText(Object element) {
	        	if (requiredParameters != null && !requiredParameters.containsKey(element.toString()))
	        		return null;
				return String.join(",", requiredParameters.get(element.toString()));
	        }
	    });
		
		column = new TableViewerColumn(viewer, SWT.LEFT);
		column.getColumn().setText("Provided By");
		column.getColumn().setWidth(150);
		column.setLabelProvider(new ColumnLabelProvider() {
	        @Override
	        public String getText(Object element) {
	        	if (providedParameters != null && !providedParameters.containsKey(element.toString()))
	        		return null;
	            return String.join(",", providedParameters.get(element.toString()));
	        }
	    });

		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(),
				"MicroNet.Tools.UI.ParameterCodeView.viewer");
		getSite().setSelectionProvider(viewer);
		makeActions();
		hookContextMenu();
		contributeToActionBars();

		String sharedDir = ModelProvider.INSTANCE.getSharedDir();

		Thread watchDirThread = new Thread(new Runnable() {
			@Override
			public void run() {
				System.out.println("WatchDir Thread Start");
				try {
					Path path = Paths.get(sharedDir);
					WatchDir watchDir = new WatchDir(path, true);
					watchDir.registerDirChangedListener(ParameterCodeView.this);
					watchDir.processEvents();
				} catch (IOException e) {
					e.printStackTrace();
				}
				System.out.println("WatchDir Thread End");
			}
		});

		watchDirThread.start();

		parent.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				watchDirThread.interrupt();
				System.out.println("Eclipse Close Event !");
			}
		});

		dirChanged(null, null);
	}

	@Override
	public void dirChanged(String event, Path path) {
		Display.getDefault().asyncExec(() -> {
			if (viewer.getControl().isDisposed())
				return;
			
			String sharedDir = ModelProvider.INSTANCE.getSharedDir();
			Set<String> builtInParameterCodes = SyncParameterCodes.getBuiltInParameterCodes();
			Set<String> userParameterCodes = SyncParameterCodes.getUserParameterCodes(sharedDir);
			
			List<String> orderedParameterCodes = new ArrayList<>(builtInParameterCodes);
			orderedParameterCodes.addAll(userParameterCodes);
			String[] codeArray = orderedParameterCodes.toArray(new String[orderedParameterCodes.size()]);

			refreshUsedParameters(sharedDir);
			
			viewer.setInput(codeArray);
			viewer.refresh();
		});
	}

	private void refreshUsedParameters(String sharedDir) {
		List<ServiceAPI> completeAPI = SyncServiceAPI.readServiceApi(sharedDir);
		
		requiredParameters = SyncParameterCodes.getAllRequiredParameters(completeAPI);
		providedParameters = SyncParameterCodes.getAllProvidedParameters(completeAPI);
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				ParameterCodeView.this.fillContextMenu(manager);
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
		manager.add(addParameterCodeAction);
		manager.add(removeParameterCodeAction);
		manager.add(new Separator());
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(addParameterCodeAction);
		manager.add(removeParameterCodeAction);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(addParameterCodeAction);
		manager.add(removeParameterCodeAction);
		manager.add(refreshServicesAction);
		manager.add(showConsole);
	}

	private void makeActions() {
		addParameterCodeAction = new Action() {
			public void run() {
				String newParameterName = promptName("Add ParameterCode", "NEW_CODE",
						"Add a new ParameterCode to the Workspace");
				if (newParameterName != null) {
					String sharedDir = ModelProvider.INSTANCE.getSharedDir();
					SyncParameterCodes.contributeParameters(new HashSet<String>(Arrays.asList(newParameterName)),
							sharedDir);
				}
			}
		};
		addParameterCodeAction.setText("Add Parameter Code");
		addParameterCodeAction.setToolTipText("Adds a new Parameter Code to the Workspace");
		addParameterCodeAction.setImageDescriptor(Icons.IMG_ADD);

		removeParameterCodeAction = new Action() {
			public void run() {

				StringBuilder messageString = new StringBuilder();
				Set<String> removedParameters = new HashSet<>();
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				
				Set<String> builtInParameterCodes = SyncParameterCodes.getBuiltInParameterCodes();
				for (Object o : selection.toList()) {
					if (builtInParameterCodes.contains(o)) {
						MessageDialog.openError(viewer.getControl().getShell(), "Error removing Parameter Codes", "Cannot remove Built-In ParameterCodes");
						return;
					}
					
					removedParameters.add(o.toString());
					messageString.append(o.toString() + ",");
				}
				if (promptQuestion("Remove ParameterCodes", "You really want to remove: " + messageString + " ?")) {
					String sharedDir = ModelProvider.INSTANCE.getSharedDir();
					SyncParameterCodes.removeParameters(removedParameters, sharedDir);
				}
			}
		};
		removeParameterCodeAction.setText("Remove Parameter Codes");
		removeParameterCodeAction.setToolTipText(
				"Removes the selected Parameter Codes from the Workspace. (Only possible when no references remain)");
		removeParameterCodeAction.setImageDescriptor(Icons.IMG_REMOVE);
		
		refreshServicesAction = new Action() {
			public void run() {
				ModelProvider.INSTANCE.buildServiceProjects();
			}
		};
		refreshServicesAction.setText("Rebuild Services");
		refreshServicesAction.setToolTipText("Rebuild all service Projects in the Workspace");
		refreshServicesAction.setImageDescriptor(Icons.IMG_BUILD);
		
		showConsole = new Action() {
			public void run() {
				Console.showGlobalConsole(getSite().getPage());
			}
		};
		showConsole.setText("MicroNet Console");
		showConsole.setToolTipText("Opens the MicroNet Console which provides framework information.");
		showConsole.setImageDescriptor(Icons.IMG_TERMINAL);
	}

	private boolean promptQuestion(String title, String message) {
		return MessageDialog.openQuestion(viewer.getControl().getShell(), title, message);
	}

	private String promptName(String title, String initialValue, String message) {
		InputDialog dlg = new InputDialog(Display.getCurrent().getActiveShell(), title, message, initialValue, null);
		if (dlg.open() == Window.OK) {
			return dlg.getValue();
		}
		return null;
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}
