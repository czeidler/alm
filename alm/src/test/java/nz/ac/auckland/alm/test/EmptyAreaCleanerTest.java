/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm.test;

import nz.ac.auckland.alm.*;
import nz.ac.auckland.alm.algebra.*;


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

        LayoutStructure layoutStructure = new LayoutStructure(layoutSpec, null);
        EmptyAreaCleaner cleaner = new EmptyAreaCleaner(layoutStructure);

        assertTrue(cleaner.clean());

        assertEquals(1, layoutStructure.getEmptySpaces().size());
    }
}
