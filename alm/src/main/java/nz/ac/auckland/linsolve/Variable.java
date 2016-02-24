package nz.ac.auckland.linsolve;


import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a variable in a linear constraint. 
 */

public class Variable {
	Object solverCookie;
	public LinearSpec ls; // linear spec that this variable belongs to
	double value = Double.NaN; // value of the variable
	String name; // name of the variable
	final List<Constraint> activeConstraints = new ArrayList<Constraint>();

	/**
	 * The pivot constraint of a variable is the constraint where this variable
	 * is most dominant, as compared with all editor constraints.
	 */

	public Variable() {

	}

	public Variable(String name) {
		setName(name);
	}

	public Object getSolverCookie() {
		return solverCookie;
	}

	public void setSolverCookie(Object solverCookie) {
		this.solverCookie = solverCookie;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	protected void onConstraintActivated(Constraint constraint) {
		if (activeConstraints.contains(constraint)) {
			throw new RuntimeException("onConstraintActivated: Variable '" + this.toString()
					+ "' can be added twice to: " + constraint.toString());
		}
		activeConstraints.add(constraint);
		if (ls == null) {
			ls = constraint.linearSpec;
			ls.addVariableIfNotInSpec(this);
		} else if (ls != constraint.linearSpec)
			throw new RuntimeException("Variables can't be shared between different linear specs");
	}

	public void onConstraintDeactivated(Constraint constraint) {
		if (!activeConstraints.remove(constraint))
			throw new RuntimeException("onConstraintDeactivated: Variable '" + this.toString()
					+ "' was not part of the constraint:" + constraint.toString());
		if (activeConstraints.size() == 0) {
			ls.removeVariable(this);
			ls = null;
		}
	}

	public List<Constraint> getActiveConstraints() {
		return activeConstraints;
	}

	/**
	 * Gets 1-based index of the variable.
	 * 
	 * @return the index of the variable
	 */
	public int getIndex() {
		if (ls != null)
			return ls.getVariables().indexOf(this);
		return -1;
	}

	/**
	 * Returns index of the variable as <code>String</code>. E.g. "Var2"
	 * 
	 * @return the <code>String</code> index of the variable
	 */
	public String toString() {
		if (name != null)
			return name;

		return "Var" + getIndex() + "(" + value + " )";
	}

	public void setValue(double value) {
		this.value = value;
	}

	public double getValue() {
		return value;
	}

}
