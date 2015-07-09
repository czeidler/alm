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


public class FillGapTest extends BaseAlgebraTestCase {
    public void testLayout1() {
        LayoutSpec layoutSpec = getLayoutSpec(600, 600);

        XTab x0 = makeXTabAt(100);
        XTab x1 = makeXTabAt(200);
        XTab x2 = makeXTabAt(300);
        XTab x3 = makeXTabAt(500);

        YTab y0 = makeYTabAt(100);
        YTab y1 = makeYTabAt(150);
        YTab y2 = makeYTabAt(200);

        YTab top = layoutSpec.getTop();
        XTab right = layoutSpec.getRight();
        YTab bottom = layoutSpec.getBottom();

        layoutSpec.addArea(new Area(x0, top, x2, y0));
        layoutSpec.addArea(new Area(x0, y0, x1, y2));
        layoutSpec.addArea(new Area(x0, y2, x1, bottom));
        layoutSpec.addArea(new Area(x3, y1, right, bottom));

        AlgebraData algebraData = SoundLayoutBuilder.fillWithEmptySpaces(layoutSpec);
        assertTrue(OverlapChecker.isNonOverlapping(algebraData));
        assertTrue(DensenessChecker.isDense(algebraData));

        FillGap.fill(algebraData, x1, new LeftDirection(), new RightDirection());

        assertTrue(OverlapChecker.isNonOverlapping(algebraData));
        assertTrue(DensenessChecker.isDense(algebraData));

        assertTrue(algebraData.getXTabEdges().get(x1) == null);
    }
}
