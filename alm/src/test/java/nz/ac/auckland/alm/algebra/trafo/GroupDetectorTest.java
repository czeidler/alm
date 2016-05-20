/*
 * Copyright 2016.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm.algebra.trafo;

import junit.framework.TestCase;
import nz.ac.auckland.alm.IArea;
import nz.ac.auckland.alm.TabArea;
import nz.ac.auckland.alm.algebra.Fragment;
import nz.ac.auckland.alm.algebra.string.Parser;
import nz.ac.auckland.alm.algebra.string.StringReader;
import nz.ac.auckland.alm.algebra.string.StringWriter;

import java.util.Comparator;
import java.util.List;


public class GroupDetectorTest extends TestCase {
    private IArea createArea(String id) {
        IArea area = new TabArea();
        area.setId(id);
        return area;
    }

    Comparator<IArea> comparator = new AreaComparator();

    public void testGroupDetection() throws Exception {
        // A|A|A
        Fragment fragment = Fragment.createEmptyFragment(Fragment.horizontalDirection);
        fragment.add(createArea("A"), false);
        fragment.add(createArea("A"), false);
        fragment.add(createArea("A"), false);
        List<Fragment> alternatives = GroupDetector.detect(fragment, comparator);

        System.out.println("Org: " + StringWriter.write(fragment, true));
        System.out.println("Alternatives:");
        for (Fragment alternative : alternatives)
            System.out.println(StringWriter.write(alternative, true));
        assertEquals(0, alternatives.size());

        // A|B|A|B
        fragment = Fragment.createEmptyFragment(Fragment.horizontalDirection);
        fragment.add(createArea("A"), false);
        fragment.add(createArea("B"), false);
        fragment.add(createArea("A"), false);
        fragment.add(createArea("B"), false);

        alternatives = GroupDetector.detect(fragment, comparator);

        System.out.println("Org: " + StringWriter.write(fragment, true));
        System.out.println("Alternatives:");
        for (Fragment alternative : alternatives)
            System.out.println(StringWriter.write(alternative, true));
        assertEquals(1, alternatives.size());
        assertEquals(alternatives.get(0).toString(), "(A|B)|(A|B)");

        // A|B|A|B|A
        fragment = Fragment.createEmptyFragment(Fragment.horizontalDirection);
        fragment.add(createArea("A"), false);
        fragment.add(createArea("B"), false);
        fragment.add(createArea("A"), false);
        fragment.add(createArea("B"), false);
        fragment.add(createArea("A"), false);

        alternatives = GroupDetector.detect(fragment, comparator);

        System.out.println("Org: " + StringWriter.write(fragment, true));
        System.out.println("Alternatives:");
        for (Fragment alternative : alternatives)
            System.out.println(StringWriter.write(alternative, true));
        assertEquals(2, alternatives.size());

        // A|A|B
        fragment = Fragment.createEmptyFragment(Fragment.horizontalDirection);
        fragment.add(createArea("A"), false);
        fragment.add(createArea("A"), false);
        fragment.add(createArea("A"), false);
        fragment.add(createArea("B"), false);

        alternatives = GroupDetector.detect(fragment, comparator);

        System.out.println("Org: " + StringWriter.write(fragment, true));
        System.out.println("Alternatives:");
        for (Fragment alternative : alternatives)
            System.out.println(StringWriter.write(alternative, true));
        assertEquals(1, alternatives.size());

        // A|A|B|A|A|B|A|B|D|D
        fragment = Fragment.createEmptyFragment(Fragment.horizontalDirection);
        fragment.add(createArea("A"), false);
        fragment.add(createArea("A"), false);
        fragment.add(createArea("B"), false);
        fragment.add(createArea("A"), false);
        fragment.add(createArea("A"), false);
        fragment.add(createArea("B"), false);
        fragment.add(createArea("A"), false);
        fragment.add(createArea("B"), false);
        fragment.add(createArea("D"), false);
        fragment.add(createArea("D"), false);

        alternatives = GroupDetector.detect(fragment, comparator);

        System.out.println("Org: " + StringWriter.write(fragment, true));
        System.out.println("Alternatives:");
        for (Fragment alternative : alternatives)
            System.out.println(StringWriter.write(alternative, true));
        assertEquals(4, alternatives.size());
    }

    private Parser.IAreaFactory areaFactory = new Parser.IAreaFactory() {
        @Override
        public IArea getArea(String areaId) {
            return createArea(areaId);
        }
    };

    private Fragment create(String algebraString) {
        return StringReader.readRawFragments(algebraString, areaFactory).get(0);
    }

    public void testGroupMerge() throws Exception {
        Fragment fragment = create("(A|B|C)/(A|B|C)/(D|E)/F/(G|H)");
        Fragment result = GroupDetector.detectAcrossChild(fragment, comparator).get(0);
        System.out.println("Org: " + StringWriter.write(fragment, true));
        result.applySpecsToChild();
        System.out.println("Merged: " + StringWriter.write(result, true));
        assertEquals(result.toString(), "A/B/C/A/B/C/(D|E)/F/(G|H)");


        fragment = create("C/(A|B)/(A|B)/(D|E)");
        result = GroupDetector.detectAcrossChild(fragment, comparator).get(0);
        System.out.println("Org: " + StringWriter.write(fragment, true));
        result.applySpecsToChild();
        System.out.println("Merged: " + StringWriter.write(result, true));
        assertEquals(result.toString(), "C/A/B/A/B/(D|E)");

        fragment = create("A/(B|C)/D");
        assertTrue(GroupDetector.detectAcrossChild(fragment, comparator).size() == 0);
    }
}
