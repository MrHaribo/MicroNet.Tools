package micronet.tools.composition;

import java.io.File;
import java.util.List;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;

import micronet.tools.core.ServiceProject;
import micronet.tools.yaml.utility.YamlUtility;

public class SyncCompose {
	
	public static void updateGameCompose(List<ServiceProject> serviceProjects) {
		ComposeFile composeFile = new ComposeFile("3");

		for (ServiceProject serviceProject : serviceProjects) {
			ComposeService composeService = new ComposeService();
			composeService.setImage(serviceProject.getName().toLowerCase());
			composeService.setBuild(serviceProject.getRelativePath());
			
			if (serviceProject.getPorts().size() > 0)
				composeService.setPorts((String[]) serviceProject.getPorts().toArray(new String[serviceProject.getPorts().size()]));
			
			if (serviceProject.getNetwork() != null) {
				String[] networks = new String[] {serviceProject.getNetwork()};				
				composeService.setNetworks(networks);
			}
			
			composeFile.getServices().put(serviceProject.getName().toLowerCase(), composeService);
			
			ExternalNetwork network = new ExternalNetwork();
			network.getExternal().put("name", serviceProject.getNetwork());
			
			composeFile.getNetworks().put(serviceProject.getNetwork(), network);
		}
		
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
