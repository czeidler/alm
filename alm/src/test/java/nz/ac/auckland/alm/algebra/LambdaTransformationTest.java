/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm.algebra;

import nz.ac.auckland.alm.*;


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
        AlgebraData algebraData = new AlgebraData(layoutSpec, null);
        LambdaTransformation trafo = new LambdaTransformation(algebraData);

        assertMakeSpace(trafo, layoutSpec.getLeft(), layoutSpec.getTop(), layoutSpec.getRight(),
                layoutSpec.getBottom());

        assertEquals(1, algebraData.getEmptySpaces().size());
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
        AlgebraData algebraData = new AlgebraData(layoutSpec, null);
        LambdaTransformation trafo = new LambdaTransformation(algebraData);

        assertMakeSpace(trafo, layoutSpec.getLeft(), layoutSpec.getTop(), tab2, layoutSpec.getBottom());

        assertEquals(1, algebraData.getEmptySpaces().size());
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

        AlgebraData algebraData = new AlgebraData(layoutSpec, null);
        LambdaTransformation trafo = new LambdaTransformation(algebraData);

        assertMakeSpace(trafo, left, y1, right, y4);

        assertEquals(3, algebraData.getEmptySpaces().size());
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

        AlgebraData algebraData = new AlgebraData(layoutSpec, null);
        LambdaTransformation trafo = new LambdaTransformation(algebraData);

        assertMakeSpace(trafo, left, y1, right, bottom);

        assertEquals(3, algebraData.getEmptySpaces().size());
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

        AlgebraData algebraData = new AlgebraData(layoutSpec, null);
        LambdaTransformation trafo = new LambdaTransformation(algebraData);

        EmptySpace emptySpace = trafo.makeSpace(left, y1, right, y4);
        assertTrue(emptySpace == null);
    }

    public void testNewLeftRightTabs() {
        LayoutSpec layoutSpec = getLayoutSpec(500, 500);
        XTab x0 = makeXTabAt(100);
        YTab y0 = makeYTabAt(100);
        XTab left = layoutSpec.getLeft();
        YTab top = layoutSpec.getTop();
        XTab right = layoutSpec.getRight();
        YTab bottom = layoutSpec.getBottom();
        layoutSpec.addArea(new EmptySpace(left, top, right, bottom));

        AlgebraData algebraData = new AlgebraData(layoutSpec, null);
        LambdaTransformation trafo = new LambdaTransformation(algebraData);

        EmptySpace emptySpace = trafo.makeSpace(x0, y0, right, bottom);
        assertTrue(emptySpace != null);
    }

    public void testNewLeftTab() {
        LayoutSpec layoutSpec = getLayoutSpec(500, 500);
        XTab x0 = makeXTabAt(200);
        XTab x1 = makeXTabAt(100);
        YTab y0 = makeYTabAt(100);
        YTab y1 = makeYTabAt(200);
        XTab left = layoutSpec.getLeft();
        YTab top = layoutSpec.getTop();
        XTab right = layoutSpec.getRight();
        YTab bottom = layoutSpec.getBottom();
        layoutSpec.addArea(new EmptySpace(left, top, x0, y0));
        layoutSpec.addArea(new Area(x0, top, right, y0));
        layoutSpec.addArea(new EmptySpace(left, y0, right, bottom));

        AlgebraData algebraData = new AlgebraData(layoutSpec, null);
        LambdaTransformation trafo = new LambdaTransformation(algebraData);

        EmptySpace emptySpace = trafo.makeSpace(x1, y0, right, y1);
        assertTrue(emptySpace != null);
    }

    public void testError1() {
        LayoutSpec layoutSpec = getLayoutSpec(500, 500);
        XTab x0 = makeXTabAt(100);
        XTab x1 = makeXTabAt(200);
        YTab y0 = makeYTabAt(100);
        YTab y1 = makeYTabAt(400);
        YTab y2 = makeYTabAt(399.9912);

        XTab left = layoutSpec.getLeft();
        YTab top = layoutSpec.getTop();
        XTab right = layoutSpec.getRight();
        YTab bottom = layoutSpec.getBottom();
        layoutSpec.addArea(new Area(left, y1, x0, bottom));
        layoutSpec.addArea(new Area(x0, y2, x1, bottom));
        layoutSpec.addArea(new EmptySpace(left, top, x0, y1));
        layoutSpec.addArea(new EmptySpace(x0, top, x1, y2));
        layoutSpec.addArea(new EmptySpace(x1, top, right, bottom));

        AlgebraData algebraData = new AlgebraData(layoutSpec, null);
        LambdaTransformation trafo = new LambdaTransformation(algebraData);

        EmptySpace emptySpace = trafo.makeSpace(left, top, right, y0);
        assertTrue(emptySpace != null);
    }


    public void testError2() {
        LayoutSpec layoutSpec = getLayoutSpec(1196, 718);
        XTab x0 = makeXTabAt(408.9935889660454);
        XTab x1 = makeXTabAt(968.9999999923093);
        XTab x2 = makeXTabAt(181.99358896604542);
        YTab y0 = makeYTabAt(621.9946225682406);
        YTab y5 = makeYTabAt(621.993867640258);

        XTab left = layoutSpec.getLeft();
        YTab top = layoutSpec.getTop();
        XTab right = layoutSpec.getRight();
        YTab bottom = layoutSpec.getBottom();


        layoutSpec.addArea(new Area(left, y0, x2, bottom));
        layoutSpec.addArea(new Area(x2, y5, x0, bottom));
        layoutSpec.addArea(new Area(x1, y5, right, bottom));

        layoutSpec.addArea(new EmptySpace(left, top, x2, y0));
        layoutSpec.addArea(new EmptySpace(x2, top, x0, y5));
        layoutSpec.addArea(new EmptySpace(x0, top, right, y5));
        layoutSpec.addArea(new EmptySpace(x0, y5, x1, bottom));
/*

Make space for: AreaCandidate(left (-0.004274022636394648), top (0.0), right (1195.9999999923093), Y5 (621.993867640258))
Area(left (-0.004274022636394648),y0 (621.9946225682406),X2 (181.99358896604542),bottom (718.0)),
Area(X2 (181.99358896604542),Y5 (621.993867640258),x0 (408.9935889660454),bottom (718.0)),
Area(x1 (968.9999999923093),Y5 (621.993867640258),right (1195.9999999923093),bottom (718.0))]

EmptySpace(left (-0.004274022636394648),top (0.0),X2 (181.99358896604542),y0 (621.9946225682406)),
EmptySpace(X2 (181.99358896604542),top (0.0),x0 (408.9935889660454),Y5 (621.993867640258)),
EmptySpace(x0 (408.9935889660454),top (0.0),right (1195.9999999923093),Y5 (621.993867640258)),
EmptySpace(x0 (408.9935889660454),Y5 (621.993867640258),x1 (968.9999999923093),bottom (718.0))]


*/

        AlgebraData algebraData = new AlgebraData(layoutSpec, null);
        LambdaTransformation trafo = new LambdaTransformation(algebraData);

        EmptySpace emptySpace = trafo.makeSpace(left, top, right, y5);
        assertTrue(emptySpace != null);
    }
}
