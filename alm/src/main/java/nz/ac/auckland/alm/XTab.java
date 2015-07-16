package nz.ac.auckland.alm;

import nz.ac.auckland.linsolve.Variable;

/**
 * Vertical grid line (x-tab).
 */
public class XTab extends Variable {
	/**
	* Constructor for class <code>X-Tab</code>.
	* X-Tab defines the vertical grid line.
	*/
	public XTab() {
	}

	public XTab(String name) {
		setName(name);
	}

	/**
	* Returns a string that displays the X-Tab.  
	* X-Tab defines the vertical grid line.
	* @return string identifies the X-Tab
	*/
	public String toString() {
		String value = getName();
		if (getName() == null) {
			value = "X";
			int index = this.getIndex();
			if (index > 0)
				value += index - 2; // there are two border tab stops
			else
				value += "0x" + Integer.toHexString(hashCode());
		}

		value += " (" + getValue() + ")";
		return value;
	}
}
