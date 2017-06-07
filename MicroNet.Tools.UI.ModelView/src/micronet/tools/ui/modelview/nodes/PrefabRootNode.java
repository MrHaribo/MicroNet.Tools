package micronet.tools.ui.modelview.nodes;

import micronet.tools.ui.modelview.IVisitor;
import micronet.tools.ui.modelview.ModelConstants;

public class PrefabRootNode extends ModelNode {

	public PrefabRootNode() {
		super(ModelConstants.PREFAB_ROOT_KEY);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void accept(IVisitor visitor) {
		//visitor.visit(this);
	}
}
