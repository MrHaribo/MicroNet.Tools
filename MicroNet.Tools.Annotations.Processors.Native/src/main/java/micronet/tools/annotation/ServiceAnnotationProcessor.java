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
		
		String packageName = processingEnv.getOptions().get("package_name");
		System.out.println(packageName);
		
		context = new ServiceAnnotationProcessorContext(processingEnv, packageName, sharedDir);
		
		String generateModelArg = processingEnv.getOptions().get("generate_model");
		if (generateModelArg != null && generateModelArg.equals("true")) {
			System.out.println("generate_model=true -> Generate global code");
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
