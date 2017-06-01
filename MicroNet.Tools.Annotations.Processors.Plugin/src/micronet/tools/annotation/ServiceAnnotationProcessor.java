package micronet.tools.annotation;

import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import micronet.tools.annotation.api.ListenerAPI;
import micronet.tools.annotation.api.ParameterAPI;
import micronet.tools.annotation.api.ServiceAPI;
import micronet.tools.annotation.codegen.ServiceAPIGenerator;
import micronet.tools.core.ModelProvider;
import micronet.tools.core.ServiceProject;

public class ServiceAnnotationProcessor extends AbstractProcessor implements Observer {

	private Elements elementUtils;

	ServiceAnnotationProcessorContext context;
	private String sharedDir;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);

		elementUtils = processingEnv.getElementUtils();

		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		sharedDir = workspaceRoot.getLocation().toOSString() + "/shared/";

		context = new ServiceAnnotationProcessorContext(processingEnv, sharedDir);
		context.addObserver(this);
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
		return context.process(roundEnv);
	}

	@Override
	public void update(Observable o, Object arg) {
		System.out.println("Annotation Processing ended");

		ServiceDescription serviceDescription = (ServiceDescription) arg;
		ServiceAPIGenerator apiGenerator = new ServiceAPIGenerator(elementUtils);
		ServiceAPI apiDescription = apiGenerator.generateAPIDescription(serviceDescription, sharedDir);

		IProject project = findProject(serviceDescription);
		
		ServiceProject serviceProject = ModelProvider.INSTANCE.getServiceProject(project.getName());
		System.out.println("generated: " + serviceProject.getPath().toOSString());

		Set<String> requiredParameters = getRequiredParameters(apiDescription);
		serviceProject.setRequiredParameters(requiredParameters);
	}

	private Set<String> getRequiredParameters(ServiceAPI apiDescription) {
		Set<String> requiredParameters = new HashSet<String>();
		for (ListenerAPI listener : apiDescription.getListeners()) {
			for (ParameterAPI parameter : listener.getRequestParameters()) {
				requiredParameters.add(parameter.getType());
			}
		}
		return requiredParameters;
	}
	
	private IProject findProject(ServiceDescription serviceDescription) {
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		for (IProject project : workspaceRoot.getProjects()) {
			if(!project.isOpen())
				continue;
			
			try {
				if (!project.hasNature(JavaCore.NATURE_ID))
					continue;
				IJavaProject javaProject = JavaCore.create(project.getProject());
				
				if (javaProject.findType(serviceDescription.getTypename()) != null) {
					return project;
				}
				
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}
}
