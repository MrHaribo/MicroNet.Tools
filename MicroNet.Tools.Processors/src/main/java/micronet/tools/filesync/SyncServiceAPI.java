package micronet.tools.filesync;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Elements;

import micronet.annotation.MessageListener;
import micronet.annotation.MessageParameter;
import micronet.annotation.RequestParameters;
import micronet.annotation.RequestPayload;
import micronet.annotation.ResponseParameters;
import micronet.annotation.ResponsePayload;
import micronet.serialization.Serialization;
import micronet.tools.annotation.ServiceDescription;
import micronet.tools.api.ListenerAPI;
import micronet.tools.api.ParameterAPI;
import micronet.tools.api.ServiceAPI;

public class SyncServiceAPI {

	private static Semaphore semaphore = new Semaphore(1);

	public static ServiceAPI generateAPIDescription(ServiceDescription description, Elements elementUtils, String sharedDir) {

		ServiceAPI serviceApi = new ServiceAPI();
		serviceApi.setServiceName(description.getName());
		serviceApi.setServiceUri(description.getURI());
		serviceApi.setDescription(description.getDescription());

		List<? extends Element> listenerElements = new ArrayList<>(description.getMessageListeners());
		List<ListenerAPI> listeners = new ArrayList<>();
		for (int i = 0; i < listenerElements.size(); i++) {
			try {
				Element listenerElement = listenerElements.get(i);
				ListenerAPI listener = generateListenerAPIDescription(description, elementUtils, listenerElement);
				listeners.add(listener);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		serviceApi.setListeners(listeners.toArray(new ListenerAPI[listeners.size()]));

		saveServiceAPI(description, serviceApi, sharedDir);

		return serviceApi;
	}

	private static ListenerAPI generateListenerAPIDescription(ServiceDescription description, Elements elementUtils,
			Element listenerElement) {
		ListenerAPI listener = new ListenerAPI();
		MessageListener listenerAnnotation = listenerElement.getAnnotation(MessageListener.class);
		listener.setListenerUri(listenerAnnotation.uri());
		try {
			listener.setDescription(listenerAnnotation.desc());
		} catch (UndeclaredThrowableException e) {
			if (!(e.getUndeclaredThrowable() instanceof NoSuchMethodException))
				e.getUndeclaredThrowable().printStackTrace();
		}
		
		RequestPayload requestPayloadAnnotation = listenerElement.getAnnotation(RequestPayload.class);
		if (requestPayloadAnnotation != null) {
			listener.setRequestPayload(readRequestPayloadType(requestPayloadAnnotation));
			listener.setRequestPayloadDescription(requestPayloadAnnotation.desc());
		}
		
		ResponsePayload responsePayloadAnnotation = listenerElement.getAnnotation(ResponsePayload.class);
		if (responsePayloadAnnotation != null) {
			listener.setResponsePayload(readResponsePayloadType(responsePayloadAnnotation));
			listener.setResponsePayloadDescription(responsePayloadAnnotation.desc());
		}

		RequestParameters requestParametersAnnotation = listenerElement.getAnnotation(RequestParameters.class);
		if (requestParametersAnnotation != null && requestParametersAnnotation.value() != null) {
			ParameterAPI[] requestParameters = new ParameterAPI[requestParametersAnnotation.value().length];
			for (int i = 0; i < requestParameters.length; i++) {
				MessageParameter messageParameterAnnotation = requestParametersAnnotation.value()[i];
				requestParameters[i] = generateParameterAPIDescription(messageParameterAnnotation);
			}
			listener.setRequestParameters(requestParameters);
		}
		
		ResponseParameters responseParametersAnnotation = listenerElement.getAnnotation(ResponseParameters.class);
		if (responseParametersAnnotation != null && responseParametersAnnotation.value() != null) {
			ParameterAPI[] responseParameters = new ParameterAPI[responseParametersAnnotation.value().length];
			for (int i = 0; i < responseParameters.length; i++) {
				MessageParameter messageParameterAnnotation = responseParametersAnnotation.value()[i];
				responseParameters[i] = generateParameterAPIDescription(messageParameterAnnotation);
			}
			listener.setResponseParameters(responseParameters);
		}
		return listener;
	}
	
	private static ParameterAPI generateParameterAPIDescription(MessageParameter messageParameterAnnotation) {
		ParameterAPI parameterAPI = new ParameterAPI();
		parameterAPI.setCode(messageParameterAnnotation.code());
		parameterAPI.setType(readMessageParameterType(messageParameterAnnotation));
		try {
			parameterAPI.setDescription(messageParameterAnnotation.desc());
		} catch (UndeclaredThrowableException e) {
			if (!(e.getUndeclaredThrowable() instanceof NoSuchMethodException))
				e.getUndeclaredThrowable().printStackTrace();
		}
		
		return parameterAPI;
	}

	public static List<ServiceAPI> readServiceApi(String sharedDir) {
		List<ServiceAPI> result = new ArrayList<>();

		try {
			semaphore.acquire();

			File apiDir = new File(sharedDir + "api/");
			File[] directoryListing = apiDir.listFiles();
			if (directoryListing == null)
				return result;

			for (File apiFile : directoryListing) {
				try {
					String data = new String(Files.readAllBytes(apiFile.toPath()), StandardCharsets.UTF_8);
					ServiceAPI apiDesc = Serialization.deserialize(data, ServiceAPI.class);
					result.add(apiDesc);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			semaphore.release();
		}
		return result;
	}
	
	public static void saveServiceAPI(ServiceDescription description, ServiceAPI serviceApi, String sharedDir) {
		try {
			String apiData = Serialization.serializePretty(serviceApi);
			String apiFileName = description.getName() + "API";

			semaphore.acquire();
			
			File apiDir = new File(sharedDir + "api/");
			if (!apiDir.exists())
				apiDir.mkdir();

			File apiFile = new File(sharedDir + "api/" + apiFileName);
			Files.write(apiFile.toPath(), apiData.getBytes());
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			semaphore.release();
		}
	}
	
	private static String readRequestPayloadType(RequestPayload payloadAnnotation) {
		try {
			return payloadAnnotation.value().getSimpleName();
		} catch (MirroredTypeException mte) {
			return parseMTE(mte);
		}
	}
	private static String readResponsePayloadType(ResponsePayload payloadAnnotation) {
		try {
			return payloadAnnotation.value().getSimpleName();
		} catch (MirroredTypeException mte) {
			return parseMTE(mte);
		}
	}
	private static String readMessageParameterType(MessageParameter messageParameterAnnotation) {
		try {
			return messageParameterAnnotation.type().getSimpleName();
		} catch (MirroredTypeException mte) {
			return parseMTE(mte);
		}
	}
	
	private static String parseMTE(MirroredTypeException mte) {
		if (mte.getTypeMirror().getKind().equals(TypeKind.ARRAY)) {
			ArrayType arrayType = (ArrayType)mte.getTypeMirror();
			return arrayType.toString().substring(arrayType.toString().lastIndexOf(".") + 1);
		}
		DeclaredType classTypeMirror = (DeclaredType) mte.getTypeMirror();
		TypeElement classTypeElement = (TypeElement) classTypeMirror.asElement();
		return classTypeElement.getSimpleName().toString();
	}
	
}
