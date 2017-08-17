package micronet.tools.contribution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Element;

import micronet.annotation.MessageListener;
import micronet.tools.annotation.ServiceDescription;
import micronet.tools.api.ListenerAPI;
import micronet.tools.api.ServiceAPI;

public class ParameterCodeParser {

	private Map<String, List<String>> requestParameters = new HashMap<>();
	private Map<String, List<String>> responseParameters = new HashMap<>();
	
	ServiceDescription serviceDescription;
	String codefile;
	
	public ParameterCodeParser(ServiceDescription serviceDescription, String codefile) {
		this.serviceDescription = serviceDescription;
		this.codefile = codefile;
	}
	
	public ServiceAPI parseParameterCodes(ServiceAPI api) {
		parseParameterCodes(codefile, serviceDescription);

		for (ListenerAPI listener : api.getListeners()) {
			if (listener.getRequestParameters() != null) {
				List<String> requestParams = requestParameters.get(listener.getListenerUri());
				for (int i = 0; i < listener.getRequestParameters().length; i++) {
					listener.getRequestParameters()[i].setCode(requestParams.get(i));
				}
			}

			if (listener.getResponseParameters() != null) {
				List<String> responseParams = responseParameters.get(listener.getListenerUri());
				for (int i = 0; i < listener.getResponseParameters().length; i++) {
					listener.getResponseParameters()[i].setCode(responseParams.get(i));
				}
			}
		}
		
		return api;
	}
	
	private void parseParameterCodes(String data, ServiceDescription serviceDescription) {
		
		Map<String, Integer> methodIndices = new HashMap<>();
		List<String> methodOrder = new ArrayList<>();
		
		int currentIndex = 0;
		for (int i = 0; i < serviceDescription.getMessageListeners().size(); i++) {
			
			
			currentIndex = data.indexOf("@MessageListener", currentIndex);
			
			int closestDistance = Integer.MAX_VALUE;
			Element closestElement = null;
		
			for (Element e : serviceDescription.getMessageListeners()) {
				
				
				int methodIndex = data.indexOf(e.getSimpleName().toString(), currentIndex);
				int distance = methodIndex - currentIndex;
				
				if (distance > 0 && distance < closestDistance) {
					closestDistance = distance;
					closestElement = e;
				}
			}
			
			if (closestElement == null)
				continue;
			
			MessageListener annotation = closestElement.getAnnotation(MessageListener.class);
			
			methodIndices.put(annotation.uri(), currentIndex + closestDistance);
			methodOrder.add(annotation.uri());
			
			currentIndex++;
		}
		
		for (int i = 0; i < methodOrder.size(); i++) {
			
			String previousMethod =  i == 0 ? null : methodOrder.get(i-1);
			String currentMethod = methodOrder.get(i);
			
			requestParameters.put(currentMethod, new ArrayList<>());
			responseParameters.put(currentMethod, new ArrayList<>());
			
			int previousIndex = previousMethod == null ? 0 : methodIndices.get(previousMethod);
			int methodIndex = methodIndices.get(currentMethod);
			
			String prefixString = data.substring(previousIndex, methodIndex);
			parseParameterLists(currentMethod, prefixString);
		}
	}
	
	private void parseParameterLists(String currentMethod, String dataFragment) {
		
		int requestParameterListIndex = dataFragment.lastIndexOf("@RequestParameters");
		int responseParameterListIndex = dataFragment.lastIndexOf("@ResponseParameters");
		
		if (requestParameterListIndex != -1) {
			if (responseParameterListIndex != -1) {
				if (requestParameterListIndex < responseParameterListIndex) {
					requestParameters.put(currentMethod, readParameterList(dataFragment.substring(requestParameterListIndex, responseParameterListIndex)));
					responseParameters.put(currentMethod,readParameterList(dataFragment.substring(responseParameterListIndex)));
				} else {
					requestParameters.put(currentMethod, readParameterList(dataFragment.substring(requestParameterListIndex)));
					responseParameters.put(currentMethod,readParameterList(dataFragment.substring(responseParameterListIndex, requestParameterListIndex)));
				}
			} else {
				requestParameters.put(currentMethod, readParameterList(dataFragment));
			}
		} else if (responseParameterListIndex != -1) {
			responseParameters.put(currentMethod,readParameterList(dataFragment));
		}
		System.out.println("");
	}
	
	private List<String> readParameterList(String dataFragment) {
		
		List<String> params = new ArrayList<>();
		
		int currentIndex = 0;
		while (currentIndex != -1) {
			currentIndex = dataFragment.indexOf("@MessageParameter", currentIndex);
			if (currentIndex == -1)
				break;
			
			int codeStart = dataFragment.indexOf("=", currentIndex);
			int codeEnd = dataFragment.indexOf(",", currentIndex);
			String codeSnipplet = dataFragment.substring(codeStart, codeEnd);
			codeSnipplet = codeSnipplet.replaceAll("\\p{Punct}ParameterCode\\p{Punct}", "");
			params.add(codeSnipplet);
			
			if (currentIndex != -1)
				currentIndex++;
		}
		return params;
	}
}
