package micronet.tools.contribution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.lang.model.element.Element;

import micronet.annotation.MessageListener;
import micronet.tools.annotation.ServiceDescription;
import micronet.tools.api.ListenerAPI;
import micronet.tools.api.ServiceAPI;

public class ParameterCodeParser {

	private Map<String, List<String>> requestParameters = new HashMap<>();
	private Map<String, List<String>> responseParameters = new HashMap<>();
	
	private ServiceDescription serviceDescription;
	private String codefile;
	
	public Consumer<String> println = str -> { };
	public Consumer<Exception> printStackTrace = e -> { };
	
	public ParameterCodeParser(ServiceDescription serviceDescription, String codefile) {
		this.serviceDescription = serviceDescription;
		this.codefile = codefile;
	}
	
	public ServiceAPI parseParameterCodes(ServiceAPI api) {
		parseParameterCodes(codefile, serviceDescription);

		for (ListenerAPI listener : api.getListeners()) {
			try {
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
			} catch (Exception e) {
				println.accept("Error Parsing Parameter Codes from File: " + api.getServiceUri() + listener.getListenerUri());
				printStackTrace.accept(e);
			}
		}
		
		return api;
	}
	
	private void parseParameterCodes(String data, ServiceDescription serviceDescription) {
		
		if (serviceDescription.getName().equals("VoteService")) {
			System.out.println("");
		}
		
		Map<String, Integer> methodIndices = new HashMap<>();
		Map<Integer, String> methodOrderMap = new TreeMap<>();
		
		
		for (Element e : serviceDescription.getMessageListeners()) {
			MessageListener annotation = e.getAnnotation(MessageListener.class);
			
			String regex = "(public|protected|private|static|\\s) +[\\w\\<\\>\\[\\]]+\\s+(" + e.getSimpleName().toString() + ") *\\([^\\)]*\\) *(\\{?|[^;])";
			
			Pattern p = Pattern.compile(regex);  // insert your pattern here
			Matcher m = p.matcher(data);
			if (m.find()) {
				int methodIndex = m.start();
				methodIndices.put(annotation.uri(), methodIndex);
				methodOrderMap.put(methodIndex, annotation.uri());
			}
		}
		
		List<String> methodOrder = new ArrayList<>(methodOrderMap.values());
		for (int i = 0; i < methodOrder.size(); i++) {
			
			String previousMethod = i == 0 ? null : methodOrder.get(i-1);
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
