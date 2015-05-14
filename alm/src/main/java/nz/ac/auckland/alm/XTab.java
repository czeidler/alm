package nz.ac.auckland.alm;

import nz.ac.auckland.linsolve.LinearSpec;
import nz.ac.auckland.linsolve.Variable;

/**
 * Vertical grid line (x-tab).
 */
public class XTab extends Variable {
	/**
	* Constructor for class <code>X-Tab</code>.
	* X-Tab defines the vertical grid line.
	* @param ls the desired linear specification.
	*/
	public XTab(LinearSpec ls) {
		super(ls);
	}

	/**
	* Returns a string that displays the X-Tab.  
	* X-Tab defines the vertical grid line.
	* @return string identifies the X-Tab
	*/
	public String toString() {
		String value;
		if (getName() != null)
			value = getName();
		else
			value = "X" + this.getIndex();

		value += " (" + getValue() + ")";
		return value;
	}
}
