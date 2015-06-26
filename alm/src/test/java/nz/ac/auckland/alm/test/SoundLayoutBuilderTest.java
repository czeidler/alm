/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm.test;

import junit.framework.TestCase;
import nz.ac.auckland.alm.Area;
import nz.ac.auckland.alm.LayoutSpec;
import nz.ac.auckland.alm.XTab;
import nz.ac.auckland.alm.YTab;
import nz.ac.auckland.alm.algebra.DensenessChecker;
import nz.ac.auckland.alm.algebra.LayoutStructure;
import nz.ac.auckland.alm.algebra.OverlapChecker;
import nz.ac.auckland.alm.algebra.SoundLayoutBuilder;


public class SoundLayoutBuilderTest extends TestCase {
    private LayoutSpec getLayoutSpec(int width, int height) {
        LayoutSpec layoutSpec = new LayoutSpec();
        layoutSpec.getLeft().setValue(0);
        layoutSpec.getTop().setValue(0);
        layoutSpec.getRight().setValue(width);
        layoutSpec.getBottom().setValue(height);
        return layoutSpec;
    }

    private XTab makeXTabAt(double value) {
        XTab tab = new XTab();
        tab.setValue(value);
        return tab;
    }

    private YTab makeYTabAt(double value) {
        YTab tab = new YTab();
        tab.setValue(value);
        return tab;
    }

    public void testThreeButton() {
        LayoutSpec layoutSpec = getLayoutSpec(500, 500);

        XTab left = layoutSpec.getLeft();
        YTab top = layoutSpec.getTop();
        XTab right = layoutSpec.getRight();
        YTab bottom = layoutSpec.getBottom();

        XTab x1 = makeXTabAt(100);
        XTab x2 = makeXTabAt(200);

        layoutSpec.addArea(new Area(left, top, x1, bottom));
        layoutSpec.addArea(new Area(x1, top, x2, bottom));
        layoutSpec.addArea(new Area(x2, top, right, bottom));

        assertTrue(SoundLayoutBuilder.fillWithEmptySpaces(layoutSpec));

        LayoutStructure layoutStructure = new LayoutStructure(layoutSpec, null);
        assertTrue(OverlapChecker.isNonOverlapping(layoutStructure));
        assertTrue(DensenessChecker.isDense(layoutStructure));

        assertEquals(0, layoutStructure.getEmptySpaces().size());
    }

    public void testTwoButtonsAndGap() {
        LayoutSpec layoutSpec = getLayoutSpec(500, 500);

        XTab left = layoutSpec.getLeft();
        YTab top = layoutSpec.getTop();
        XTab right = layoutSpec.getRight();
        YTab bottom = layoutSpec.getBottom();

        XTab x1 = makeXTabAt(100);
        XTab x2 = makeXTabAt(200);

        layoutSpec.addArea(new Area(left, top, x1, bottom));
        layoutSpec.addArea(new Area(x2, top, right, bottom));

        assertTrue(SoundLayoutBuilder.fillWithEmptySpaces(layoutSpec));

        LayoutStructure layoutStructure = new LayoutStructure(layoutSpec, null);
        assertTrue(OverlapChecker.isNonOverlapping(layoutStructure));
        assertTrue(DensenessChecker.isDense(layoutStructure));

        assertEquals(1, layoutStructure.getEmptySpaces().size());
    }

    public void testPinWheel() {
        LayoutSpec layoutSpec = getLayoutSpec(500, 500);

        XTab left = layoutSpec.getLeft();
        YTab top = layoutSpec.getTop();
        XTab right = layoutSpec.getRight();
        YTab bottom = layoutSpec.getBottom();

        XTab x1 = makeXTabAt(100);
        XTab x2 = makeXTabAt(200);

        YTab y1 = makeYTabAt(100);
        YTab y2 = makeYTabAt(200);

        // outer areas
        layoutSpec.addArea(new Area(left, top, x2, y1));
        layoutSpec.addArea(new Area(x2, top, right, y2));
        layoutSpec.addArea(new Area(x1, y2, right, bottom));
        layoutSpec.addArea(new Area(left, y1, x1, bottom));
        // middle
        layoutSpec.addArea(new Area(x1, y1, x2, y2));


        assertTrue(SoundLayoutBuilder.fillWithEmptySpaces(layoutSpec));

        LayoutStructure layoutStructure = new LayoutStructure(layoutSpec, null);
        assertTrue(OverlapChecker.isNonOverlapping(layoutStructure));
        assertTrue(DensenessChecker.isDense(layoutStructure));

        assertEquals(0, layoutStructure.getEmptySpaces().size());
    }

    public void testPinWheelMiddleGap() {
        LayoutSpec layoutSpec = getLayoutSpec(500, 500);

        XTab left = layoutSpec.getLeft();
        YTab top = layoutSpec.getTop();
        XTab right = layoutSpec.getRight();
        YTab bottom = layoutSpec.getBottom();

        XTab x1 = makeXTabAt(100);
        XTab x2 = makeXTabAt(200);

        YTab y1 = makeYTabAt(100);
        YTab y2 = makeYTabAt(200);

        // outer areas
        layoutSpec.addArea(new Area(left, top, x2, y1));
        layoutSpec.addArea(new Area(x2, top, right, y2));
        layoutSpec.addArea(new Area(x1, y2, right, bottom));
        layoutSpec.addArea(new Area(left, y1, x1, bottom));


        assertTrue(SoundLayoutBuilder.fillWithEmptySpaces(layoutSpec));

        LayoutStructure layoutStructure = new LayoutStructure(layoutSpec, null);
        assertTrue(OverlapChecker.isNonOverlapping(layoutStructure));
        assertTrue(DensenessChecker.isDense(layoutStructure));

        assertEquals(1, layoutStructure.getEmptySpaces().size());
    }
}
