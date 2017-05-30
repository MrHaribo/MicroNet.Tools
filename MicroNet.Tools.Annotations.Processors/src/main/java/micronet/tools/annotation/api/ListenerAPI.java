package micronet.tools.annotation.api;

public class ListenerAPI {
    private String listenerUri;
    private ParameterAPI[] requestParameters;
    private ParameterAPI[] responseParameters;
    
	public String getListenerUri() {
		return listenerUri;
	}
	public void setListenerUri(String listenerUri) {
		this.listenerUri = listenerUri;
	}
	public ParameterAPI[] getRequestParameters() {
		return requestParameters;
	}
	public void setRequestParameters(ParameterAPI[] requestParameters) {
		this.requestParameters = requestParameters;
	}
	public ParameterAPI[] getResponseParameters() {
		return responseParameters;
	}
	public void setResponseParameters(ParameterAPI[] responseParameters) {
		this.responseParameters = responseParameters;
	}
    
    
}
