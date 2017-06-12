package micronet.tools.model.nodes;

import micronet.tools.model.IVisitor;
import micronet.tools.model.ModelConstants;

public class EnumRootNode extends ModelNode {
	
	public EnumRootNode() {
		super(ModelConstants.ENUM_DEFINITION_ROOT_KEY);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void accept(IVisitor visitor) {
		visitor.visit(this);
	}

}
