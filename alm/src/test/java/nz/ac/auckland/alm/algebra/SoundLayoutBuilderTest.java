/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm.algebra;

import nz.ac.auckland.alm.Area;
import nz.ac.auckland.alm.LayoutSpec;
import nz.ac.auckland.alm.XTab;
import nz.ac.auckland.alm.YTab;


public class SoundLayoutBuilderTest extends BaseAlgebraTestCase {
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

    public void testLayout1() {
        LayoutSpec layoutSpec = getLayoutSpec(700, 1500);

        XTab left = layoutSpec.getLeft();
        YTab top = layoutSpec.getTop();
        XTab right = layoutSpec.getRight();
        YTab bottom = layoutSpec.getBottom();

        left.setValue(-2.6990676360583166E-10);
        right.setValue(767.9999999991705);
        bottom.setValue(1134.0);
        XTab x2 = makeXTabAt(181.99999999999997);
        XTab x0 = makeXTabAt(249.9999999997301);
        XTab x4 = makeXTabAt(585.9999999991705);

        YTab y0 = makeYTabAt(95.99142805967185);
        YTab y3 = makeYTabAt(1037.9928129795542);
        YTab y6 = makeYTabAt(95.99142805967185);

        layoutSpec.addArea(new Area(left, y3, x2, bottom));
        layoutSpec.addArea(new Area(x4, y3, right, bottom));
        layoutSpec.addArea(new Area(left, top, x0, y6));

        layoutSpec.addArea(new Area(left, y0, right, y3));
        layoutSpec.addArea(new Area(x2, y3, x4, bottom));

        assertTrue(SoundLayoutBuilder.fillWithEmptySpaces(layoutSpec));

        LayoutStructure layoutStructure = new LayoutStructure(layoutSpec, null);
        assertTrue(OverlapChecker.isNonOverlapping(layoutStructure));
        assertTrue(DensenessChecker.isDense(layoutStructure));
    }
}
