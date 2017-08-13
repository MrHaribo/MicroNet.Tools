package micronet.tools.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public class Console extends MessageConsole {

	public Console(String name, ImageDescriptor imageDescriptor) {
		super(name, imageDescriptor);
	}
	
	public static void createConsole(String name, InputStream inStream, IWorkbenchPage page) {
		Console console = new Console(name, null);
		
		MessageConsoleStream out = console.newMessageStream();
		printStream(inStream, new PrintStream(out));
		
		ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[]{console});
		showConsole(console, page);
	}
	
	public static void showConsole(IConsole console, IWorkbenchPage page) {
		try {
			String id = IConsoleConstants.ID_CONSOLE_VIEW;
			IConsoleView view = (IConsoleView) page.showView(id);
			view.display(console);
		} catch (PartInitException e) {
			e.printStackTrace();
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
	
	public static PrintStream reuseConsole(String launchName) {
		IOConsole launchConsole = (IOConsole) Console.findConsole(launchName);
		IOConsoleOutputStream out = launchConsole.newOutputStream();
		return new PrintStream(out);
	}
	
	public static void printStream(InputStream inStream, PrintStream outStream) {
		new Thread() {
			public void run() {
				BufferedReader input = new BufferedReader(new InputStreamReader(inStream));
				String line = null;
				try {
					while ((line = input.readLine()) != null) {
						outStream.println(line);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
}