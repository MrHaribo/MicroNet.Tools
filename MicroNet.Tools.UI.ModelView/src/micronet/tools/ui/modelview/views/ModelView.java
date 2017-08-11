package micronet.tools.ui.modelview.views;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;

import micronet.tools.core.Icons;
import micronet.tools.core.ModelProvider;
import micronet.tools.filesync.SyncEnumTree;
import micronet.tools.filesync.SyncPrefabTree;
import micronet.tools.filesync.SyncTemplateTree;
import micronet.tools.model.INode;
import micronet.tools.model.nodes.EntityTemplateNode;
import micronet.tools.model.nodes.EntityTemplateRootNode;
import micronet.tools.model.nodes.EntityVariableNode;
import micronet.tools.model.nodes.EnumNode;
import micronet.tools.model.nodes.EnumRootNode;
import micronet.tools.model.nodes.ModelNode;
import micronet.tools.model.nodes.PrefabNode;
import micronet.tools.model.nodes.PrefabRootNode;
import micronet.tools.model.nodes.PrefabVariableEntryNode;
import micronet.tools.model.nodes.PrefabVariableNode;
import micronet.tools.ui.modelview.actions.EnumCreateAction;
import micronet.tools.ui.modelview.actions.EnumRemoveAction;
import micronet.tools.ui.modelview.actions.ModelAction;
import micronet.tools.ui.modelview.actions.PrefabCreateAction;
import micronet.tools.ui.modelview.actions.PrefabRemoveAction;
import micronet.tools.ui.modelview.actions.TemplateCreateAction;
import micronet.tools.ui.modelview.actions.TemplateRemoveAction;
import micronet.tools.ui.modelview.actions.TemplateVariableCreateAction;
import micronet.tools.ui.modelview.actions.TemplateVariableRemoveAction;

