package micronet.tools.ui.parametercodeview.views;


import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.part.*;

import micronet.serialization.Serialization;
import micronet.tools.annotation.codegen.CodegenConstants;
import micronet.tools.annotation.filesync.SyncParameterCodes;
import micronet.tools.core.ModelProvider;
import micronet.tools.ui.parametercodeview.WatchDir;
import micronet.tools.ui.parametercodeview.WatchDir.DirChangedListener;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;


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

public class ParameterCodeView extends ViewPart implements DirChangedListener {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "micronet.tools.ui.parametercodeview.views.SampleView";

	private TableViewer viewer;
	private Action addParameterCodeAction;
	private Action removeParameterCodeAction;

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
	public ParameterCodeView() {
		System.out.println("Test");
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(true);
		
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setInput(new String[] { "One", "Two", "Three" });
		viewer.setLabelProvider(new ViewLabelProvider());

		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "MicroNet.Tools.UI.ParameterCodeView.viewer");
		getSite().setSelectionProvider(viewer);
		makeActions();
		hookContextMenu();
		contributeToActionBars();
		
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		String sharedDir = workspaceRoot.getLocation().toOSString() + "/shared/";
		
		Thread watchDirThread = new Thread(new Runnable() {
			@Override
			public void run() {
				System.out.println("WatchDir Thread Start");
				try {
					Path path = Paths.get(sharedDir);
					WatchDir watchDir = new WatchDir(path, false);
					watchDir.registerDirChangedListener(ParameterCodeView.this);
					watchDir.processEvents();
				} catch (IOException e) {
					// TODO Auto-generated catch block
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
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		String sharedDir = workspaceRoot.getLocation().toOSString() + "/shared/";
		
		Set<String> parameterCodes = SyncParameterCodes.readParameters(sharedDir);
		String[] codeArray = parameterCodes.toArray(new String[parameterCodes.size()]);
		
		Display.getDefault().asyncExec(() -> {
			if (viewer.getControl().isDisposed())
				return;
			viewer.setInput(codeArray);
			viewer.refresh();
		});
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
		manager.add(new Separator());
		manager.add(removeParameterCodeAction);
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
	}

	private void makeActions() {
		addParameterCodeAction = new Action() {
			public void run() {
				showMessage("Action 1 executed");
			}
		};
		addParameterCodeAction.setText("Action 1");
		addParameterCodeAction.setToolTipText("Action 1 tooltip");
		addParameterCodeAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
			getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		
		removeParameterCodeAction = new Action() {
			public void run() {
				showMessage("Action 2 executed");
			}
		};
		removeParameterCodeAction.setText("Action 2");
		removeParameterCodeAction.setToolTipText("Action 2 tooltip");
		removeParameterCodeAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
	}

	private void showMessage(String message) {
		MessageDialog.openInformation(
			viewer.getControl().getShell(),
			"Sample View",
			message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}
