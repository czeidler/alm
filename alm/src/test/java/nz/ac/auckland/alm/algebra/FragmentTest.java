/*
 * Copyright 2016.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm.algebra;

import junit.framework.TestCase;
import nz.ac.auckland.alm.Area;
import nz.ac.auckland.alm.IArea;
import nz.ac.auckland.alm.algebra.string.Parser;
import nz.ac.auckland.alm.algebra.string.StringReader;

import java.util.HashMap;
import java.util.Map;


public class FragmentTest extends TestCase {

    private Parser.IAreaFactory equivalentTestAreaFactory = new Parser.IAreaFactory() {
            final Map<String, IArea> areaMap = new HashMap<String, IArea>();

            @Override
            public IArea getArea(String areaId) {
                IArea area = areaMap.get(areaId);
                if (area == null) {
                    area = new Area();
                    areaMap.put(areaId, area);
                    area.setId(areaId);
                }
                return area;
            }
        };

    private Fragment read(String string) {
        return StringReader.readRawFragments(string, equivalentTestAreaFactory).get(0);
    }

    public void testEquivalent() {
        Fragment fragment = read("A");
        assertTrue(fragment.isEquivalent(read("A")));

        fragment = read("A|B");
        assertTrue(fragment.isEquivalent(read("A|B")));

        fragment = read("A/B");
        assertTrue(fragment.isEquivalent(read("A/B")));

        fragment = read("A/B");
        assertTrue(fragment.isEquivalent(read("(A/B)")));

        fragment = read("(((A|B)))");
        assertTrue(fragment.isEquivalent(read("((A|B))")));
        assertTrue(fragment.isEquivalent(read("A|B")));

        fragment = read("(((A/B)))");
        assertTrue(fragment.isEquivalent(read("((((A/B))))")));
        assertTrue(fragment.isEquivalent(read("((A/B))")));
        assertTrue(fragment.isEquivalent(read("A/B")));

        fragment = read("A|(A|A)|(A|A)|A");
        assertTrue(fragment.isEquivalent(read("A|A|A|A|A|A")));
        assertTrue(fragment.isEquivalent(read("A|(A|A|A|A)|A")));

        fragment = read("A|(B/C)|(D/E)|A");
        assertTrue(fragment.isEquivalent(read("A|((B/C)|(D/E))|A")));
        assertTrue(fragment.isEquivalent(read("A|((B/C))|(D/E)|A")));

        fragment = read("A|(B/C)|(D/E)|A");
        assertFalse(fragment.isEquivalent(read("A|((B/C)|(D|E))|A")));
        assertFalse(fragment.isEquivalent(read("A|(B|C)|(D/E)|A")));

        fragment = read("A|(B/(C|F))|(D/E)|A");
        assertTrue(fragment.isEquivalent(read("A|(B/(C|F))|(D/E)|A")));
        assertTrue(fragment.isEquivalent(read("A|((B/(C|F))|(D/E)|A)")));
    }
}
