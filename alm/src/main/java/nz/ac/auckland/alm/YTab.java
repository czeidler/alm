package nz.ac.auckland.alm;

import nz.ac.auckland.linsolve.Variable;

/**
 * Horizontal grid line (y-tab).
 */
public class YTab extends Variable {
	/**
	* Constructor for class <code>Y-Tab</code>.
	* Y-Tab defines the horizontal grid line.
	*/
	public YTab() {
	}

	public YTab(String name) {
		setName(name);
	}

	/**
	* Returns a string that displays the Y-Tab
	* Y-Tab defines the horizontal grid line.
	* @return string identifies the Y-Tab
	*/
	public String toString() {
		String value;
		if (getName() != null)
			value = getName();
		else
			value = "Y" + this.getIndex();

		value += " (" + getValue() + ")";
		return value;
	}
}
