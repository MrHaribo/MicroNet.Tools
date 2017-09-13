package micronet.tools.model.variables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScriptDescription extends VariableDescription {

	private String scriptName;
	
	private List<String> memberArgs = new ArrayList<>();
	private Map<String, VariableDescription> externalArgs = new HashMap<>();
	
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
	
	public List<String> getMemberArgs() {
		return memberArgs;
	}

	public Map<String, VariableDescription> getExternalArgs() {
		return externalArgs;
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
