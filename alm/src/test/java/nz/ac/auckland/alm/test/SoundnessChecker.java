/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm.test;

import nz.ac.auckland.alm.*;
import nz.ac.auckland.alm.algebra.DensenessChecker;
import nz.ac.auckland.alm.algebra.LayoutStructure;
import nz.ac.auckland.alm.algebra.OverlapChecker;


public class SoundnessChecker extends BaseAlgebraTestCase {
    public void testNonOverlap() {
        LayoutSpec layoutSpec = getLayoutSpec(500, 500);

        XTab x1 = makeXTabAt(250);
        YTab y1 = makeYTabAt(100);
        YTab y2 = makeYTabAt(200);
        YTab y3 = makeYTabAt(300);
        YTab y4 = makeYTabAt(400);
        XTab left = layoutSpec.getLeft();
        YTab top = layoutSpec.getTop();
        XTab right = layoutSpec.getRight();
        YTab bottom = layoutSpec.getBottom();
        layoutSpec.addArea(new Area(left, top, x1, y1));
        layoutSpec.addArea(new EmptySpace(left, y1, x1, y4));
        layoutSpec.addArea(new Area(left, y4, x1, bottom));
        layoutSpec.addArea(new EmptySpace(x1, top, right, y2));
        layoutSpec.addArea(new EmptySpace(x1, y2, right, y3));
        layoutSpec.addArea(new EmptySpace(x1, y3, right, bottom));

        LayoutStructure layoutStructure = new LayoutStructure(layoutSpec, null);
        assertTrue(OverlapChecker.isNonOverlapping(layoutStructure));
    }

    public void testOverlap() {
        LayoutSpec layoutSpec = getLayoutSpec(500, 500);

        XTab x1 = makeXTabAt(250);
        YTab y1 = makeYTabAt(100);
        YTab y2 = makeYTabAt(200);
        YTab y3 = makeYTabAt(300);
        YTab y4 = makeYTabAt(400);
        XTab left = layoutSpec.getLeft();
        YTab top = layoutSpec.getTop();
        XTab right = layoutSpec.getRight();
        YTab bottom = layoutSpec.getBottom();
        layoutSpec.addArea(new Area(left, top, x1, y1));
        layoutSpec.addArea(new EmptySpace(left, y1, x1, y4));
        layoutSpec.addArea(new Area(left, y4, x1, bottom));
        layoutSpec.addArea(new EmptySpace(x1, top, right, y2));
        layoutSpec.addArea(new EmptySpace(left, y2, right, y3));
        layoutSpec.addArea(new EmptySpace(x1, y3, right, bottom));

        LayoutStructure layoutStructure = new LayoutStructure(layoutSpec, null);
        assertFalse(OverlapChecker.isNonOverlapping(layoutStructure));
    }

    public void testDenseness() {
        LayoutSpec layoutSpec = getLayoutSpec(300, 300);

        XTab x1 = makeXTabAt(100);
        XTab x2 = makeXTabAt(200);
        YTab y1 = makeYTabAt(100);
        YTab y2 = makeYTabAt(200);
        XTab left = layoutSpec.getLeft();
        YTab top = layoutSpec.getTop();
        XTab right = layoutSpec.getRight();
        YTab bottom = layoutSpec.getBottom();
        // 9 x 9 grid:
        // row 1:
        layoutSpec.addArea(new Area(left, top, x1, y1));
        layoutSpec.addArea(new Area(x1, top, x2, y1));
        layoutSpec.addArea(new Area(x2, top, right, y1));
        // row 2:
        layoutSpec.addArea(new Area(left, y1, x1, y2));
        layoutSpec.addArea(new Area(x1, y1, x2, y2));
        layoutSpec.addArea(new Area(x2, y1, right, y2));
        // row 3:
        layoutSpec.addArea(new Area(left, y2, x1, bottom));
        layoutSpec.addArea(new Area(x1, y2, x2, bottom));
        layoutSpec.addArea(new Area(x2, y2, right, bottom));

        LayoutStructure layoutStructure = new LayoutStructure(layoutSpec, null);
        assertTrue(OverlapChecker.isNonOverlapping(layoutStructure));
        assertTrue(DensenessChecker.isDense(layoutStructure));
    }

