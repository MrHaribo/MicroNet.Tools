package micronet.tools.ui.modelview.views;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
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
import micronet.tools.ui.modelview.codegen.ModelGenerator;
import micronet.tools.ui.modelview.nodes.EntityTemplateNode;
import micronet.tools.ui.modelview.nodes.EntityTemplateRootNode;
import micronet.tools.ui.modelview.nodes.EntityVariableNode;
import micronet.tools.ui.modelview.nodes.EnumNode;
import micronet.tools.ui.modelview.nodes.EnumRootNode;
import micronet.tools.ui.modelview.nodes.ModelNode;
import micronet.tools.ui.modelview.nodes.PrefabNode;
import micronet.tools.ui.modelview.nodes.PrefabRootNode;
import micronet.tools.ui.modelview.nodes.PrefabVariableNode;
import micronet.tools.ui.modelview.variables.VariableDescription;
import micronet.tools.ui.modelview.variables.VariableType;

public class ModelView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "micronet.tools.ui.modelview.views.ModelView";

	private final ImageDescriptor IMG_ADD = getImageDescriptor("add.png");
	private final ImageDescriptor IMG_REMOVE = getImageDescriptor("remove.png");
	private final ImageDescriptor IMG_MICRONET = getImageDescriptor("micronet_icon.png");

	private TreeViewer viewer;
	private DrillDownAdapter drillDownAdapter;
	
	private Action removeNodeAction;
	
	private Action addChildTemplateAction;
	private Action addChildVariableAction;
	private Action reftreshPrefabTree;
	
	private Action addEnumAction;
	
	private Action addPrefabAction;
	private Action refreshPrefabTreeAction;
	private Action savePrefabTreeAction;
	
	private Action testGenAction;

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
					TemplateNodeRootDetails templateRootDetails = new TemplateNodeRootDetails(detailsContainer,	SWT.NONE);
					templateRootDetails.setOnAddChildTemplate(addChildTemplateAction);
					currentDetailPanel = templateRootDetails;
				} else if (selectedNode instanceof EntityTemplateNode) {
					TemplateNodeDetails templateDetails = new TemplateNodeDetails((EntityTemplateNode)selectedNode, detailsContainer, SWT.NONE);
					templateDetails.setOnAddChildTemplate(addChildTemplateAction);
					templateDetails.setOnRemove(removeNodeAction);
					templateDetails.setOnAddChildVariable(addChildVariableAction);
					currentDetailPanel = templateDetails;
				} else if (selectedNode instanceof EntityVariableNode) {
					TemplateVariableNodeDetails variableDetails = new TemplateVariableNodeDetails((EntityVariableNode)selectedNode, detailsContainer, SWT.NONE);
					variableDetails.setOnRemove(removeNodeAction);
					variableDetails.setOnVariableChanged(reftreshPrefabTree);
					currentDetailPanel = variableDetails;
				} else if (selectedNode instanceof EnumRootNode) {
					EnumNodeRootDetails enumRootDetails = new EnumNodeRootDetails(detailsContainer, SWT.NONE);
					enumRootDetails.setOnAddEnum(addEnumAction);
					currentDetailPanel = enumRootDetails;
				} else if (selectedNode instanceof EnumNode) {
					EnumNodeDetails enumDetails = new EnumNodeDetails((EnumNode)selectedNode, detailsContainer, SWT.NONE);
					enumDetails.setOnRemove(removeNodeAction);
					currentDetailPanel = enumDetails;
				} else if (selectedNode instanceof PrefabNode) {
					PrefabNodeDetails prefabDetails = new PrefabNodeDetails((PrefabNode)selectedNode, detailsContainer, SWT.NONE);
					prefabDetails.setOnRemove(removeNodeAction);
					prefabDetails.setOnAddPrefab(addPrefabAction);
					prefabDetails.setOnSavePrefab(savePrefabTreeAction);
					currentDetailPanel = prefabDetails;
				} else if (selectedNode instanceof PrefabRootNode) {
					PrefabNodeRootDetails prefabDetails = new PrefabNodeRootDetails(detailsContainer, SWT.NONE);
					prefabDetails.setOnAddPrefab(addPrefabAction);
					prefabDetails.setOnSavePrefab(savePrefabTreeAction);
					currentDetailPanel = prefabDetails;
				} else if (selectedNode instanceof PrefabVariableNode) {
					PrefabVariableNodeDetails prefabDetails = new PrefabVariableNodeDetails((PrefabVariableNode)selectedNode, detailsContainer, SWT.NONE);
					prefabDetails.setOnPrefabTreeChanged(refreshPrefabTreeAction);
					currentDetailPanel = prefabDetails;
				}

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
		manager.add(removeNodeAction);
		manager.add(new Separator());
		manager.add(addChildTemplateAction);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(removeNodeAction);
		manager.add(addChildTemplateAction);
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
		testGenAction = new Action() {
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
		
		removeNodeAction = new Action() {
			public void run() {
				if (selectedNode == null || selectedNode.getParent() == null)
					return;
				
				if (selectedNode instanceof EntityTemplateRootNode || selectedNode instanceof EnumRootNode) {
					return;
				}
				String sharedDir = ModelProvider.INSTANCE.getSharedDir();
				
				if (selectedNode instanceof EnumNode) {
					
					Map<String, Set<String>> enumUsage = SyncTemplateTree.getEnumUsage(sharedDir);
					if (enumUsage.containsKey(selectedNode.getName())) {
						showMessage("Enum " + selectedNode.getName() + " cant be removed because it is in use by: " +
								String.join(",", enumUsage.get(selectedNode.getName())));
						return;
					}
					
					if (!promptQuestion("Remove Node", "Do you really want to remove: " + selectedNode.getName()))
						return;
					
					SyncEnumTree.removeEnum(selectedNode, sharedDir);

					enumRoot.removeChild(selectedNode);
					
				} else if (selectedNode instanceof EntityTemplateNode) {
					
					EntityTemplateNode entityTemplateNode = (EntityTemplateNode) selectedNode;

					for (INode node : entityTemplateNode.getChildren()) {
						if (node instanceof EntityTemplateNode) {
							showMessage("Cannot Remove Template with Children");
							return;
						}
					}
					
					Map<String, Set<String>> templateUsage = SyncTemplateTree.getTemplateUsage(sharedDir);
					if (templateUsage.containsKey(entityTemplateNode.getName())) {
						showMessage("Template " + entityTemplateNode.getName() + " cant be removed because it is in use by: " +
								String.join(",", templateUsage.get(entityTemplateNode.getName())));
						return;
					}
					
					if (!promptQuestion("Remove Node", "Do you really want to remove: " + selectedNode.getName()))
						return;

					SyncTemplateTree.removeTemplate(selectedNode, sharedDir);
					
					((EntityTemplateNode) selectedNode.getParent()).removeChild(selectedNode);
					
				} else if (selectedNode instanceof EntityVariableNode) {
					
					if (!promptQuestion("Remove Node", "Do you really want to remove: " + selectedNode.getName()))
						return;

					EntityTemplateNode parentTemplate = (EntityTemplateNode)selectedNode.getParent();
					
					parentTemplate.removeChild(selectedNode);
					SyncTemplateTree.saveTemplateTree(parentTemplate, sharedDir);
					
				} else if (selectedNode instanceof PrefabNode) {
					
					if (!promptQuestion("Remove Node", "Do you really want to remove: " + selectedNode.getName()))
						return;

					SyncPrefabTree.removePrefab((PrefabNode)selectedNode, sharedDir);
					((ModelNode)selectedNode.getParent()).removeChild(selectedNode);
				}
				
				prefabRoot = SyncPrefabTree.loadPrefabTree(sharedDir);
				viewer.refresh();
			}
		};
		removeNodeAction.setText("Action 1");
		removeNodeAction.setToolTipText("Action 1 tooltip");
		removeNodeAction.setImageDescriptor(IMG_REMOVE);

		addChildTemplateAction = new Action() {
			public void run() {
				String name = promptName("Add EntityTemplate", "NewType", "Enter Name for new EntityTemplate.");
				if (name == null)
					return;
				String sharedDir = ModelProvider.INSTANCE.getSharedDir();

				if (!ModelConstants.isValidJavaIdentifier(name)) {
					showMessage("\"" + name + "\" is an invalid name.");
					return;
				}
				
				if (ModelConstants.isPrimitiveTypeName(name)) {
					showMessage("Primitive Typenames are reserved.");
					return;
				}

				if (SyncTemplateTree.templateExists(name, sharedDir)) {
					showMessage("Template with the name \"" + name + "\" already exists. Choose a unique name.");
					return;
				}
				
				if (selectedNode == null)
					selectedNode = entityTemplateRoot;

				if (selectedNode instanceof EntityTemplateNode) {
					EntityTemplateNode entityTemplateNode = (EntityTemplateNode) selectedNode;
					entityTemplateNode.addChild(new EntityTemplateNode(name));
					viewer.refresh();

					SyncTemplateTree.saveTemplateTree(entityTemplateNode, sharedDir);
				}
			}
		};
		addChildTemplateAction.setText("Add Child Template");
		addChildTemplateAction.setToolTipText("Adds a Child Template to the currently selected Template.");
		addChildTemplateAction.setImageDescriptor(IMG_ADD);

		addChildVariableAction = new Action() {
			public void run() {
				
				if (selectedNode instanceof EntityTemplateRootNode)
					return;
				
				if (selectedNode instanceof EntityTemplateNode) {
					EntityTemplateNode entityTemplateNode = (EntityTemplateNode) selectedNode;

					String name = promptName("Add Variable", "NewVariable",	"Add a new Variable to " + selectedNode.getName());
					if (name == null)
						return;
					
					if (!ModelConstants.isValidJavaIdentifier(name)) {
						showMessage("\"" + name + "\" is an invalid name.");
						return;
					}

					for (INode child : entityTemplateNode.getChildren()) {
						if (child.getName().equals(name)) {
							showMessage("Variable with the same name already exists. Choose a unique name.");
							return;
						}
					}

					EntityVariableNode variableNode = new EntityVariableNode(name);
					variableNode.setVariabelDescription(new VariableDescription(VariableType.STRING));
					entityTemplateNode.addChild(variableNode);

					String sharedDir = ModelProvider.INSTANCE.getSharedDir();
					SyncTemplateTree.saveTemplateTree(entityTemplateNode, sharedDir);
					
					prefabRoot = SyncPrefabTree.loadPrefabTree(sharedDir);
					viewer.refresh();
				}
			}
		};
		addChildVariableAction.setText("Add Variable");
		addChildVariableAction.setToolTipText("Adds a Variable to the Selected Template");
		addChildVariableAction.setImageDescriptor(IMG_ADD);
		
		addEnumAction = new Action() {
			public void run() {
				if (selectedNode instanceof EnumRootNode) {
					EnumRootNode enumRootNode = (EnumRootNode) selectedNode;

					String name = promptName("Add new Enum", "NewEnum",	"Enter Name for the new Enum");
					if (name == null)
						return;
					
					if (!ModelConstants.isValidJavaIdentifier(name)) {
						showMessage("\"" + name + "\" is an invalid name.");
						return;
					}
					String sharedDir = ModelProvider.INSTANCE.getSharedDir();
					if (SyncEnumTree.enumExists(name, sharedDir)) {
						showMessage("Enum with the same name already exists. Choose a unique name.");
						return;
					}

					enumRootNode.addChild(new EnumNode(name));
					viewer.refresh();

					SyncEnumTree.saveEnumTree(enumRootNode, sharedDir);
				}
			}
		};
		addEnumAction.setText("Add Enum");
		addEnumAction.setToolTipText("Adds a new Enum.");
		addEnumAction.setImageDescriptor(IMG_ADD);
		
		addPrefabAction = new Action() {
			public void run() {
				if (selectedNode instanceof PrefabNode || selectedNode instanceof PrefabRootNode) {

					String name = promptName("Add new Prefab Node", "NewPrefab", "Enter Name for the new Prefab");
					if (name == null)
						return;
					
					if (!ModelConstants.isValidJavaIdentifier(name)) {
						showMessage("\"" + name + "\" is an invalid name.");
						return;
					}
					
					String sharedDir = ModelProvider.INSTANCE.getSharedDir();
					List<String> allTemplateNames = SyncTemplateTree.getAllTemplateNames(sharedDir);
					
					ElementListSelectionDialog dialog = new ElementListSelectionDialog(viewer.getControl().getShell(), new LabelProvider());
					dialog.setElements(allTemplateNames.toArray());
					dialog.setTitle("Select A type for the Prefab");
					if (dialog.open() != Window.OK)
						return;
					
					Object[] selectedType = dialog.getResult();
					if (selectedType.length == 0 || selectedType.length > 1)
						return;
					
//					if (SyncEnumTree.enumExists(name, sharedDir)) {
//						showMessage("Enum with the same name already exists. Choose a unique name.");
//						return;
//					}

					selectedNode.addChild(new PrefabNode(name, selectedType[0].toString()));
					viewer.refresh();

					//SyncEnumTree.saveEnumTree(enumRootNode, sharedDir);
				}
			}
		};
		addPrefabAction.setText("Add Prefab");
		addPrefabAction.setToolTipText("Adds a new Prefab.");
		addPrefabAction.setImageDescriptor(IMG_ADD);
		
		refreshPrefabTreeAction = new Action() {
			public void run() {
				viewer.refresh();
			}
		};
		refreshPrefabTreeAction.setText("Refresh Prefab Tree");
		refreshPrefabTreeAction.setToolTipText("Refreshes the Prefab tree.");
		refreshPrefabTreeAction.setImageDescriptor(IMG_ADD);
		
		savePrefabTreeAction = new Action() {
			public void run() {
				if (selectedNode != null) {
					String sharedDir = ModelProvider.INSTANCE.getSharedDir();
					SyncPrefabTree.savePrefab(selectedNode, sharedDir);
				}
			}
		};
		savePrefabTreeAction.setText("Save Prefab Tree");
		savePrefabTreeAction.setToolTipText("Saves the Prefab Tree to disk.");
		savePrefabTreeAction.setImageDescriptor(IMG_ADD);
		
		reftreshPrefabTree = new Action() {
			public void run() {
				String sharedDir = ModelProvider.INSTANCE.getSharedDir();
				prefabRoot = SyncPrefabTree.loadPrefabTree(sharedDir);
				viewer.refresh();
			}
		};
		reftreshPrefabTree.setText("Refresh Prefab Tree");
		reftreshPrefabTree.setToolTipText("Refreshed the prefab tree.");
		reftreshPrefabTree.setImageDescriptor(IMG_ADD);
	}

	private void showMessage(String message) {
		MessageDialog.openInformation(viewer.getControl().getShell(), "Model View", message);
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

	private static ImageDescriptor getImageDescriptor(String file) {
		// assume that the current class is called View.java
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
