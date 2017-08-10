package micronet.tools.codeassist;

import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension3;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

public class APICompletionProposal implements ICompletionProposal, ICompletionProposalExtension3{

	public String replacementString;
	public int replacementOffset;
	public int replacementLength;
	public int cursorPosition;
	public Image image;
	public String additionalProposalInfo;
	
	@Override
	public IInformationControlCreator getInformationControlCreator() {
		return new IInformationControlCreator() {
			@Override
			public IInformationControl createInformationControl(Shell parentShell) {
				return new DefaultInformationControl(parentShell);
			}
		};
	}

	@Override
	public int getPrefixCompletionStart(IDocument arg0, int arg1) {
		return 0;
	}

	@Override
	public CharSequence getPrefixCompletionText(IDocument arg0, int arg1) {
		return null;
	}

	@Override
	public void apply(IDocument arg0) {
		CompletionProposal completionProposal = new CompletionProposal(replacementString, replacementOffset, replacementLength, cursorPosition);
		completionProposal.apply(arg0);
	}

	@Override
	public String getAdditionalProposalInfo() {
		return additionalProposalInfo;
	}

	@Override
	public IContextInformation getContextInformation() {
		return null;
	}

	@Override
	public String getDisplayString() {
		return replacementString;
	}

	@Override
	public Image getImage() {
		return image;
	}

	@Override
	public Point getSelection(IDocument arg0) {
		return null;
	}
}
