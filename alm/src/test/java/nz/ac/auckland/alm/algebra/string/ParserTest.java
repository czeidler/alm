/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm.algebra.string;

import junit.framework.TestCase;
import nz.ac.auckland.alm.IArea;

import java.util.ArrayList;
import java.util.List;


public class ParserTest extends TestCase {
    private void assertStringNotParsed(String string) {
        System.out.println("Parse bad string: " + string);
        List<IArea> items = StringReader.read(string);
        assertTrue(items == null);
    }

    private void assertStringParsed(String string) {
        System.out.println("Parse: " + string);
        List<IArea> items = StringReader.read(string);
        assertTrue(items != null);
        System.out.println("Result: " + StringWriter.write(items));
    }

    public void testString() {
        assertStringParsed("AD2 |{2} B * (A | {  2 } B) ");
        assertStringParsed("(A|B|C)/ (D|E|((r/b)) ) / A  *m");
        assertStringParsed("((m))");
        assertStringParsed("A");

        assertStringNotParsed("(");
        assertStringNotParsed("}");
        assertStringNotParsed("(A");
        assertStringNotParsed("(A |");
        assertStringNotParsed("(A | }");
        assertStringNotParsed("(A: | }");

        List<IArea> items = StringReader.read("");
        assertTrue(items != null);
        assertEquals(0, items.size());
    }

    private void setBorderTabs(List<IArea> fragments) {
        List<IArea> atoms = toAtoms(fragments);
        for (IArea atom : atoms) {


        }
    }
    private List<IArea> toAtoms(List<IArea> fragments) {
        List<IArea> atoms = new ArrayList<IArea>();
        for (IArea fragment : fragments)
            toAtoms(fragment, atoms);
        return atoms;
    }

    private void toAtoms(IArea fragment, List<IArea> atoms) {
        if (!(fragment instanceof Fragment)) {
            atoms.add(fragment);
            return;
        }
        for (IArea child : (List<IArea>)((Fragment) fragment).getItems())
            toAtoms(child, atoms);
    }
}
