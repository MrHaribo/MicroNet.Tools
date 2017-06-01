package micronet.tools.annotation;

import java.util.LinkedHashSet;
import java.util.Observable;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import micronet.annotation.MessageListener;
import micronet.annotation.MessageService;
import micronet.annotation.OnStart;
import micronet.annotation.OnStop;
import micronet.tools.annotation.codegen.AnnotationGenerator;
import micronet.tools.annotation.codegen.ParameterCodesGenerator;
import micronet.tools.annotation.codegen.ServiceImplGenerator;

public class ServiceAnnotationProcessorContext extends Observable {
	private Types typeUtils;
	private Elements elementUtils;
	private Filer filer;
	private Messager messager;

	private String sharedDir;

	private ServiceDescription serviceDescription;
	
	public enum ProcessingState {
		SERVICE_FOUND, PROCESSING_COMPLETE
	}

	public ServiceAnnotationProcessorContext(ProcessingEnvironment processingEnv, String sharedDir) {
		typeUtils = processingEnv.getTypeUtils();
		elementUtils = processingEnv.getElementUtils();
		filer = processingEnv.getFiler();
		messager = processingEnv.getMessager();

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

	public boolean process(RoundEnvironment roundEnv) {

		try {
			for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(MessageService.class)) {
				processRound1(annotatedElement, roundEnv);
				return true;
			}

			if (serviceDescription != null) {
				processRound2(roundEnv);
				serviceDescription = null;
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	private void processRound1(Element serviceElement, RoundEnvironment roundEnv) {
		serviceDescription = new ServiceDescription();

		serviceDescription.setMessageListeners(roundEnv.getElementsAnnotatedWith(MessageListener.class));
		serviceDescription.setStartMethods(roundEnv.getElementsAnnotatedWith(OnStart.class));
		serviceDescription.setStopMethods(roundEnv.getElementsAnnotatedWith(OnStop.class));
		serviceDescription.setService(serviceElement);

		setChanged();
		notifyObservers(ProcessingState.SERVICE_FOUND);
		
		ParameterCodesGenerator parameterCodesGenerator = new ParameterCodesGenerator(filer);
		parameterCodesGenerator.generateParameterCodeEnum(serviceDescription, sharedDir);

		AnnotationGenerator annotationGenerator = new AnnotationGenerator(filer);
		annotationGenerator.generateMessageParameterAnnotation(serviceDescription);
		annotationGenerator.generateParametersAnnotations(serviceDescription);
	}

	private void processRound2(RoundEnvironment roundEnv) {
		ServiceImplGenerator implGenerator = new ServiceImplGenerator(filer, messager);
		implGenerator.generateServiceImplementation(serviceDescription);
		
		setChanged();
		notifyObservers(ProcessingState.PROCESSING_COMPLETE);
	}

	public ServiceDescription getServiceDescription() {
		return serviceDescription;
	}
}
