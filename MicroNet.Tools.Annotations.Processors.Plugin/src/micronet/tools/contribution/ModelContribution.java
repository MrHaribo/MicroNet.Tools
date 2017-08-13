package micronet.tools.contribution;

import java.util.List;
import java.util.Set;

import javax.xml.ws.Holder;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import micronet.tools.contribution.ContributionChoiceDialog.ContributionChoice;
import micronet.tools.filesync.SyncParameterCodes;
import micronet.tools.filesync.SyncTemplateTree;
import micronet.tools.model.INode;
import micronet.tools.model.nodes.EntityTemplateNode;
import micronet.tools.model.nodes.EntityVariableNode;
import micronet.tools.model.nodes.ModelNode;

public class ModelContribution {

	public static void contributeSharedDir(String contributedSharedDir, String sharedDir) {
		contributeTemplates(contributedSharedDir, sharedDir);
		contributeParameterCodes(contributedSharedDir, sharedDir);
	}
	
	private static void contributeParameterCodes(String contributedSharedDir, String sharedDir) {
		Set<String> contributedParameterCodes = SyncParameterCodes.getUserParameterCodes(contributedSharedDir);
		SyncParameterCodes.contributeParameters(contributedParameterCodes, sharedDir);
	}
	
	private static void contributeTemplates(String contributedSharedDir, String sharedDir) {
		
		List<EntityTemplateNode> existingTemplateTypes = SyncTemplateTree.loadAllTemplateTypes(sharedDir);
		List<EntityTemplateNode> contributedTemplateTypes = SyncTemplateTree.loadAllTemplateTypes(contributedSharedDir);
		
		for (EntityTemplateNode contributedTemplate : contributedTemplateTypes) {
			
			boolean templateIsNew = true;
			for (EntityTemplateNode existingTemplate : existingTemplateTypes) {
				
				if (contributedTemplate.getName().equals(existingTemplate.getName())) {
					templateIsNew = false;
					EntityTemplateNode combinedTemplate = combineTemplates(contributedTemplate, existingTemplate);
					if (combinedTemplate != null)
						SyncTemplateTree.saveTemplateTree(combinedTemplate, sharedDir);
					break;
				}
			}
			
			if (templateIsNew)
				SyncTemplateTree.saveTemplateTree(contributedTemplate, sharedDir);
		}
		
		System.out.println(existingTemplateTypes + "");
	}

	private static EntityTemplateNode combineTemplates(EntityTemplateNode contributedTemplate, EntityTemplateNode existingTemplate) {

		for (INode contributedChild : contributedTemplate.getChildren()) {
			if (contributedChild instanceof EntityVariableNode) {
				EntityVariableNode contributedVariable = (EntityVariableNode) contributedChild;
				
				boolean variableIsNew = true;
				
				for (INode existingChild : existingTemplate.getChildren()) {
					if (existingChild instanceof EntityVariableNode) {
						EntityVariableNode existingVariable = (EntityVariableNode) existingChild;
						
						if (contributedVariable.getName().equals(existingVariable.getName())) {
							variableIsNew = false;
							
							if (!contributedVariable.getVariabelDescription().equals(existingVariable.getVariabelDescription())) {
								String title = contributedTemplate.getName() + " Combine Problem";
								String message = "The variable <" + contributedVariable.getName() + "> of the template <"
										+ contributedTemplate.getName() + "> is ambigious. Select Type!";
								String replaceChoice = contributedVariable.getVariabelDescription().toString();
								String keepChoice = existingVariable.getVariabelDescription().toString();
								
								ContributionChoice choice = showStringChoice(title, message, replaceChoice, keepChoice);
								if (choice == ContributionChoice.REPLACE) {
									existingVariable.setVariabelDescription(contributedVariable.getVariabelDescription());
									System.out.println("Variable Type Changed: " + contributedVariable.getVariabelDescription());
								}
							}
							break;
						}
					}
				}
				
				if (variableIsNew) {
					EntityVariableNode newVariable = new EntityVariableNode(contributedVariable.getName());
					newVariable.setVariabelDescription(contributedVariable.getVariabelDescription());
					existingTemplate.addChild(newVariable);
					System.out.println("Variable Added: " + newVariable.getName());
				}
			}
		}
		
		boolean parentsMatch = false;
		if (contributedTemplate.getParent() != null) {
			if (existingTemplate.getParent() != null) {
				parentsMatch = contributedTemplate.getParent().getName().equals(existingTemplate.getParent().getName());
			}
		} else if (existingTemplate.getParent() == null){
			parentsMatch = true;
		}
		
		if (!parentsMatch) {
			String title = contributedTemplate.getName() + " Combine Problem";
			String message = "The parent of the template " + contributedTemplate.getName() + " ambigious. Select Parent!";
			String replaceChoice = contributedTemplate.getParent() == null ? "null" : contributedTemplate.getParent().getName();
			String keepChoice = existingTemplate.getParent() == null ? "null" : existingTemplate.getParent().getName();
			
			ContributionChoice choice = showStringChoice(title, message, replaceChoice, keepChoice);
			if (choice == ContributionChoice.REPLACE) {
				if (existingTemplate.getParent() != null) {
					if (contributedTemplate.getParent() != null) {
						ModelNode existingParent = (ModelNode) existingTemplate.getParent();
						ModelNode contributedParent = (ModelNode) contributedTemplate.getParent();
						
						existingParent.removeChild(existingTemplate);
						contributedParent.removeChild(contributedTemplate);
						contributedParent.addChild(existingTemplate);
					} else {
						existingTemplate.setParent(null);
					}
				} else {
					if (contributedTemplate.getParent() != null) {
						ModelNode contributedParent = (ModelNode) contributedTemplate.getParent();
						contributedParent.removeChild(contributedTemplate);
						contributedParent.addChild(existingTemplate);
					}
				}
				System.out.println("Parent Changed: " + keepChoice);
			}
		}
		
		
		return existingTemplate;
	}
	
	private static ContributionChoice showStringChoice(String title, String message, Object replaceChoice, Object keepChoice) {
		
		Holder<ContributionChoice> result = new Holder<>();
		
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
		    public void run() {
		        Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		        
		        ContributionChoiceDialog choiceDialog = new ContributionChoiceDialog(activeShell, title, message, replaceChoice, keepChoice);
		        choiceDialog.open();
		        
		        result.value = choiceDialog.getChoice();
		    }
		});
		
		return result.value;
	}
}
