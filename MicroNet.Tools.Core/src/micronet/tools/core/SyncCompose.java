package micronet.tools.core;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;

import micronet.tools.yaml.utility.YamlUtility;

public class SyncCompose {
	
	public static void updateGameCompose(List<ServiceProject> serviceProjects) {
		Map<String, ComposeService> composeServices = new HashMap<>();
		for (ServiceProject serviceProject : serviceProjects) {
			ComposeService composeService = new ComposeService();
			composeService.setImage(serviceProject.getName().toLowerCase());
			composeService.setBuild("./" + serviceProject.getName());
			
			if (serviceProject.getLinks().size() > 0)
				composeService.setLinks((String[]) serviceProject.getLinks().toArray(new String[serviceProject.getLinks().size()]));
			
			if (serviceProject.getPorts().size() > 0)
				composeService.setPorts((String[]) serviceProject.getPorts().toArray(new String[serviceProject.getPorts().size()]));
			
			composeServices.put(serviceProject.getName().toLowerCase(), composeService);
		}
		ComposeFile composeFile = new ComposeFile("3");
		composeFile.setServices(composeServices);
		
		saveComposeFile(composeFile);
	}
	
	public static boolean isServiceInCompose(ServiceProject serviceProject) {
		return loadComposeFile().getServices().containsKey(serviceProject.getName().toLowerCase());
	}
	
	public static ComposeFile loadComposeFile() {
		IWorkspaceRoot myWorkspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		String ymlFilepath = myWorkspaceRoot.getLocation().append("docker-compose.yml").toOSString();
		return YamlUtility.readYaml(new File(ymlFilepath), ComposeFile.class);
	}
	
	public static void saveComposeFile(ComposeFile composeFile) {
		IWorkspaceRoot myWorkspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		String ymlFilepath = myWorkspaceRoot.getLocation().append("docker-compose.yml").toOSString();
		YamlUtility.writeYaml(new File(ymlFilepath), composeFile);
	}
}
