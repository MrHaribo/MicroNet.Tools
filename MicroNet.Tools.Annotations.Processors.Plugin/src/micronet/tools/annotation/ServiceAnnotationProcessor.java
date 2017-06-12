package micronet.tools.annotation;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
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

import micronet.tools.annotation.api.ListenerAPI;
import micronet.tools.annotation.api.ParameterAPI;
import micronet.tools.annotation.api.ServiceAPI;
import micronet.tools.annotation.filesync.SyncParameterCodes;
import micronet.tools.annotation.filesync.SyncServiceAPI;
import micronet.tools.core.ModelProvider;
import micronet.tools.core.ServiceProject;
import micronet.tools.core.ServiceProject.Nature;

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
		
		String packageName = "game";
		if (serviceProject != null) {
			Set<String> contributedParameters = serviceProject.getRequiredParameters();
			SyncParameterCodes.contributeParameters(contributedParameters, sharedDir);
			
			if (serviceProject.hasNature(Nature.MAVEN)) {
				String groupString = serviceProject.getGroupID().replaceAll("[^a-zA-Z0-9]+",".");
				String artifactString = serviceProject.getArtifactID().replaceAll("[^a-zA-Z0-9]+",".");
				packageName = groupString + "." + artifactString;
			}
		}

		context = new ServiceAnnotationProcessorContext(processingEnv, packageName, sharedDir);
		
		if (processingEnv.getOptions().containsKey("no_service")) {
			context.generateGlobalCode();
		}
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		return context.getSupportedAnnotationTypes();
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return context.getSupportedSourceVersion();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		context.process(roundEnv);
		
		if (context.getServiceDescription() != null) {
			ServiceAPI apiDescription = SyncServiceAPI.generateAPIDescription(context.getServiceDescription(), elementUtils, sharedDir);
			Set<String> requiredParameters = getRequiredParameters(apiDescription);
			serviceProject.setRequiredParameters(requiredParameters);
		}
		return true;
	}

	private Set<String> getRequiredParameters(ServiceAPI apiDescription) {
		Set<String> requiredParameters = new HashSet<String>();
		for (ListenerAPI listener : apiDescription.getListeners()) {
			for (ParameterAPI parameter : listener.getRequestParameters()) {
				requiredParameters.add(parameter.getType());
			}
			for (ParameterAPI parameter : listener.getResponseParameters()) {
				requiredParameters.add(parameter.getType());
			}
		}
		return requiredParameters;
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
