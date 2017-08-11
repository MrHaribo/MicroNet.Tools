package micronet.tools.annotation;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;

import micronet.tools.api.ServiceAPI;
import micronet.tools.contribution.ModelContribution;
import micronet.tools.core.ModelProvider;
import micronet.tools.core.ServiceProject;
import micronet.tools.core.ServiceProject.Nature;
import micronet.tools.filesync.SyncParameterCodes;
import micronet.tools.filesync.SyncServiceAPI;

public class ServiceAnnotationProcessor extends AbstractProcessor {

	private Elements elementUtils;
	private Types typeUtils;
	private ServiceAnnotationProcessorContext context;
	private String sharedDir;
	private ServiceProject serviceProject;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		
		elementUtils = processingEnv.getElementUtils();
		typeUtils = processingEnv.getTypeUtils();
		
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		sharedDir = workspaceRoot.getLocation().toOSString() + "/shared/";
		
		String projectName = findProjectName(processingEnv.getFiler());
		serviceProject = ModelProvider.INSTANCE.getServiceProject(projectName);
		
		String packageName = null;
		if (serviceProject != null) {
			Set<String> contributedParameters = serviceProject.getRequiredParameters();
			SyncParameterCodes.contributeParameters(contributedParameters, sharedDir);
			
			String contributedSharedDir = serviceProject.getContributedSharedDir();
			if (contributedSharedDir != null) {
				ModelContribution.contributeSharedDir(contributedSharedDir, sharedDir);
				ModelProvider.INSTANCE.notifyTemplatesChangedListeners();
			}
			
			if (serviceProject.hasNature(Nature.MAVEN)) {
				String groupString = serviceProject.getGroupID().replaceAll("[^a-zA-Z0-9]+",".");
				String artifactString = serviceProject.getArtifactID().replaceAll("[^a-zA-Z0-9]+",".");
				if (!groupString.equals("") && !artifactString.equals(""))
					packageName = groupString + "." + artifactString;
			}
		}
		
		if (packageName == null)
			return;

		context = new ServiceAnnotationProcessorContext(processingEnv, packageName, sharedDir);
		
		String generateModelArg = processingEnv.getOptions().get("generate_model");
		if (generateModelArg != null && generateModelArg.equals("true")) {
			System.out.println("generate_model=true -> Generate global code");
			context.generateGlobalCode();
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
		
		context.process(roundEnv);

		if (context.getServiceDescription() != null) {
			ServiceAPI apiDescription = SyncServiceAPI.generateAPIDescription(context.getServiceDescription(), elementUtils, sharedDir);
			Set<String> requiredParameters = SyncParameterCodes.getUsedParameters(apiDescription);
			serviceProject.setRequiredParameters(requiredParameters);
		}
		return true;
	}

	
	private String findProjectName(Filer filer) {
		try {
			FileObject resource = filer.getResource(StandardLocation.SOURCE_OUTPUT, "", "");
			File file = new File(resource.toUri().getPath().replaceAll(resource.getName(), ""));
			return file.getName();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
