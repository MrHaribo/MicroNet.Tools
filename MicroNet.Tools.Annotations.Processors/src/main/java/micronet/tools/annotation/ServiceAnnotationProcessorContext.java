package micronet.tools.annotation;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;

import micronet.annotation.MessageListener;
import micronet.annotation.MessageService;
import micronet.annotation.OnStart;
import micronet.annotation.OnStop;
import micronet.tools.annotation.codegen.ServiceAPIGenerator;
import micronet.tools.annotation.codegen.ServiceImplGenerator;

public class ServiceAnnotationProcessorContext {
	private Types typeUtils;
	//private Elements elementUtils;
	private Filer filer;
	private Messager messager;

	private String workspacePath;

	public ServiceAnnotationProcessorContext(ProcessingEnvironment processingEnv, String workspacePath) {
		typeUtils = processingEnv.getTypeUtils();
		//elementUtils = processingEnv.getElementUtils();
		filer = processingEnv.getFiler();
		messager = processingEnv.getMessager();
		this.workspacePath = workspacePath;
	}

	public Set<String> getSupportedAnnotationTypes() {
		Set<String> annotataions = new LinkedHashSet<String>();
		annotataions.add(MessageService.class.getCanonicalName());
		return annotataions;
	}

	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

	public boolean processServiceAnnotations(RoundEnvironment roundEnv) {
		ServiceDescription description = new ServiceDescription();

		description.setMessageListeners(roundEnv.getElementsAnnotatedWith(MessageListener.class));
		description.setStartMethods(roundEnv.getElementsAnnotatedWith(OnStart.class));
		description.setStopMethods(roundEnv.getElementsAnnotatedWith(OnStop.class));

		for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(MessageService.class)) {
			// Check if a class has been annotated with @Factory
			if (annotatedElement.getKind() != ElementKind.CLASS) {
				error(annotatedElement, "Only classes can be annotated with @%s", MessageService.class.getSimpleName());
				return true; // Exit processing
			}
			description.setService(annotatedElement);
			
			ServiceImplGenerator implGenerator = new ServiceImplGenerator(filer, messager);
			implGenerator.generateServiceImplementation(description);

			ServiceAPIGenerator apiGenerator = new ServiceAPIGenerator(typeUtils);
			apiGenerator.generateAPIDescription(description, workspacePath);
			break;
		}

		return true;
	}

	private void error(Element e, String msg, Object... args) {
		messager.printMessage(Kind.ERROR, String.format(msg, args), e);
	}

}
