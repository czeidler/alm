/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm.algebra;


import nz.ac.auckland.alm.*;

import java.util.List;

public class LayoutItemPathTest extends BaseAlgebraTestCase {
    public void testLayoutItemPath() {
        LayoutSpec layoutSpec = getLayoutSpec(600, 600);

        XTab x0 = makeXTabAt(100);
        XTab x1 = makeXTabAt(200);
        XTab x2 = makeXTabAt(300);
        XTab x4 = makeXTabAt(500);

        YTab y0 = makeYTabAt(100);
        YTab y1 = makeYTabAt(200);
        YTab y2 = makeYTabAt(300);
        YTab y4 = makeYTabAt(500);

        XTab left = layoutSpec.getLeft();
        YTab top = layoutSpec.getTop();
        XTab right = layoutSpec.getRight();
        YTab bottom = layoutSpec.getBottom();
        layoutSpec.addArea(new Area(x4, y4, right, bottom));

        // build group
        layoutSpec.addArea(new Area(left, top, x0, y0));
        Area seed = new Area(left, y0, x0, y1);
        layoutSpec.addArea(seed);
        layoutSpec.addArea(new Area(x0, y0, x1, y1));
        layoutSpec.addArea(new Area(x1, y0, x2, y1));
        layoutSpec.addArea(new Area(x1, y1, x2, y2));

        assertTrue(SoundLayoutBuilder.fillWithEmptySpaces(layoutSpec));

        LayoutStructure layoutStructure = new LayoutStructure(layoutSpec, null);
        assertTrue(OverlapChecker.isNonOverlapping(layoutStructure));
        assertTrue(DensenessChecker.isDense(layoutStructure));

        List<Area> group = LayoutItemPath.detect(seed, layoutStructure.getXTabEdges(), layoutStructure.getYTabEdges());
        assertEquals(5, group.size());
    }
}
