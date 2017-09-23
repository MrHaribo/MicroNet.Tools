package micronet.tools.model.variables;

public class GeometryDescription extends VariableDescription {

	private GeometryType geometryType;
	
	public GeometryDescription(GeometryType geometryType) {
		super(VariableType.GEOMETRY);
		this.geometryType = geometryType;
	}

	public GeometryType getGeometryType() {
		return geometryType;
	}

	public void setGeometryType(GeometryType geometryType) {
		this.geometryType = geometryType;
	}

	@Override
	public String toString() {
		return geometryType.toString();
	}
	
	@Override
	public boolean equals(Object other) {
		if (!super.equals(other))
			return false;
	    if (!(other instanceof GeometryDescription))
	        return false;
	    GeometryDescription castOther = (GeometryDescription) other;
		return getGeometryType().equals(castOther.getGeometryType());
	}
}
