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
		String value = getName();
		if (getName() == null) {
			value = "Y";
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
