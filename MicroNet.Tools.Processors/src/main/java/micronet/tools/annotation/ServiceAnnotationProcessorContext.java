package micronet.tools.annotation;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;

import micronet.annotation.MessageListener;
import micronet.annotation.MessageService;
import micronet.annotation.OnStart;
import micronet.annotation.OnStop;
import micronet.tools.codegen.ModelGenerator;
import micronet.tools.codegen.ParameterCodesGenerator;
import micronet.tools.codegen.ServiceImplGenerator;

public class ServiceAnnotationProcessorContext {
	private Filer filer;
	private Messager messager;

	private String sharedDir;
	private String packageName;

	private ServiceDescription serviceDescription;

	private boolean codeCreated = false;

	public Consumer<String> print = str -> {};
	public Consumer<String> println = str -> {};
	public Consumer<Exception> printStackTrace = e -> {};

	public enum ProcessingState {
		SERVICE_FOUND, PROCESSING_COMPLETE
	}

	public ServiceAnnotationProcessorContext(ProcessingEnvironment processingEnv, String packageName,
			String sharedDir) {
		filer = processingEnv.getFiler();
		messager = processingEnv.getMessager();

		this.packageName = packageName;
		this.sharedDir = sharedDir;
	}

	public Set<String> getSupportedAnnotationTypes() {
		Set<String> annotataions = new LinkedHashSet<String>();
		annotataions.add(MessageService.class.getCanonicalName());
		return annotataions;
	}

	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

	public void process(RoundEnvironment roundEnv) {

		try {
			Element annotatedServiceElement = null;
			for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(MessageService.class)) {
				annotatedServiceElement = annotatedElement;
				break;
			}

			if (annotatedServiceElement != null) {
				serviceDescription = new ServiceDescription();

				serviceDescription.setMessageListeners(roundEnv.getElementsAnnotatedWith(MessageListener.class));
				serviceDescription.setStartMethods(roundEnv.getElementsAnnotatedWith(OnStart.class));
				serviceDescription.setStopMethods(roundEnv.getElementsAnnotatedWith(OnStop.class));
				serviceDescription.setService(annotatedServiceElement);

				packageName = serviceDescription.getPackage();
			}

			if (!codeCreated && serviceDescription != null) {
				try {
					ServiceImplGenerator implGenerator = new ServiceImplGenerator(filer, messager);
					implGenerator.generateServiceImplementation(serviceDescription);
					codeCreated = true;
				} catch (Exception e) {
					print.accept("Error Generation Service Implementation for " + annotatedServiceElement.getSimpleName());
					printStackTrace.accept(e);
				}
			}
		} catch (Exception e) {
			print.accept("Annotation processing Round Error for " + serviceDescription);
			printStackTrace.accept(e);
		}
	}

	public void generateGlobalCode() {
		try {
			ParameterCodesGenerator parameterCodesGenerator = new ParameterCodesGenerator(filer);
			parameterCodesGenerator.generateParameterCodeEnum(packageName, sharedDir);
		} catch (Exception e) {
			print.accept("Error Generating Parameter Codes for " + serviceDescription);
			printStackTrace.accept(e);
		}
		
		try {
			ModelGenerator modelGenerator = new ModelGenerator(packageName, filer);
			modelGenerator.generateModel(sharedDir);
		} catch (Exception e) {
			print.accept("Error Generating Model for " + serviceDescription);
			printStackTrace.accept(e);
		}
	}

	public ServiceDescription getServiceDescription() {
		return serviceDescription;
	}
}
