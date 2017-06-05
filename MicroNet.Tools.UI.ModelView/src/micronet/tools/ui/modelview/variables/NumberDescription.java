package micronet.tools.ui.modelview.variables;

public class NumberDescription extends VariableDescription  {

	private NumberType numberType;

	public NumberDescription(NumberType numberType) {
		super(VariableType.NUMBER);
		this.numberType = numberType;
	}

	public NumberType getNumberType() {
		return numberType;
	}
}