public class ModelView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "micronet.tools.ui.modelview.views.ModelView";

	private TreeViewer viewer;
	private DrillDownAdapter drillDownAdapter;
	
	private ModelAction refreshViewerAction;

	private ModelAction createTemplateAction;
	private ModelAction createEnumAction;
	private ModelAction createPrefabAction;
	
	private Action refreshServicesAction;

	private ModelNode selectedNode;

	private EntityTemplateRootNode entityTemplateRoot;
	private EnumRootNode enumRoot;
	private PrefabRootNode prefabRoot;
	
	private Group detailsContainer;
	private Composite currentDetailPanel;

	class ViewContentProvider implements ITreeContentProvider {

		public Object[] getElements(Object parent) {
			if (parent.equals(getViewSite())) {
				return Arrays.asList(entityTemplateRoot, enumRoot, prefabRoot).toArray();
			}
			return getChildren(parent);
		}

		public Object getParent(Object child) {
			if (child instanceof INode) {
				return ((INode) child).getParent();
			}
			return null;
		}

		public Object[] getChildren(Object parent) {
			if (parent instanceof ModelNode)  {
				List<INode> children = ((ModelNode) parent).getChildren();
				return children.toArray(new ModelNode[children.size()]);
			}
			return new Object[0];
		}

		public boolean hasChildren(Object parent) {
			if (parent instanceof ModelNode)
				return ((ModelNode) parent).hasChildren();
			return false;
		}
	}

	class ViewLabelProvider extends LabelProvider {

		public String getText(Object obj) {
			return obj.toString();
		}

		public Image getImage(Object obj) {
			String imageKey = ISharedImages.IMG_OBJ_ELEMENT;
			if (obj instanceof EntityTemplateRootNode || obj instanceof EnumRootNode || obj instanceof PrefabRootNode) {
				imageKey = ISharedImages.IMG_OBJ_FOLDER;
			} else if(obj instanceof EntityTemplateNode) {
				return Icons.IMG_TEMPLATE.createImage();
			} else if(obj instanceof PrefabNode) {
				return Icons.IMG_PREFAB.createImage();
			} else if(obj instanceof EnumNode) {
				return Icons.IMG_ENUM.createImage();
			} else if(obj instanceof EntityVariableNode || obj instanceof PrefabVariableNode) {
				return Icons.IMG_VARIABLE.createImage();
			}
			
			
			return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
		}
	}

	public ModelView() {
	}

	public void createPartControl(Composite parent) {

		parent.setLayout(new GridLayout(2, false));

		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		drillDownAdapter = new DrillDownAdapter(viewer);

		String sharedDir = ModelProvider.INSTANCE.getSharedDir();
		entityTemplateRoot = SyncTemplateTree.loadTemplateTree(sharedDir);
		enumRoot = SyncEnumTree.loadEnumTree(sharedDir);
		prefabRoot = SyncPrefabTree.loadPrefabTree(sharedDir);

		viewer.setContentProvider(new ViewContentProvider());
		viewer.setInput(getViewSite());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		ModelProvider.INSTANCE.registerTemplatesChangedListener(()-> {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					entityTemplateRoot = SyncTemplateTree.loadTemplateTree(sharedDir);
					viewer.refresh();
				}
			});
		});

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent arg0) {

				TreeSelection selection = (TreeSelection) arg0.getSelection();

				if (currentDetailPanel != null)
					currentDetailPanel.dispose();

				if (selection.getFirstElement() == null || selection.size() > 1) {
					selectedNode = null;
					return;
				}
				
				if (!(selection.getFirstElement() instanceof ModelNode))
					return;

				selectedNode = (ModelNode) selection.getFirstElement();

				if (selectedNode instanceof EntityTemplateRootNode) {
					TemplateNodeRootDetails templateRootDetails = new TemplateNodeRootDetails(entityTemplateRoot, detailsContainer,	SWT.NONE);
					currentDetailPanel = templateRootDetails;
				} else if (selectedNode instanceof EntityTemplateNode) {
					TemplateNodeDetails templateDetails = new TemplateNodeDetails((EntityTemplateNode)selectedNode, detailsContainer, SWT.NONE);
					currentDetailPanel = templateDetails;
				} else if (selectedNode instanceof EntityVariableNode) {
					TemplateVariableNodeDetails variableDetails = new TemplateVariableNodeDetails((EntityVariableNode)selectedNode, detailsContainer, SWT.NONE);
					currentDetailPanel = variableDetails;
				} else if (selectedNode instanceof EnumRootNode) {
					EnumNodeRootDetails enumRootDetails = new EnumNodeRootDetails(enumRoot, detailsContainer, SWT.NONE);
					currentDetailPanel = enumRootDetails;
				} else if (selectedNode instanceof EnumNode) {
					EnumNodeDetails enumDetails = new EnumNodeDetails((EnumNode)selectedNode, detailsContainer, SWT.NONE);
					currentDetailPanel = enumDetails;
				} else if (selectedNode instanceof PrefabNode) {
					PrefabNodeDetails prefabDetails = new PrefabNodeDetails((PrefabNode)selectedNode, detailsContainer, SWT.NONE);
					currentDetailPanel = prefabDetails;
				} else if (selectedNode instanceof PrefabRootNode) {
					PrefabNodeRootDetails prefabDetails = new PrefabNodeRootDetails(prefabRoot, detailsContainer, SWT.NONE);
					currentDetailPanel = prefabDetails;
				} else if (selectedNode instanceof PrefabVariableEntryNode) {
					PrefabVariableNodeDetails prefabDetails = new PrefabVariableNodeDetails((PrefabVariableNode)selectedNode, detailsContainer, SWT.NONE, true);
					currentDetailPanel = prefabDetails;
				} else if (selectedNode instanceof PrefabVariableNode) {
					PrefabVariableNodeDetails prefabDetails = new PrefabVariableNodeDetails((PrefabVariableNode)selectedNode, detailsContainer, SWT.NONE, false);
					currentDetailPanel = prefabDetails;
				}
				
				if (currentDetailPanel instanceof IDetails)
					((IDetails)currentDetailPanel).setRefreshViewerAction(refreshViewerAction);

				detailsContainer.layout(true);
				detailsContainer.getParent().layout(true);
			}
		});

		detailsContainer = new Group(parent, SWT.NONE);
		detailsContainer.setText("Node Context");
		detailsContainer.setLayout(new GridLayout(1, false));

		GridData gridData = new GridData(SWT.FILL, SWT.FILL, false, true);
		gridData.widthHint = 280;
		detailsContainer.setLayoutData(gridData);

		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "MicroNet.Tools.UI.ModelView.viewer");
		getSite().setSelectionProvider(viewer);
		makeActions();
		hookContextMenu();
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
		manager.add(refreshViewerAction);
		manager.add(refreshServicesAction);
		manager.add(new Separator());
		manager.add(createTemplateAction);
		manager.add(createEnumAction);
		manager.add(createPrefabAction);
	}

	private void fillContextMenu(IMenuManager manager) {
		ModelAction action = null;
		
		if (selectedNode != null) {
			if (selectedNode instanceof EntityTemplateRootNode) {
				manager.add(createTemplateAction);
			} else if(selectedNode instanceof EnumRootNode) { 
				manager.add(createEnumAction);
			} else if (selectedNode instanceof PrefabRootNode) {
				manager.add(createPrefabAction);
			} else if(selectedNode instanceof EntityTemplateNode) {
				action = new TemplateRemoveAction(viewer.getControl().getShell(), (EntityTemplateNode)selectedNode);
				action.setRefreshViewerAction(refreshViewerAction, true);
				manager.add(action);
				
				action = new TemplateCreateAction(viewer.getControl().getShell(), (EntityTemplateNode)selectedNode);
				action.setText("Add Derived Template");
				action.setRefreshViewerAction(refreshViewerAction, false);
				manager.add(action);
				
				action = new TemplateVariableCreateAction(viewer.getControl().getShell(), (EntityTemplateNode)selectedNode);
				action.setRefreshViewerAction(refreshViewerAction, true);
				manager.add(action);
			} else if(selectedNode instanceof PrefabNode) {
				action = new PrefabCreateAction(viewer.getControl().getShell(), (PrefabNode)selectedNode);
				action.setRefreshViewerAction(refreshViewerAction, false);
				manager.add(action);

				action = new PrefabRemoveAction(viewer.getControl().getShell(), (PrefabNode)selectedNode);
				action.setRefreshViewerAction(refreshViewerAction, false);
				manager.add(action);
			} else if(selectedNode instanceof EnumNode) {
				action = new EnumRemoveAction(viewer.getControl().getShell(), (EnumNode)selectedNode);
				action.setRefreshViewerAction(refreshViewerAction, true);
				manager.add(action);
			} else if(selectedNode instanceof EntityVariableNode) { 
				action = new TemplateVariableRemoveAction(viewer.getControl().getShell(), (EntityVariableNode)selectedNode);
				action.setRefreshViewerAction(refreshViewerAction, true);
				manager.add(action);
			} else if (selectedNode instanceof PrefabVariableNode) {
			}
		}
		
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(refreshServicesAction);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
	}
	
	private void makeActions() {
		
		refreshViewerAction = new ModelAction() {
			@Override
			public void runWithEvent(Event event) {
				if (event.data != null && (boolean)event.data) {
					String sharedDir = ModelProvider.INSTANCE.getSharedDir();
					prefabRoot = SyncPrefabTree.loadPrefabTree(sharedDir);
				}
				run();
			}
			public void run() {
				viewer.refresh();
			}
		};
		refreshViewerAction.setText("Refresh Model Tree");
		refreshViewerAction.setToolTipText("Refreshes the template, enum and prefab tree.");
		refreshViewerAction.setImageDescriptor(Icons.IMG_REFRESH);

		createTemplateAction = new TemplateCreateAction(viewer.getControl().getShell(), entityTemplateRoot);
		createTemplateAction.setRefreshViewerAction(refreshViewerAction, false);
		
		createEnumAction = new EnumCreateAction(viewer.getControl().getShell(), enumRoot);
		createEnumAction.setRefreshViewerAction(refreshViewerAction, false);
		
		createPrefabAction = new PrefabCreateAction(viewer.getControl().getShell(), prefabRoot);
		createPrefabAction.setRefreshViewerAction(refreshViewerAction, false);
		
		refreshServicesAction = new Action() {
			public void run() {
				ModelProvider.INSTANCE.buildServiceProjects();
			}
		};
		refreshServicesAction.setText("Rebuild Services");
		refreshServicesAction.setToolTipText("Rebuild all service Projects in the Workspace");
		refreshServicesAction.setImageDescriptor(Icons.IMG_BUILD);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}
