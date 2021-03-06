package micronet.tools.model.variables;

public class ComponentDescription extends VariableDescription  {

	private String componentType;

	public ComponentDescription(String componentType) {
		super(VariableType.COMPONENT);
		this.componentType = componentType;
	}

	public String getComponentType() {
		return componentType;
	}

	public void setComponentType(String componentType) {
		this.componentType = componentType;
	}
	
	@Override
	public String toString() {
		return componentType;
	}
	
	@Override
	public boolean equals(Object other) {
		if (!super.equals(other))
			return false;
	    if (!(other instanceof ComponentDescription))
	        return false;
	    ComponentDescription castOther = (ComponentDescription) other;
	    return getComponentType().equals(castOther.getComponentType());
	}
}
