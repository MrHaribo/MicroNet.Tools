package micronet.tools.ui.modelview.variables;

public class ComponentDescription extends VariableDescription  {

	private String componentType;

	public ComponentDescription(String componentType) {
		super(VariableType.COMPONENT);
		this.componentType = componentType;
	}

	public String getComponentType() {
		return componentType;
	}
}
