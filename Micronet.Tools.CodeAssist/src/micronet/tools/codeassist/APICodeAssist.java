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
				return fullServiceProposal(uriStartIdx, currentString.length());
			} else {

				Optional<ServiceAPI> service = fullAPI.stream()
						.filter(s -> s.getServiceUri().equals("mn://" + tokens[0])).findFirst();

				if (!service.isPresent())
					return fullServiceProposal(uriStartIdx, currentString.length());

				for (ListenerAPI listenerApi : service.get().getListeners()) {
					
					String replacementString = listenerApi.getListenerUri();
					int replacementOffset = pathStartIdx;
					int replacementLength = uriEndIdx - pathStartIdx;
					int cursorPosition = listenerApi.getListenerUri().length();
					Image image = Icons.IMG_LETTERBOX.createImage();
					String displayString = replacementString;
					String additionalProposalInfo = "Additional Proposal Info";
					
					proposals.add(new CompletionProposal(replacementString, replacementOffset, replacementLength, cursorPosition, image, displayString, null, additionalProposalInfo));
				}
			}

		} catch (BadLocationException e) {
			System.out.println("Bad Matcher Location: " + context.getInvocationOffset());
		}

		return proposals;
	}

	List<ICompletionProposal> fullServiceProposal(int replacementOffset, int replacementLength) {
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		for (ServiceAPI serviceApi : fullAPI) {
			
			String replacementString = serviceApi.getServiceUri();
			int cursorPosition = serviceApi.getServiceUri().length();
			Image image = Icons.IMG_MICRONET.createImage();
			String displayString = replacementString;
			String additionalProposalInfo = "Additional Proposal Info";
			
			proposals.add(new CompletionProposal(replacementString, replacementOffset, replacementLength, cursorPosition, image, displayString, null, additionalProposalInfo));
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
