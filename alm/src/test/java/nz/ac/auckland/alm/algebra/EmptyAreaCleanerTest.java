/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm.algebra;

import nz.ac.auckland.alm.*;


public class EmptyAreaCleanerTest extends BaseAlgebraTestCase {
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
        layoutSpec.addArea(new EmptySpace(left, top, x2, y1));
        layoutSpec.addArea(new EmptySpace(x2, top, right, y2));
        layoutSpec.addArea(new EmptySpace(x1, y2, right, bottom));
        layoutSpec.addArea(new EmptySpace(left, y1, x1, bottom));
        // middle
        layoutSpec.addArea(new EmptySpace(x1, y1, x2, y2));

        AlgebraData algebraData = new AlgebraData(layoutSpec, null);

        assertTrue(EmptyAreaCleaner.clean(algebraData));

        assertEquals(1, algebraData.getEmptySpaces().size());
    }

    public void testSimplify() {
        LayoutSpec layoutSpec = getLayoutSpec(500, 500);

        XTab left = layoutSpec.getLeft();
        YTab top = layoutSpec.getTop();
        XTab right = layoutSpec.getRight();
        YTab bottom = layoutSpec.getBottom();

        XTab x0 = makeXTabAt(100);
        XTab x1 = makeXTabAt(200);

        YTab y0 = makeYTabAt(100);
        YTab y1 = makeYTabAt(200);

        // (L1 / L2 / L3) | L4 | L5
        layoutSpec.addArea(new EmptySpace(left, top, x0, y0));
        layoutSpec.addArea(new EmptySpace(left, y0, x0, y1));
        layoutSpec.addArea(new EmptySpace(left, y1, x0, bottom));

        layoutSpec.addArea(new EmptySpace(x0, top, x1, bottom));
        layoutSpec.addArea(new EmptySpace(x1, top, right, bottom));

        AlgebraData algebraData = new AlgebraData(layoutSpec, null);
        EmptyAreaCleaner.simplify(algebraData, new BottomDirection());

        assertEquals(3, algebraData.getEmptySpaces().size());

        EmptyAreaCleaner.simplify(algebraData, new RightDirection());

        assertEquals(1, algebraData.getEmptySpaces().size());
    }
}