    public void testGap() {
        LayoutSpec layoutSpec = getLayoutSpec(300, 300);

        XTab x1 = makeXTabAt(100);
        XTab x2 = makeXTabAt(200);
        YTab y1 = makeYTabAt(100);
        YTab y2 = makeYTabAt(200);
        XTab left = layoutSpec.getLeft();
        YTab top = layoutSpec.getTop();
        XTab right = layoutSpec.getRight();
        YTab bottom = layoutSpec.getBottom();
        // 9 x 9 grid:
        // row 1:
        layoutSpec.addArea(new Area(left, top, x1, y1));
        layoutSpec.addArea(new Area(x1, top, x2, y1));
        layoutSpec.addArea(new Area(x2, top, right, y1));
        // row 2:
        layoutSpec.addArea(new Area(left, y1, x1, y2));
        layoutSpec.addArea(new Area(x2, y1, right, y2));
        // row 3:
        layoutSpec.addArea(new Area(left, y2, x1, bottom));
        layoutSpec.addArea(new Area(x1, y2, x2, bottom));
        layoutSpec.addArea(new Area(x2, y2, right, bottom));

        LayoutStructure layoutStructure = new LayoutStructure(layoutSpec, null);
        assertTrue(OverlapChecker.isNonOverlapping(layoutStructure));
        assertFalse(DensenessChecker.isDense(layoutStructure));
    }

    public void testDenseness2() {
        LayoutSpec layoutSpec = getLayoutSpec(300, 500);

        XTab x1 = makeXTabAt(100);
        XTab x2 = makeXTabAt(200);
        XTab x3 = makeXTabAt(300);
        XTab x4 = makeXTabAt(400);
        YTab y1 = makeYTabAt(100);
        YTab y2 = makeYTabAt(200);
        XTab left = layoutSpec.getLeft();
        YTab top = layoutSpec.getTop();
        XTab right = layoutSpec.getRight();
        YTab bottom = layoutSpec.getBottom();
        // ((A | B | C) / D / (E | F)) | G
        layoutSpec.addArea(new Area(left, top, x1, y1));
        layoutSpec.addArea(new Area(x1, top, x3, y1));
        layoutSpec.addArea(new Area(x3, top, x4, y1));
        layoutSpec.addArea(new Area(left, y1, x4, y2));
        layoutSpec.addArea(new Area(left, y2, x2, bottom));
        layoutSpec.addArea(new Area(x2, y2, x4, bottom));
        layoutSpec.addArea(new Area(x4, top, right, bottom));

        LayoutStructure layoutStructure = new LayoutStructure(layoutSpec, null);
        assertTrue(OverlapChecker.isNonOverlapping(layoutStructure));
        assertTrue(DensenessChecker.isDense(layoutStructure));
    }

    public void testGap2() {
        LayoutSpec layoutSpec = getLayoutSpec(300, 500);

        XTab x1 = makeXTabAt(100);
        XTab x2 = makeXTabAt(200);
        XTab x3 = makeXTabAt(300);
        XTab x4 = makeXTabAt(400);
        YTab y1 = makeYTabAt(100);
        YTab y2 = makeYTabAt(200);
        XTab left = layoutSpec.getLeft();
        YTab top = layoutSpec.getTop();
        XTab right = layoutSpec.getRight();
        YTab bottom = layoutSpec.getBottom();
        // ((A | B | C) / ... / (E | F)) | G
        layoutSpec.addArea(new Area(left, top, x1, y1));
        layoutSpec.addArea(new Area(x1, top, x3, y1));
        layoutSpec.addArea(new Area(x3, top, x4, y1));
        layoutSpec.addArea(new Area(left, y2, x2, bottom));
        layoutSpec.addArea(new Area(x2, y2, x4, bottom));
        layoutSpec.addArea(new Area(x4, top, right, bottom));

        LayoutStructure layoutStructure = new LayoutStructure(layoutSpec, null);
        assertFalse(DensenessChecker.isDense(layoutStructure));
    }
}
