package micronet.tools.ui.modelview.views;

import java.net.URL;
import java.util.Arrays;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
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
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import micronet.tools.core.ModelProvider;
import micronet.tools.ui.modelview.INode;
import micronet.tools.ui.modelview.ModelConstants;
import micronet.tools.ui.modelview.SyncEnumTree;
import micronet.tools.ui.modelview.SyncPrefabTree;
import micronet.tools.ui.modelview.SyncTemplateTree;
import micronet.tools.ui.modelview.actions.EnumCreateAction;
import micronet.tools.ui.modelview.actions.ModelAction;
import micronet.tools.ui.modelview.actions.TemplateCreateAction;
import micronet.tools.ui.modelview.codegen.ModelGenerator;
import micronet.tools.ui.modelview.nodes.EntityTemplateNode;
import micronet.tools.ui.modelview.nodes.EntityTemplateRootNode;
import micronet.tools.ui.modelview.nodes.EntityVariableNode;
import micronet.tools.ui.modelview.nodes.EnumNode;
import micronet.tools.ui.modelview.nodes.EnumRootNode;
import micronet.tools.ui.modelview.nodes.ModelNode;
import micronet.tools.ui.modelview.nodes.PrefabNode;
import micronet.tools.ui.modelview.nodes.PrefabRootNode;
import micronet.tools.ui.modelview.nodes.PrefabVariableEntryNode;
import micronet.tools.ui.modelview.nodes.PrefabVariableNode;

public class ModelView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "micronet.tools.ui.modelview.views.ModelView";

	public static final ImageDescriptor IMG_ADD = getImageDescriptor("add.png");
	public static final ImageDescriptor IMG_REMOVE = getImageDescriptor("remove.png");
	public static final ImageDescriptor IMG_MICRONET = getImageDescriptor("micronet_icon.png");

	private TreeViewer viewer;
	private DrillDownAdapter drillDownAdapter;
	
	private ModelAction refreshViewerAction;
	
	private ModelAction testGenAction;

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
			if (parent instanceof ModelNode) 
				return ((ModelNode) parent).getChildren();
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
			if (obj instanceof EntityTemplateNode || obj instanceof EnumRootNode || obj instanceof PrefabNode || obj instanceof PrefabRootNode)
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
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
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
					TemplateNodeRootDetails templateRootDetails = new TemplateNodeRootDetails(entityTemplateRoot, detailsContainer,	SWT.BORDER);
					currentDetailPanel = templateRootDetails;
				} else if (selectedNode instanceof EntityTemplateNode) {
					TemplateNodeDetails templateDetails = new TemplateNodeDetails((EntityTemplateNode)selectedNode, detailsContainer, SWT.BORDER);
					currentDetailPanel = templateDetails;
				} else if (selectedNode instanceof EntityVariableNode) {
					TemplateVariableNodeDetails variableDetails = new TemplateVariableNodeDetails((EntityVariableNode)selectedNode, detailsContainer, SWT.BORDER);
					currentDetailPanel = variableDetails;
				} else if (selectedNode instanceof EnumRootNode) {
					EnumNodeRootDetails enumRootDetails = new EnumNodeRootDetails(enumRoot, detailsContainer, SWT.BORDER);
					currentDetailPanel = enumRootDetails;
				} else if (selectedNode instanceof EnumNode) {
					EnumNodeDetails enumDetails = new EnumNodeDetails((EnumNode)selectedNode, detailsContainer, SWT.BORDER);
					currentDetailPanel = enumDetails;
				} else if (selectedNode instanceof PrefabNode) {
					PrefabNodeDetails prefabDetails = new PrefabNodeDetails((PrefabNode)selectedNode, detailsContainer, SWT.BORDER);
					currentDetailPanel = prefabDetails;
				} else if (selectedNode instanceof PrefabRootNode) {
					PrefabNodeRootDetails prefabDetails = new PrefabNodeRootDetails(prefabRoot, detailsContainer, SWT.BORDER);
					currentDetailPanel = prefabDetails;
				} else if (selectedNode instanceof PrefabVariableEntryNode) {
					PrefabVariableNodeDetails prefabDetails = new PrefabVariableNodeDetails((PrefabVariableNode)selectedNode, detailsContainer, SWT.BORDER, true);
					currentDetailPanel = prefabDetails;
				} else if (selectedNode instanceof PrefabVariableNode) {
					PrefabVariableNodeDetails prefabDetails = new PrefabVariableNodeDetails((PrefabVariableNode)selectedNode, detailsContainer, SWT.BORDER, false);
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
		manager.add(new Separator());
		manager.add(new TemplateCreateAction(viewer.getControl().getShell(), entityTemplateRoot));
		manager.add(new EnumCreateAction(viewer.getControl().getShell(), enumRoot));
	}

	private void fillContextMenu(IMenuManager manager) {
		//manager.add(removeNodeAction);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(testGenAction);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
	}

	private void makeActions() {
		testGenAction = new ModelAction() {
			public void run() {
				if (selectedNode == null || selectedNode.getParent() == null)
					return;
				
				if (selectedNode instanceof EntityTemplateNode) {
					EntityTemplateNode entityTemplateNode = (EntityTemplateNode) selectedNode;
					
					if (entityTemplateNode.getName().equals(ModelConstants.ENTITY_TEMPLATE_ROOT_KEY))
						return;
					
					ModelGenerator.generateModelEntity(entityTemplateNode);
				}
			}
		};
		testGenAction.setText("Test Gen");
		testGenAction.setToolTipText("Action 1 tooltip");
		testGenAction.setImageDescriptor(IMG_MICRONET);
		
		refreshViewerAction = new ModelAction() {
			@Override
			public void runWithEvent(Event event) {
				if ((boolean)event.data) {
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
		refreshViewerAction.setImageDescriptor(IMG_ADD);
	}

	public static ImageDescriptor getImageDescriptor(String file) {
		Bundle bundle = FrameworkUtil.getBundle(ModelView.class);
		URL url = FileLocator.find(bundle, new org.eclipse.core.runtime.Path("icons/" + file), null);
		return ImageDescriptor.createFromURL(url);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}
