package micronet.tools.ui.modelview.actions;

import org.eclipse.jface.action.Action;

public class ModelAction extends Action {
	
	private Action refreshViewerAction;
	
	public void setRefreshViewerAction(Action refreshViewerAction) {
		this.refreshViewerAction = refreshViewerAction;
	}

	public void refreshViewer() {
		if (refreshViewerAction != null)
			refreshViewerAction.run();
	}
}
