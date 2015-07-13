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

import java.util.List;


public class ParserTest extends TestCase {
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
    }
}
