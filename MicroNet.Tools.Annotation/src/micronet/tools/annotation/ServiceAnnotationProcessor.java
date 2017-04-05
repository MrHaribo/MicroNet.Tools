package micronet.tools.annotation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.VariablesPlugin;

import micronet.annotation.MessageListener;
import micronet.annotation.MessageParameter;
import micronet.annotation.MessageService;
import micronet.annotation.OnStart;
import micronet.annotation.OnStop;
import micronet.api.ListenerAPI;
import micronet.api.ParameterAPI;
import micronet.api.ServiceAPI;
import micronet.serialization.Serialization;

public class ServiceAnnotationProcessor extends AbstractProcessor {

	private Types typeUtils;
	private Elements elementUtils;
	private Filer filer;
	private Messager messager;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		typeUtils = processingEnv.getTypeUtils();
		elementUtils = processingEnv.getElementUtils();
		filer = processingEnv.getFiler();
		messager = processingEnv.getMessager();
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		Set<String> annotataions = new LinkedHashSet<String>();
		annotataions.add(MessageService.class.getCanonicalName());
		return annotataions;
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

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
			
			if (!generateServiceImplementation(description)) {
				error(annotatedElement, "Error processing serviceElement:", annotatedElement.getSimpleName());
				return true; // Exit processing
			}
			
