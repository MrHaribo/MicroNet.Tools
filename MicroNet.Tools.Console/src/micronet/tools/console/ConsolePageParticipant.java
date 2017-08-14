package micronet.tools.console;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.part.IPageSite;

public class ConsolePageParticipant  implements IConsolePageParticipant {

    private IPageBookViewPage page;
    private Action remove, removeAll;
    private IActionBars bars;
    private Console console;

    @Override
    public void init(final IPageBookViewPage page, final IConsole console) {
        this.console = (Console) console;
        this.page = page;
        IPageSite site = page.getSite();
        this.bars = site.getActionBars();

        createRemoveButton();
        createRemoveAllButton();

        bars.getMenuManager().add(new Separator());
        bars.getMenuManager().add(remove);
        bars.getMenuManager().add(removeAll);

        IToolBarManager toolbarManager = bars.getToolBarManager();

        toolbarManager.appendToGroup(IConsoleConstants.LAUNCH_GROUP, remove);
        toolbarManager.appendToGroup(IConsoleConstants.LAUNCH_GROUP, removeAll);

        bars.updateActionBars();
        this.console.addTerminationListener(terminated -> {
    		setTerminated(terminated);
    	});
        setTerminated(this.console.isTerminated());
    }
    
    private void setTerminated(boolean terminated) {
        removeAll.setEnabled(terminated);
        remove.setEnabled(terminated);
        page.getSite().getShell().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				bars.updateActionBars();
			}
		});
    }

    private void createRemoveAllButton() {
    	ImageDescriptor icon = PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(org.eclipse.ui.ISharedImages.IMG_ELCL_REMOVEALL);
        this.removeAll = new Action("Remove all Terminated MicroNet Launches", icon) {
            public void run() {
            	List<IConsole> microNetConsoles = new ArrayList<>();
            	for (IConsole c : ConsolePlugin.getDefault().getConsoleManager().getConsoles()) {
            		if (c instanceof Console && ((Console) c).isTerminated())
            			microNetConsoles.add(c);
            	}
            	ConsolePlugin.getDefault().getConsoleManager().removeConsoles(microNetConsoles.toArray(new IConsole[microNetConsoles.size()]));
            }
        };

    }

    private void createRemoveButton() {
    	ImageDescriptor icon = PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(org.eclipse.ui.ISharedImages.IMG_ELCL_REMOVE);
        this.remove= new Action("Remove Launch", icon) {
            public void run() {
            	if (console instanceof Console && ((Console) console).isTerminated())
            		ConsolePlugin.getDefault().getConsoleManager().removeConsoles(new IConsole[]{console});
            }
        };
    }

    @Override
    public void dispose() {
        remove= null;
        removeAll = null;
        bars = null;
        page = null;
    }

    @Override
    public <T> T getAdapter(Class<T> arg0) {
    	return null;
    }

    @Override
    public void activated() {
        updateVis(console.isTerminated());
    }

    @Override
    public void deactivated() {
        updateVis(false);
    }

    private void updateVis(boolean isEnabled) {

        if (page == null)
            return;
        removeAll.setEnabled(isEnabled);
        remove.setEnabled(isEnabled);
        bars.updateActionBars();
    }
}
