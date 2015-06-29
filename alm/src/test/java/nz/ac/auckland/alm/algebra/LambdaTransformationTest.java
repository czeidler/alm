/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm.algebra;

import nz.ac.auckland.alm.*;
import nz.ac.auckland.alm.algebra.BaseAlgebraTestCase;
import nz.ac.auckland.alm.algebra.LambdaTransformation;
import nz.ac.auckland.alm.algebra.LayoutStructure;


public class LambdaTransformationTest extends BaseAlgebraTestCase {
    private EmptySpace assertMakeSpace(LambdaTransformation trafo, XTab left, YTab top, XTab right, YTab bottom) {
        EmptySpace emptySpace = trafo.makeSpace(left, top, right, bottom);

        assertTrue(emptySpace != null);

        assertEquals(left, emptySpace.getLeft());
        assertEquals(top, emptySpace.getTop());
        assertEquals(right, emptySpace.getRight());
        assertEquals(bottom, emptySpace.getBottom());
        return emptySpace;
    }

    public void testSimple() {
        LayoutSpec layoutSpec = getLayoutSpec(500, 500);

        layoutSpec.addArea(new EmptySpace(layoutSpec.getLeft(), layoutSpec.getTop(), layoutSpec.getRight(),
                layoutSpec.getBottom()));
        LayoutStructure layoutStructure = new LayoutStructure(layoutSpec, null);
        LambdaTransformation trafo = new LambdaTransformation(layoutStructure);

        assertMakeSpace(trafo, layoutSpec.getLeft(), layoutSpec.getTop(), layoutSpec.getRight(),
                layoutSpec.getBottom());

        assertEquals(1, layoutStructure.getEmptySpaces().size());
    }

    public void testSimple2() {
        LayoutSpec layoutSpec = getLayoutSpec(500, 500);
        XTab tab1 = makeXTabAt(100);
        XTab tab2 = makeXTabAt(200);
        layoutSpec.addArea(new EmptySpace(layoutSpec.getLeft(), layoutSpec.getTop(), tab1,
                layoutSpec.getBottom()));
        layoutSpec.addArea(new EmptySpace(tab1, layoutSpec.getTop(), tab2,
                layoutSpec.getBottom()));
        layoutSpec.addArea(new Area(tab2, layoutSpec.getTop(), layoutSpec.getRight(),
                layoutSpec.getBottom()));
        LayoutStructure layoutStructure = new LayoutStructure(layoutSpec, null);
        LambdaTransformation trafo = new LambdaTransformation(layoutStructure);

        assertMakeSpace(trafo, layoutSpec.getLeft(), layoutSpec.getTop(), tab2, layoutSpec.getBottom());

        assertEquals(1, layoutStructure.getEmptySpaces().size());
    }

    public void testIntersectingNeighbours() {
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
        // intersecting neighbours:
        layoutSpec.addArea(new EmptySpace(x1, top, right, y2));
        layoutSpec.addArea(new EmptySpace(x1, y2, right, y3));
        layoutSpec.addArea(new EmptySpace(x1, y3, right, bottom));

        LayoutStructure layoutStructure = new LayoutStructure(layoutSpec, null);
        LambdaTransformation trafo = new LambdaTransformation(layoutStructure);

        assertMakeSpace(trafo, left, y1, right, y4);

        assertEquals(3, layoutStructure.getEmptySpaces().size());
    }

    public void testExtendRightAndBottom() {
        LayoutSpec layoutSpec = getLayoutSpec(500, 500);
        XTab x1 = makeXTabAt(100);
        XTab x2 = makeXTabAt(200);
        XTab x3 = makeXTabAt(300);
        // test same position as x3:
        XTab x4 = makeXTabAt(300);
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
        layoutSpec.addArea(new EmptySpace(left, y4, x1, bottom));
        // neighbours:
        layoutSpec.addArea(new EmptySpace(x1, top, x2, y2));
        layoutSpec.addArea(new EmptySpace(x2, top, right, y2));
        layoutSpec.addArea(new EmptySpace(x1, y2, x3, y3));
        layoutSpec.addArea(new EmptySpace(x3, y2, right, y3));
        layoutSpec.addArea(new EmptySpace(x1, y3, x4, bottom));
        layoutSpec.addArea(new EmptySpace(x4, y3, right, bottom));

        LayoutStructure layoutStructure = new LayoutStructure(layoutSpec, null);
        LambdaTransformation trafo = new LambdaTransformation(layoutStructure);

        assertMakeSpace(trafo, left, y1, right, bottom);

        assertEquals(3, layoutStructure.getEmptySpaces().size());
    }

    public void testIntersectingNeighboursConflict() {
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
        // intersecting neighbours:
        layoutSpec.addArea(new EmptySpace(x1, top, right, y2));
        layoutSpec.addArea(new Area(x1, y2, right, y3));
        layoutSpec.addArea(new EmptySpace(x1, y3, right, bottom));

        LayoutStructure layoutStructure = new LayoutStructure(layoutSpec, null);
        LambdaTransformation trafo = new LambdaTransformation(layoutStructure);

        EmptySpace emptySpace = trafo.makeSpace(left, y1, right, y4);
        assertTrue(emptySpace == null);
    }
}
