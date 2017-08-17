package micronet.tools.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

public class Console extends MessageConsole {
	
	private static final ImageDescriptor IMG_MICRONET = getImageDescriptor("micronet_icon.png");

	private static final String globalConsoleName = "MicroNet";
	private static final Semaphore globalConsoleLock = new Semaphore(0);
	private static MessageConsole globalConsole = null;
	private static MessageConsoleStream globalConsoleStream = null;
	
	private boolean terminated = false;
	
	private List<Consumer<Boolean>> terminationListeners = new ArrayList<>();
	
	static {
		globalConsole = new MessageConsole(globalConsoleName, IMG_MICRONET);
		globalConsoleStream = globalConsole.newMessageStream();
		globalConsoleLock.release();
    }
	
	public static ImageDescriptor getImageDescriptor(String file) {
		Bundle bundle = FrameworkUtil.getBundle(Console.class);
		URL url = FileLocator.find(bundle, new org.eclipse.core.runtime.Path("icons/" + file), null);
		return ImageDescriptor.createFromURL(url);
	}
	
	public Console(String name, ImageDescriptor imageDescriptor) {
		super(name, imageDescriptor);
	}
	
	public boolean isTerminated() {
		return terminated;
	}

	public void setTerminated(boolean terminated) {
		notifyTerminationListeners(terminated);
		this.terminated = terminated;
	}
	
	public static void print(String str) {
		try {
			globalConsoleLock.acquire();
			globalConsoleStream.print(str);
		} catch (InterruptedException e) {
		} finally {
			globalConsoleLock.release();
		}
	}
	
	public static void println(String str) {
		try {
			globalConsoleLock.acquire();
			globalConsoleStream.println(str);
		} catch (InterruptedException e) {
		} finally {
			globalConsoleLock.release();
		}
	}
	
	public static void printStackTrace(Exception exception) {
		try {
			globalConsoleLock.acquire();
			exception.printStackTrace(new PrintStream(globalConsoleStream));
		} catch (InterruptedException e) {
		} finally {
			globalConsoleLock.release();
		}
	}

	public static void showGlobalConsole(IWorkbenchPage page) {
		ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[]{globalConsole});
		showConsole(globalConsole, page);
	}
	
	public static void createConsole(String name, InputStream inStream, IWorkbenchPage page) {
		createConsole(name, inStream, page, null);
	}
	
	public static void createConsole(String name, InputStream inStream, IWorkbenchPage page, ImageDescriptor icon) {
		Console console = new Console(name, icon);
		
		MessageConsoleStream out = console.newMessageStream();
		printStream(inStream, new PrintStream(out), () -> {
			console.setTerminated(true);
		});
		
		ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[]{console});
		showConsole(console, page);
	}

	public static void showConsole(IConsole console, IWorkbenchPage page) {
		try {
			String id = IConsoleConstants.ID_CONSOLE_VIEW;
			IConsoleView view = (IConsoleView) page.showView(id);
			view.display(console);
		} catch (PartInitException e) {
			print("Show Console PartInitException for " + console.getName());
			printStackTrace(e);
		} catch (Exception e) {
			print("Show Console Error for " + console.getName());
			printStackTrace(e);
		}
	}

	public static IConsole findConsole(String name) {
		IConsoleManager conMan = ConsolePlugin.getDefault().getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++)
			if (existing[i].getName().startsWith(name))
				return existing[i];
		return null;
	}
	
	public static PrintStream reuseConsole(ILaunch launch) {
		IProcess[] processes = launch.getProcesses();
		if (processes.length > 0) {
			IConsole c = DebugUITools.getConsole(processes[0]);
			if (c instanceof IOConsole) {
				IOConsole console = (IOConsole)c;
				return new PrintStream(console.newOutputStream());
			}
		}
		return null;
	}
	
	public static void printStream(InputStream inStream, PrintStream outStream) {
		printStream(inStream, outStream, null);
	}
	
	public static void printStream(InputStream inStream, PrintStream outStream, Runnable terminationCallback) {
		new Thread() {
			public void run() {
				BufferedReader input = new BufferedReader(new InputStreamReader(inStream));
				String line = null;
				try {
					while ((line = input.readLine()) != null) {
						outStream.println(line);
					}
					inStream.close();
					outStream.close();
					if (terminationCallback != null)
						terminationCallback.run();
				} catch (IOException e) {
					print("Print Console Stream IO Error");
					printStackTrace(e);
				} catch (Exception e) {
					print("Print Console Stream Error");
					printStackTrace(e);
				}
			}
		}.start();
	}
	
	public void addTerminationListener(Consumer<Boolean> listener) {
		terminationListeners.add(listener);
	}
	public void removeTerminationListener(Consumer<Boolean>  listener) {
		terminationListeners.remove(listener);
	}
	public void notifyTerminationListeners(boolean terminated) {
		for (Consumer<Boolean> listener : terminationListeners)
			listener.accept(terminated);
	}
}
