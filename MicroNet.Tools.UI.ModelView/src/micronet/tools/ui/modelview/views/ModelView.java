package micronet.tools.ui.modelview.views;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.*;

import micronet.tools.ui.modelview.Entity;

import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;


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

public class ModelView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "micronet.tools.ui.modelview.views.ModelView";

	private TreeViewer viewer;
	private DrillDownAdapter drillDownAdapter;
	private Action action1;
	private Action action2;
	private Action doubleClickAction;
	
	private Set<String> entityTemplatesName = new HashSet<>();
	 
	class ViewContentProvider implements ITreeContentProvider {
		private EntityTemplateNode entityTemplateRoot;

		public Object[] getElements(Object parent) {
			if (parent.equals(getViewSite())) {
				if (entityTemplateRoot==null)
					initialize();
				return Arrays.asList(entityTemplateRoot).toArray();
			}
			return getChildren(parent);
		}
		public Object getParent(Object child) {
			if (child instanceof EntityNode) {
				return ((EntityNode)child).getParent();
			}
			return null;
		}
		public Object [] getChildren(Object parent) {
			if (parent instanceof EntityTemplateNode) {
				return ((EntityTemplateNode)parent).getChildren();
			}
			return new Object[0];
		}
		public boolean hasChildren(Object parent) {
			if (parent instanceof EntityTemplateNode)
				return ((EntityTemplateNode)parent).hasChildren();
			return false;
		}
		
		
		private void initialize() {
			entityTemplateRoot = new EntityTemplateNode("Entity Templates");
			
			EntityTemplateNode to1 = new EntityTemplateNode("Leaf 1");
			EntityTemplateNode to2 = new EntityTemplateNode("Leaf 2");
			EntityTemplateNode to3 = new EntityTemplateNode("Leaf 3");
			entityTemplateRoot.addChild(to1);
			entityTemplateRoot.addChild(to2);
			entityTemplateRoot.addChild(to3);
		}
	}

	class ViewLabelProvider extends LabelProvider {

		public String getText(Object obj) {
			return obj.toString();
		}
		public Image getImage(Object obj) {
			String imageKey = ISharedImages.IMG_OBJ_ELEMENT;
			if (obj instanceof EntityTemplateNode)
			   imageKey = ISharedImages.IMG_OBJ_FOLDER;
			return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
		}
	}

/**
	 * The constructor.
	 */
	public ModelView() {
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		
		parent.setLayout(new GridLayout(1, false));
		
		EntityDetails entityDetailPanel = new EntityDetails(parent, SWT.NONE);
		entityDetailPanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		drillDownAdapter = new DrillDownAdapter(viewer);
		
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setInput(getViewSite());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent arg0) {
				
				TreeSelection selection = (TreeSelection) arg0.getSelection();

				if (selection.getFirstElement() == null || selection.size() > 1)
					entityDetailPanel.setEntity(null);
				
				EntityNode selectedEntity = (EntityNode)selection.getFirstElement();
				entityDetailPanel.setEntity(selectedEntity);
			}
		});
		
		entityDetailPanel.setAddTemplateListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String name = promptName("Add EntityTemplate", "NewType", "Enter Name for new EntityTemplate.");
				if (name == null)
					return;
				
				if (entityTemplatesName.contains(name)) {
					showMessage("Template with the same name exists already. Choose a unique name.");
					return;
				}
				
				EntityNode selectedNode = entityDetailPanel.getSelectedEntity();
				if (selectedNode instanceof EntityTemplateNode) {
					EntityTemplateNode entityTemplateNode = (EntityTemplateNode)selectedNode;
					entityTemplateNode.addChild(new EntityTemplateNode(name));
					entityTemplatesName.add(name);
					viewer.refresh();
				}
			}
		});
		
		entityDetailPanel.setRemoveTemplateListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				EntityNode selectedNode = entityDetailPanel.getSelectedEntity();
				
				if (selectedNode == null || selectedNode.getParent() == null)
					return;
				
				if (selectedNode instanceof EntityTemplateNode) {
					EntityTemplateNode entityTemplateNode = (EntityTemplateNode)selectedNode;
					
					if (entityTemplateNode.hasChildren()) {
						showMessage("Cannot Remove Template with Children");
						return;
					}
					
					if (!promptQuestion("Remove Template", "Do you really want to remove the template: " + entityTemplateNode.getName()))
						return;
					
					((EntityTemplateNode)entityTemplateNode.getParent()).removeChild(entityTemplateNode);
					entityTemplatesName.add(entityTemplateNode.getName());
					viewer.refresh();
				}
			}
		});

		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "MicroNet.Tools.UI.ModelView.viewer");
		getSite().setSelectionProvider(viewer);
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				ModelView.this.fillContextMenu(manager);
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
		manager.add(action1);
		manager.add(new Separator());
		manager.add(action2);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(action1);
		manager.add(action2);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(action1);
		manager.add(action2);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
	}

	private void makeActions() {
		action1 = new Action() {
			public void run() {
				showMessage("Action 1 executed");
			}
		};
		action1.setText("Action 1");
		action1.setToolTipText("Action 1 tooltip");
		action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
			getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		
		action2 = new Action() {
			public void run() {
				showMessage("Action 2 executed");
			}
		};
		action2.setText("Action 2");
		action2.setToolTipText("Action 2 tooltip");
		action2.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				showMessage("Double-click detected on "+obj.toString());
			}
		};
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}
	private void showMessage(String message) {
		MessageDialog.openInformation(
			viewer.getControl().getShell(),
			"Model View",
			message);
	}
	
	private String promptName(String title, String initialValue, String message) {
		InputDialog dlg = new InputDialog(Display.getCurrent().getActiveShell(), title, message, initialValue, null);
		if (dlg.open() == Window.OK) {
			return dlg.getValue();
		}
		return null;
	}
	
	private boolean promptQuestion(String title, String message) {
		return MessageDialog.openQuestion(viewer.getControl().getShell(), title, message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}
