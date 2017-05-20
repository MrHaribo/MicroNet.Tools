package micronet.tools.launch.utility;

import java.util.function.Consumer;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;

public final class LaunchUtility {
	private LaunchUtility() {
	}
	
	public static void waitForLaunchTermination(String launchName, Consumer<ILaunch> callback) {
		final ILaunchManager launchMan = DebugPlugin.getDefault().getLaunchManager();
		launchMan.addLaunchListener(new ILaunchesListener2() {

			public void launchesAdded(ILaunch[] arg0) {	}
			public void launchesChanged(ILaunch[] arg0) { }
			public void launchesRemoved(ILaunch[] arg0) { }

			@Override
			public void launchesTerminated(ILaunch[] arg0) {
				for (ILaunch launch : arg0) {
					if (launch.getLaunchConfiguration().getName().equals(launchName)) {
						launchMan.removeLaunchListener(this);
						ILaunch launchCaptcha = launch;
						callback.accept(launchCaptcha);
					}
				}
			}
		});
	}
	
	public static boolean isLaunchRunning(String name) {
		final ILaunchManager launchMan = DebugPlugin.getDefault().getLaunchManager();

		for (ILaunch launch : launchMan.getLaunches()) {
			if (!launch.getLaunchConfiguration().getName().equals(name))
				continue;
			if (!launch.isTerminated())
				return true;
		}
		return false;
	}
	
	public static ILaunch getLaunch(String name) {
		final ILaunchManager launchMan = DebugPlugin.getDefault().getLaunchManager();

		for (ILaunch launch : launchMan.getLaunches()) {
			if (!launch.getLaunchConfiguration().getName().equals(name))
				continue;
			return launch;
		}
		return null;
	}

	public static void removeLaunch(ILaunch launch) {
		final ILaunchManager launchMan = DebugPlugin.getDefault().getLaunchManager();
		launchMan.removeLaunch(launch);
	}
	
	public static void removeLaunch(String name) {
		final ILaunchManager launchMan = DebugPlugin.getDefault().getLaunchManager();
		for (ILaunch launch : launchMan.getLaunches()) {
			if (launch.getLaunchConfiguration().getName().equals(name)) {
				try {
					launch.terminate();
				} catch (DebugException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				launchMan.removeLaunch(launch);
				return;
			}
		}
	}
	
	public static void showWarningMessageBox(String text, String message) {
		if (Display.getCurrent() == null || Display.getCurrent().getActiveShell() == null)
			return;
		MessageBox dialog = new MessageBox(Display.getCurrent().getActiveShell(), SWT.ICON_WARNING | SWT.OK);
		dialog.setText(text);
		dialog.setMessage(message);
		dialog.open();
	}
}
