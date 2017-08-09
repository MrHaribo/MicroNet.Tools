package micronet.tools.api;

public class ListenerAPI {
	private String listenerUri;
	private String description;
	private ParameterAPI[] requestParameters;
	private ParameterAPI[] responseParameters;
	private String requestPayload;
	private String responsePayload;
	private String requestPayloadDescription;
	private String responsePayloadDescription;

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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getRequestPayload() {
		return requestPayload;
	}

	public void setRequestPayload(String requestPayload) {
		this.requestPayload = requestPayload;
	}

	public String getResponsePayload() {
		return responsePayload;
	}

	public void setResponsePayload(String responsePayload) {
		this.responsePayload = responsePayload;
	}

	public String getRequestPayloadDescription() {
		return requestPayloadDescription;
	}

	public void setRequestPayloadDescription(String requestPayloadDescription) {
		this.requestPayloadDescription = requestPayloadDescription;
	}

	public String getResponsePayloadDescription() {
		return responsePayloadDescription;
	}

	public void setResponsePayloadDescription(String responsePayloadDescription) {
		this.responsePayloadDescription = responsePayloadDescription;
	}

}
