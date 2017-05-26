package micronet.tools.launch.utility;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;

public final class BuildGameComposeUtility {
	
	public static InputStream buildGame() {
		
		List<String> argArray = new ArrayList<>();
		argArray.add("docker-compose");
		argArray.add("build");
		
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		String composePath = workspaceRoot.getLocation().toOSString();
		
		ProcessBuilder builder = new ProcessBuilder(argArray);
		builder.directory(new File(composePath));
		builder.redirectErrorStream(true);

		try {
			Process process = builder.start();
			return process.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
