package micronet.tools.annotation;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

public class ServiceAnnotationProcessor extends AbstractProcessor {

	ServiceAnnotationProcessorContext context;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		String workspacePath = processingEnv.getOptions().get("workspace_path");
		String sharedDir = workspacePath + "/shared/";
		
		String groupID = processingEnv.getOptions().get("group_id");
		String artifactID = processingEnv.getOptions().get("artifact_id");
		String packageName = groupID + "." + artifactID;
		System.out.println(packageName);
		
		context = new ServiceAnnotationProcessorContext(processingEnv, packageName, sharedDir);
		
		
		if (processingEnv.getOptions().containsKey("no_service")) {
			System.out.println("Generating global code for no_ervice");
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
		return true;
	}
}
