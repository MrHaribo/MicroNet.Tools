package micronet.tools.annotation.codegen;

import static micronet.tools.annotation.codegen.CodegenConstants.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import micronet.annotation.MessageListener;
import micronet.serialization.Serialization;
import micronet.tools.annotation.ServiceDescription;
import micronet.tools.annotation.api.ListenerAPI;
import micronet.tools.annotation.api.ParameterAPI;
import micronet.tools.annotation.api.ServiceAPI;

public class ServiceAPIGenerator {
	
	private Elements elementUtils;
	
	public ServiceAPIGenerator(Elements elementUtils) {
		this.elementUtils = elementUtils;
	}

	public ServiceAPI generateAPIDescription(ServiceDescription description, String sharedDir) {
		
		ServiceAPI serviceApi = new ServiceAPI();
		serviceApi.setServiceName(description.getName());
		serviceApi.setServiceUri(description.getURI());
		
		int listenerCount = description.getMessageListeners().size();
		List<? extends Element> listenerElements = new ArrayList<>(description.getMessageListeners());		
		ListenerAPI[] listeners = new ListenerAPI[listenerCount];
		for (int i = 0; i < listeners.length; i++) {
			ListenerAPI listener = new ListenerAPI();
			
			MessageListener listenerAnnotation = listenerElements.get(i).getAnnotation(MessageListener.class);
			listener.setListenerUri(listenerAnnotation.uri());
			
			System.out.println("Listener: " + listenerElements.get(i).getSimpleName().toString());
			
			for (AnnotationMirror annotationMirror : listenerElements.get(i).getAnnotationMirrors()) {
				System.out.println(annotationMirror.getAnnotationType().toString());
			}
			
			List<String> requestParameterList = readParameterList(REQUEST_PARAMETERS, listenerElements.get(i), description);
			ParameterAPI[] requestParameters = new ParameterAPI[requestParameterList.size()];
			for (int j = 0; j < requestParameters.length; j++) {
				requestParameters[j] = new ParameterAPI();
				requestParameters[j].setType(requestParameterList.get(j));
			}
			listener.setRequestParameters(requestParameters);
			
			List<String> responseParameterList = readParameterList(RESPONSE_PARAMETERS, listenerElements.get(i), description);
			ParameterAPI[] responseParameters = new ParameterAPI[responseParameterList.size()];
			for (int j = 0; j < responseParameters.length; j++) { 
				responseParameters[j] = new ParameterAPI();
				responseParameters[j].setType(responseParameterList.get(j));
			}
			listener.setResponseParameters(responseParameters);
			
			listeners[i] = listener;
		}
		
		serviceApi.setListeners(listeners);
		
		try {
			String apiData = Serialization.serializePretty(serviceApi);
			String apiFileName = description.getName() + "API";
						
			String path = sharedDir + "api/" + apiFileName;
			System.out.println("Api Path:" + path);
			BufferedWriter out = new BufferedWriter(new FileWriter(path));
		    out.write(apiData);
		    out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return serviceApi;
	}
	
	@SuppressWarnings("unchecked")
	private List<String> readParameterList(String parameterType, Element listenerElement, ServiceDescription description) {
		List<String> parameterList = new ArrayList<>();
		TypeElement parameterTypeElement = elementUtils.getTypeElement(description.getPackage() + "." + parameterType);
		if (parameterTypeElement == null)
			return parameterList;
		
		AnnotationMirror parameterListMirror = getAnnotationMirrorFromElementByName(listenerElement, parameterTypeElement.toString());
		if (parameterListMirror == null)
			return parameterList;
		
		AnnotationValue value = getFieldFromAnnotationMirror(parameterListMirror, "value");
		if (value == null)
			return parameterList;
		
		List<AnnotationValue> paramList = (List<AnnotationValue>) value.getValue();
		for (AnnotationValue param : paramList) {
			AnnotationMirror paramMiror = (AnnotationMirror) param.getValue();
			AnnotationValue paramValue = getFieldFromAnnotationMirror(paramMiror, "value");
			
			if (paramValue.getValue().toString().equals("<error>"))
				continue;
			
			parameterList.add(paramValue.getValue().toString());
		}
		return parameterList;
	}
	
	private AnnotationMirror getAnnotationMirrorFromElementByName(Element elem, String name) {
		for (AnnotationMirror annotationMirror : elem.getAnnotationMirrors()) {
			if (!annotationMirror.getAnnotationType().toString().contains(name))
				continue;
			return annotationMirror;
		}
		return null;
	}

	private AnnotationValue getFieldFromAnnotationMirror(AnnotationMirror mirror, String fieldName) {
		Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = mirror.getElementValues();
		
		System.out.println("Checking Field: " + mirror.getAnnotationType().toString() + ":" + fieldName + " > " + mirror.getElementValues().size());
		
		for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : elementValues.entrySet()) {
			System.out.println("Checking Field: " + entry.getKey().toString());
			
			if (entry.getKey().getSimpleName().toString().equals(fieldName)) {
				return entry.getValue();
			}
		}
		return null;
	}
	
//	private String getTypeElementName(TypeMirror mirror) {
//	    TypeElement classTypeElement = (TypeElement) typeUtils.asElement(mirror);
//	    return classTypeElement.getSimpleName().toString();
//	}
	
//	private String getParameterTypeName(MessageParameter parameterAnnotation) {
//		try {
//			return parameterAnnotation.valueType().toString();
//		} catch (MirroredTypeException e) {
//		    return getTypeElementName(e.getTypeMirror());
//		}
//	}
//	
//	private ParameterAPI parseParameterAnnotation(MessageParameter parameterAnnotation) {
//		ParameterAPI param = new ParameterAPI();
//		param.setType(parameterAnnotation.type());
//		param.setValueType(getParameterTypeName(parameterAnnotation));
//		return param;
//	}
}
