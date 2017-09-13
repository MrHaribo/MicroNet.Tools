package micronet.tools.model.variables;

public class ScriptDescription extends VariableDescription {

	private String scriptName;
	
	public ScriptDescription(String scriptName) {
		super(VariableType.SCRIPT);
		this.scriptName = scriptName;
	}

	public String getScriptName() {
		return scriptName;
	}

	public void setScriptName(String scriptName) {
		this.scriptName = scriptName;
	}

	@Override
	public String toString() {
		return scriptName;
	}
	
	@Override
	public boolean equals(Object other) {
		if (!super.equals(other))
			return false;
	    if (!(other instanceof ScriptDescription))
	        return false;
	    ScriptDescription castOther = (ScriptDescription) other;
	    return getScriptName().equals(castOther.getScriptName());
	}
}
