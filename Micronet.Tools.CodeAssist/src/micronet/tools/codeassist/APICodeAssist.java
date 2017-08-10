package micronet.tools.codeassist;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;

import micronet.tools.api.ListenerAPI;
import micronet.tools.api.ParameterAPI;
import micronet.tools.api.ServiceAPI;
import micronet.tools.core.Icons;
import micronet.tools.core.ModelProvider;
import micronet.tools.filesync.SyncServiceAPI;

public class APICodeAssist implements IJavaCompletionProposalComputer {

	private List<ServiceAPI> fullAPI = new ArrayList<>();

	@Override
	public void sessionStarted() {
		System.out.println("Code Assist Session Start");

		String sharedDir = ModelProvider.INSTANCE.getSharedDir();
		fullAPI = SyncServiceAPI.readServiceApi(sharedDir);
	}

	@Override
	public List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext context,
			IProgressMonitor monitor) {

		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();

		try {

			String line = getCurrentLine(context);

			if (!line.contains("mn://"))
				return proposals;

			int uriRelativeStartIdx = line.indexOf("mn://");
			int uriRelativeEndIdx = line.indexOf("\"", uriRelativeStartIdx);
			int lineOffset = getLineOffset(context);
			int uriStartIdx = lineOffset + uriRelativeStartIdx;
			int uriEndIdx = lineOffset + uriRelativeEndIdx;

			if (context.getInvocationOffset() < uriStartIdx || context.getInvocationOffset() > uriEndIdx)
				return proposals;

			String currentString = line.substring(uriRelativeStartIdx, uriRelativeEndIdx);
			String pathPlain = currentString.replace("mn://", "");
			String[] tokens = pathPlain.split("/");

			int pathStartIdx = uriStartIdx + tokens[0].length() + 5;
			boolean cursorInService = context.getInvocationOffset() < pathStartIdx;

			if (cursorInService) {
				return serviceProposals(uriStartIdx, currentString.length());
			} else {

				Optional<ServiceAPI> service = fullAPI.stream()
						.filter(s -> s.getServiceUri().equals("mn://" + tokens[0])).findFirst();

				if (!service.isPresent())
					return serviceProposals(uriStartIdx, currentString.length());

				for (ListenerAPI listenerApi : service.get().getListeners()) {

					APICompletionProposal proposal = new APICompletionProposal();
					proposal.replacementString = listenerApi.getListenerUri();
					proposal.replacementOffset = pathStartIdx;
					proposal.replacementLength = uriEndIdx - pathStartIdx;
					proposal.cursorPosition = listenerApi.getListenerUri().length();
					proposal.image = Icons.IMG_LETTERBOX.createImage();
					proposal.additionalProposalInfo = getListenerDescription(service.get(), listenerApi);
					proposals.add(proposal);
				}
			}

		} catch (BadLocationException e) {
			System.out.println("Bad Matcher Location: " + context.getInvocationOffset());
		}

		return proposals;
	}

	private String getListenerDescription(ServiceAPI serviceAPI, ListenerAPI listenerApi) {
		String description = String.format("Message Listener: %s%s\n", serviceAPI.getServiceUri(), listenerApi.getListenerUri());
		
		if (listenerApi.getDescription() != null && !listenerApi.getDescription().equals(""))
			description += listenerApi.getDescription();
		description += "\n";
		
		if (listenerApi.getRequestParameters() != null) {
			description += "\nRequired Request Parameters:\n";
			for (ParameterAPI parameter : listenerApi.getRequestParameters()) {
				String descriptionString = parameter.getDescription() == null && !parameter.getDescription().equals("") ? "" : " (" + parameter.getDescription() + ")";
				description += " " + parameter.getCode() + ": " + parameter.getType() + descriptionString + "\n";
			}
		}
		if (listenerApi.getRequestPayload() != null && !listenerApi.getRequestPayload().equals("")) {
			String descriptionString = listenerApi.getRequestPayloadDescription() == null && !listenerApi.getRequestPayloadDescription().equals("") ? "" : " (" + listenerApi.getRequestPayloadDescription() + ")";
			description += "\nRequest Payload Type:\n " + listenerApi.getRequestPayload() + descriptionString + "\n";
		}
			
		if (listenerApi.getResponseParameters() != null) {
			description += "\nProvided Response Parameters:\n";
			for (ParameterAPI parameter : listenerApi.getResponseParameters()) {
				String descriptionString = parameter.getDescription() == null && !parameter.getDescription().equals("") ? "" : " (" + parameter.getDescription() + ")";
				description += " " + parameter.getCode() + ": " + parameter.getType() + descriptionString + "\n";
			}
		}
		if (listenerApi.getResponsePayload() != null && !listenerApi.getResponsePayload().equals("")) {
			String descriptionString = listenerApi.getResponsePayloadDescription() == null && !listenerApi.getResponsePayloadDescription().equals("") ? "" : " (" + listenerApi.getResponsePayloadDescription() + ")";
			description += "\nResponse Payload Type:\n " + listenerApi.getResponsePayload() + descriptionString + "\n";
		}
		
		return description;
	}

	private List<ICompletionProposal> serviceProposals(int replacementOffset, int replacementLength) {
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		for (ServiceAPI serviceApi : fullAPI) {

			APICompletionProposal proposal = new APICompletionProposal();
			proposal.replacementString = serviceApi.getServiceUri();
			proposal.replacementOffset = replacementOffset;
			proposal.replacementLength = replacementLength;
			proposal.cursorPosition = serviceApi.getServiceUri().length();
			proposal.image = Icons.IMG_MICRONET.createImage();
			proposal.additionalProposalInfo = "Service: " + serviceApi.getServiceName() + "\n";
			proposal.additionalProposalInfo += "URI: " + serviceApi.getServiceUri() + "\n";
			
			if (serviceApi.getDescription() != null && !serviceApi.getDescription().equals(""))
			proposal.additionalProposalInfo += "\n" + serviceApi.getDescription();
			proposals.add(proposal);
		}
		return proposals;
	}

	@Override
	public List<IContextInformation> computeContextInformation(ContentAssistInvocationContext context,
			IProgressMonitor monitor) {

		ContextInformation info = new ContextInformation("Context", "Info");
		List<IContextInformation> proposals = new ArrayList<IContextInformation>();
		proposals.add(info);
		System.out.println("Complete Proposals Info");

		return proposals;
	}

	@Override
	public String getErrorMessage() {

		System.out.println("Compute Complete Proposals Error Msg");
		return null;
	}

	@Override
	public void sessionEnded() {
		System.out.println("Session End");
	}

	protected String getCurrentLine(final ContentAssistInvocationContext context) throws BadLocationException {
		IDocument document = context.getDocument();
		int lineNumber = document.getLineOfOffset(context.getInvocationOffset());
		IRegion lineInformation = document.getLineInformation(lineNumber);
		return document.get(lineInformation.getOffset(), lineInformation.getLength());
	}

	protected int getLineOffset(final ContentAssistInvocationContext context) throws BadLocationException {
		IDocument document = context.getDocument();
		int lineNumber = document.getLineOfOffset(context.getInvocationOffset());
		IRegion lineInformation = document.getLineInformation(lineNumber);
		return lineInformation.getOffset();
	}
}
