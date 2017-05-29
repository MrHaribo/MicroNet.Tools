package micronet.tools.annotation.codegen;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

import micronet.annotation.MessageListener;
import micronet.annotation.MessageParameter;
import micronet.api.ListenerAPI;
import micronet.api.ParameterAPI;
import micronet.api.ServiceAPI;
import micronet.serialization.Serialization;
import micronet.tools.annotation.ServiceDescription;

public class ServiceAPIGenerator {
	
	private Types typeUtils;
	
	public ServiceAPIGenerator(Types typeUtils) {
		this.typeUtils = typeUtils;
	}

	public void generateAPIDescription(ServiceDescription description, String workspacePath) {
		
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
						
			String path = workspacePath + "/shared_api/" + apiFileName;
			System.out.println("Api Path:" + path);
			BufferedWriter out = new BufferedWriter(new FileWriter(path));
		    out.write(apiData);
		    out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
}
