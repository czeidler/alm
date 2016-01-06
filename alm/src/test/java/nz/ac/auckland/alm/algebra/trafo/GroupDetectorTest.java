/*
 * Copyright 2015.
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
import nz.ac.auckland.alm.algebra.string.StringWriter;

import java.util.Comparator;
import java.util.List;


public class GroupDetectorTest extends TestCase {
    private Comparator<IArea> comparator = new Comparator<IArea>() {
        private String fragmentId(Fragment fragment) {
            String id = "";
            if (fragment.isHorizontalDirection())
                id += "h";
            else
                id += "v";
            List<IArea> items = fragment.getItems();
            for (IArea item : items) {
                if (item instanceof Fragment)
                    id += fragmentId((Fragment)item);
                else
                    id += item.getId();
            }
            return id;
        }

        @Override
        public int compare(IArea area0, IArea area1) {
            String area0Id;
            String area1Id;
            if (area0 instanceof Fragment)
                area0Id = fragmentId((Fragment)area0);
            else
                area0Id = area0.getId();

            if (area1 instanceof Fragment)
                area1Id = fragmentId((Fragment)area1);
            else
                area1Id = area1.getId();

            if (area0Id.equals(area1Id))
                return 0;
            return -1;
        }
    };

    private IArea createArea(String id) {
        IArea area = new TabArea();
        area.setId(id);
        return area;
    }

    public void testGroupDetection() throws Exception {
        // A|B|A|B
        Fragment fragment = Fragment.createEmptyFragment(Fragment.horizontalDirection);
        fragment.add(createArea("A"));
        fragment.add(createArea("B"));
        fragment.add(createArea("A"));
        fragment.add(createArea("B"));

        List<Fragment> alternatives = GroupDetector.detect(fragment, comparator);

        System.out.println("Org: " + StringWriter.write(fragment, true));
        System.out.println("Alternatives:");
        for (Fragment alternative : alternatives)
            System.out.println(StringWriter.write(alternative, true));

        // A|B|A|B|A
        fragment = Fragment.createEmptyFragment(Fragment.horizontalDirection);
        fragment.add(createArea("A"));
        fragment.add(createArea("B"));
        fragment.add(createArea("A"));
        fragment.add(createArea("B"));
        fragment.add(createArea("A"));

        alternatives = GroupDetector.detect(fragment, comparator);

        System.out.println("Org: " + StringWriter.write(fragment, true));
        System.out.println("Alternatives:");
        for (Fragment alternative : alternatives)
            System.out.println(StringWriter.write(alternative, true));

        // A|A|B
        fragment = Fragment.createEmptyFragment(Fragment.horizontalDirection);
        fragment.add(createArea("A"));
        fragment.add(createArea("A"));
        fragment.add(createArea("A"));
        fragment.add(createArea("B"));

        alternatives = GroupDetector.detect(fragment, comparator);

        System.out.println("Org: " + StringWriter.write(fragment, true));
        System.out.println("Alternatives:");
        for (Fragment alternative : alternatives)
            System.out.println(StringWriter.write(alternative, true));

        // A|A|B|A|A|B|A|B|D|D
        fragment = Fragment.createEmptyFragment(Fragment.horizontalDirection);
        fragment.add(createArea("A"));
        fragment.add(createArea("A"));
        fragment.add(createArea("B"));
        fragment.add(createArea("A"));
        fragment.add(createArea("A"));
        fragment.add(createArea("B"));
        fragment.add(createArea("A"));
        fragment.add(createArea("B"));
        fragment.add(createArea("D"));
        fragment.add(createArea("D"));

        alternatives = GroupDetector.detect(fragment, comparator);

        System.out.println("Org: " + StringWriter.write(fragment, true));
        System.out.println("Alternatives:");
        for (Fragment alternative : alternatives)
            System.out.println(StringWriter.write(alternative, true));
    }
}
