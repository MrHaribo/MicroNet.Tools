package micronet.tools.annotation;

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
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;

import micronet.tools.annotation.codegen.ServiceAPIGenerator;

public class ServiceAnnotationProcessor extends AbstractProcessor implements Observer {

	private Elements elementUtils;
	
	ServiceAnnotationProcessorContext context;
	private String sharedDir;
	private String projectDir;

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
		apiGenerator.generateAPIDescription(serviceDescription, sharedDir);
		
		projectDir = findProjectDir(serviceDescription);
		System.out.println("generated: " + projectDir);
	}

	private String findProjectDir(ServiceDescription serviceDescription) {
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		for (IProject project : workspaceRoot.getProjects()) {
			if(!project.isOpen())
				continue;
			
			try {
				if (!project.hasNature(JavaCore.NATURE_ID))
					continue;
				IJavaProject javaProject = JavaCore.create(project.getProject());
				
				if (javaProject.findType(serviceDescription.getTypename()) != null) {
					return project.getLocation().toOSString();
				}
				
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return projectDir;
	}
}
