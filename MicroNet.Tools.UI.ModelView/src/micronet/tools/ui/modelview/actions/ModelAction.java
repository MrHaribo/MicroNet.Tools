package micronet.tools.ui.modelview.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Event;

public class ModelAction extends Action {
	
	private Action refreshViewerAction;
	private boolean refreshPrefabTree;
	
	public void setRefreshViewerAction(Action refreshViewerAction, boolean refreshPrefabTree) {
		this.refreshViewerAction = refreshViewerAction;
		this.refreshPrefabTree = refreshPrefabTree;
	}

	public void refreshViewer() {
		if (refreshViewerAction != null) {
			if (refreshPrefabTree) {
				Event event = new Event();
				event.data = true;
				refreshViewerAction.runWithEvent(event);
			} else {
				refreshViewerAction.run();
			}
		}
	}
}