			generateAPIDescription(description);
		}
				
		return true;
	}
	
	private void generateAPIDescription(ServiceDescription description) {
		
		ServiceAPI serviceApi = new ServiceAPI();
		serviceApi.setServiceName(description.getName());
		serviceApi.setServiceUri(description.getURI());
		
		int listenerCount = description.getMessageListeners().size();
		Element[] listenerElements = description.getMessageListeners().toArray(new Element[listenerCount]);
		ListenerAPI[] listeners = new ListenerAPI[listenerCount];
		for (int i = 0; i < listeners.length; i++) {
			ListenerAPI listener = new ListenerAPI();
			
			MessageListener listenerAnnotation = listenerElements[i].getAnnotation(MessageListener.class);
			
			listener.setListenerUri(listenerAnnotation.uri());
			listener.setRequestDataType(getRequestDataTypeName(listenerAnnotation));
			listener.setResponseDataType(getResponseDataTypeName(listenerAnnotation));
			
			ParameterAPI[] requestParameters = new ParameterAPI[listenerAnnotation.requestParameters().length];
			for (int j = 0; j < requestParameters.length; j++) { 
				MessageParameter parameterAnnotation = listenerAnnotation.requestParameters()[j];
				requestParameters[j] = parseParameterAnnotation(parameterAnnotation);
			}
			listener.setRequestParameters(requestParameters);
			
			ParameterAPI[] responseParameters = new ParameterAPI[listenerAnnotation.responseParameters().length];
			for (int j = 0; j < responseParameters.length; j++) { 
				MessageParameter parameterAnnotation = listenerAnnotation.responseParameters()[j];
				responseParameters[j] = parseParameterAnnotation(parameterAnnotation);
			}
			listener.setResponseParameters(responseParameters);
			
			listeners[i] = listener;
		}
		
		serviceApi.setListeners(listeners);
		
		try {
			String apiData = Serialization.serializePretty(serviceApi);
			String apiFileName = description.getName() + "API";
						
			IDynamicVariable var = VariablesPlugin.getDefault().getStringVariableManager().getDynamicVariable("workspace_loc");
			String workspacePath = var.getValue(null);
			
			String path = workspacePath + "/shared_api/" + apiFileName;
			log(null, "Api Path:" + path);
			BufferedWriter out = new BufferedWriter(new FileWriter(path));
		    out.write(apiData);
		    out.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		} 
	}
	
	private boolean generateServiceImplementation(ServiceDescription description) {
		log(description.getService(), "Generating Service implementation: " + description.getService().getSimpleName());

		String additionalImports = "import " + description.getService().toString() + ";\n\n";
		String serviceClassName = "ServiceImpl";
		
		String startCode = generateStartCode(description);
		String stopCode = generateStopCode(description);
		String listenerCode = generateListenerCode(description);

		InputStream resourceAsStream = ServiceAnnotationProcessor.class.getResourceAsStream("ServiceTemplate.txt");
		BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream));
		
		try {

			JavaFileObject file = filer.createSourceFile(serviceClassName,  description.getService());
			Writer writer = file.openWriter();
			
			String line = reader.readLine();
			while (line != null) {
				
				line = line.replaceAll(Pattern.quote("${additional_imports}"), additionalImports);
				line = line.replaceAll(Pattern.quote("${service_class}"), serviceClassName);
				line = line.replaceAll(Pattern.quote("${service_name}"), description.getName());
				line = line.replaceAll(Pattern.quote("${service_uri}"), description.getURI());
				line = line.replaceAll(Pattern.quote("${service_variable}"), description.getServiceVariable());
				line = line.replaceAll(Pattern.quote("${peer_variable}"), description.getPeerVariable());
				line = line.replaceAll(Pattern.quote("${on_start}"), startCode);
				line = line.replaceAll(Pattern.quote("${on_stop}"), stopCode);
				line = line.replaceAll(Pattern.quote("${register_listeners}"), listenerCode);
				
				
				writer.append(line + "\n");
				line = reader.readLine();
			}
			
			
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return true;
	}

	private String generateListenerCode(ServiceDescription description) {
				
		StringBuilder code = new StringBuilder();
		
		for (Element method : description.getMessageListeners()) {
			MessageListener annotation = method.getAnnotation(MessageListener.class);
			code.append(description.getPeerVariable() + ".listen(\"" + annotation.uri() + "\", (Request request) -> ");
			code.append(description.getServiceVariable() + "." + method.getSimpleName() + "(context, request));\n");
		}
		
		return code.toString();
	}

	private String generateStopCode(ServiceDescription description) {
		StringBuilder code = new StringBuilder();
		code.append("System.out.println(\"" + description.getName() +  " stopped...\");\n");
		
		for (Element method : description.getStopMethods()) {
			code.append(description.getServiceVariable() + "." + method.getSimpleName() + "(context);\n");
		}
		
		return code.toString();
	}

	private String generateStartCode(ServiceDescription description) {
		
		StringBuilder code = new StringBuilder();
		code.append("System.out.println(\"" + description.getName() +  " started...\");\n");
		
		for (Element method : description.getStartMethods()) {
			code.append(description.getServiceVariable() + "." + method.getSimpleName() + "(context);\n");
		}
		
		return code.toString();
	}
	
	private String getTypeElementName(TypeMirror mirror) {
	    TypeElement classTypeElement = (TypeElement) typeUtils.asElement(mirror);
	    return classTypeElement.getSimpleName().toString();
	}
	
	private String getRequestDataTypeName(MessageListener listenerAnnotation) {
		try {
			return listenerAnnotation.requestDataType().toString();
		} catch (MirroredTypeException e) {
		    return getTypeElementName(e.getTypeMirror());
		}
	}

	private String getResponseDataTypeName(MessageListener listenerAnnotation) {
		try {
			return listenerAnnotation.responseDataType().toString();
		} catch (MirroredTypeException e) {
		    return getTypeElementName(e.getTypeMirror());
		}
	}
	
	private String getParameterTypeName(MessageParameter parameterAnnotation) {
		try {
			return parameterAnnotation.valueType().toString();
		} catch (MirroredTypeException e) {
		    return getTypeElementName(e.getTypeMirror());
		}
	}
	
	private ParameterAPI parseParameterAnnotation(MessageParameter parameterAnnotation) {
		ParameterAPI param = new ParameterAPI();
		param.setType(parameterAnnotation.type());
		param.setValueType(getParameterTypeName(parameterAnnotation));
		return param;
	}
	
	private void log(Element e, String msg) {
		messager.printMessage(Kind.NOTE, msg);
	}
	
	private void error(Element e, String msg, Object... args) {
		messager.printMessage(Kind.ERROR, String.format(msg, args), e);
	}
}
