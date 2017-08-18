package micronet.tools.annotation;

import java.io.File;
import java.nio.file.Files;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;

import micronet.tools.api.ServiceAPI;
import micronet.tools.console.Console;
import micronet.tools.contribution.ModelContribution;
import micronet.tools.contribution.ParameterCodeParser;
import micronet.tools.core.ModelProvider;
import micronet.tools.core.ServiceProject;
import micronet.tools.filesync.SyncServiceAPI;

public class ServiceAnnotationProcessor extends AbstractProcessor {

	private Elements elementUtils;
	private ServiceAnnotationProcessorContext context;
	private String sharedDir;
	private ServiceProject serviceProject;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);

		elementUtils = processingEnv.getElementUtils();

		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		sharedDir = workspaceRoot.getLocation().toOSString() + "/shared/";

		String projectName = findProjectName(processingEnv.getFiler());
		serviceProject = ModelProvider.INSTANCE.getServiceProject(projectName);

		String packageName = null;
		if (serviceProject != null) {
			String contributedSharedDir = serviceProject.getContributedSharedDir();
			if (serviceProject.isSharedDirContributionEnabled() && contributedSharedDir != null) {
				try {
					ModelContribution.contributeSharedDir(contributedSharedDir, sharedDir);
					ModelProvider.INSTANCE.notifyModelChangedListeners();
					serviceProject.setSharedDirContributionEnabled(false);
				} catch (Exception e) {
					Console.println("Annotation Processing Init Exception: Contributing Shared Dir for " + serviceProject.getName());
					Console.printStackTrace(e);
				}
			}

			packageName = serviceProject.getPackageName();
		}

		if (packageName == null)
			return;

		context = new ServiceAnnotationProcessorContext(processingEnv, packageName, sharedDir);
		context.print = str -> Console.print(str);
		context.println = str -> Console.println(str);
		context.printStackTrace = e -> Console.printStackTrace(e);

		String generateModelArg = processingEnv.getOptions().get("generate_model");
		if (generateModelArg != null && generateModelArg.equals("true")) {
			try {
				context.generateGlobalCode();
			} catch (Exception e) {
				Console.println("Annotation Processing Init Exception: Generate Global Code for " + serviceProject.getName());
				Console.printStackTrace(e);
			}
		}
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		if (context == null)
			return null;
		return context.getSupportedAnnotationTypes();
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		if (context == null)
			return null;
		return context.getSupportedSourceVersion();
	}
	
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (context == null)
			return false;
		
		try {
			context.process(roundEnv);
		} catch (Exception e) {
			Console.println("Annotation Processing Exception: Processing Error for " + serviceProject.getName());
			Console.printStackTrace(e);
		}
		
		if (context.getServiceDescription() != null) {
			try {
				ServiceAPI api = SyncServiceAPI.generateAPIDescription(context.getServiceDescription(), elementUtils, sharedDir);

				String fileName = context.getServiceDescription().getName() + ".java";
				String srcPath = "src/main/java";
				String packagePath = context.getServiceDescription().getPackage().replace(".", "/");
				File codeFile = new File(serviceProject.getProject().getLocation().append(srcPath).append(packagePath).append(fileName).toString());
				String data = new String(Files.readAllBytes(codeFile.toPath()));
				
				ParameterCodeParser parser = new ParameterCodeParser(context.getServiceDescription(), data);
				parser.println = str -> Console.println(str);
				parser.printStackTrace = e -> Console.printStackTrace(e);
				api = parser.parseParameterCodes(api);
				SyncServiceAPI.saveServiceAPI(context.getServiceDescription(), api, sharedDir);
			} catch (Exception e) {
				Console.println("Annotation Processing Exception: Generating API Description for " + serviceProject.getName());
				Console.printStackTrace(e);
			}
			
		}
		return true;
	}
	
	

	private String findProjectName(Filer filer) {
		try {
			FileObject resource = filer.getResource(StandardLocation.SOURCE_OUTPUT, "", "");
			File file = new File(resource.toUri().getPath().replaceAll(resource.getName(), ""));
			return file.getName();
		} catch (Exception e) {
			Console.println("Annotation Processing Exception: Error finding Service Project Location");
			Console.printStackTrace(e);
		}
		return null;
	}
}
