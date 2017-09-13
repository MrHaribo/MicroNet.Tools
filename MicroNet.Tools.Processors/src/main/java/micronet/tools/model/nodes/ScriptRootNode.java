package micronet.tools.model.nodes;

import micronet.tools.model.IVisitor;
import micronet.tools.model.ModelConstants;

public class ScriptRootNode extends ModelNode {

	public ScriptRootNode() {
		super(ModelConstants.SCRIPTS_ROOT_KEY);
	}

	@Override
	public void accept(IVisitor visitor) {
	}
}
