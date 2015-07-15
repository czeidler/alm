/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm.algebra.string;

import nz.ac.auckland.alm.*;
import nz.ac.auckland.alm.algebra.AlgebraData;
import nz.ac.auckland.alm.algebra.BaseAlgebraTestCase;


public class AlgebraStringTest extends BaseAlgebraTestCase {
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
        layoutSpec.addArea(new EmptySpace(x1, y1, x2, y2));


        AlgebraData data = new AlgebraData(layoutSpec, null);

        AlgebraSpec algebraSpec = new AlgebraSpec(data);
        algebraSpec.compress();

        System.out.println("Pin Wheel:");
        System.out.println(StringWriter.write(algebraSpec));

        assertEquals(2, algebraSpec.getFragments().size());
    }

    public void testPinWheelExtension() {
        LayoutSpec layoutSpec = getLayoutSpec(500, 500);

        XTab left = layoutSpec.getLeft();
        YTab top = layoutSpec.getTop();
        XTab right = layoutSpec.getRight();
        YTab bottom = layoutSpec.getBottom();

        XTab x1 = makeXTabAt(100);
        XTab x2 = makeXTabAt(200);
        XTab x3 = makeXTabAt(300);
        XTab x4 = makeXTabAt(400);

        YTab y1 = makeYTabAt(100);
        YTab y2 = makeYTabAt(200);

        // outer areas
        layoutSpec.addArea(new Area(left, top, x2, y1));
        layoutSpec.addArea(new Area(x2, top, x3, y2));
        layoutSpec.addArea(new Area(x1, y2, x3, bottom));
        layoutSpec.addArea(new Area(left, y1, x1, bottom));
        // middle
        layoutSpec.addArea(new EmptySpace(x1, y1, x2, y2));

        layoutSpec.addArea(new Area(x3, top, x4, bottom));
        layoutSpec.addArea(new Area(x4, top, right, bottom));

        AlgebraData data = new AlgebraData(layoutSpec, null);

        AlgebraSpec algebraSpec = new AlgebraSpec(data);
        algebraSpec.compress();

        System.out.println("Pin Wheel extension:");
        System.out.println(StringWriter.write(algebraSpec));
    }

    public void testGrid() {
        LayoutSpec layoutSpec = getLayoutSpec(500, 500);

        XTab left = layoutSpec.getLeft();
        YTab top = layoutSpec.getTop();
        XTab right = layoutSpec.getRight();
        YTab bottom = layoutSpec.getBottom();

        XTab x0 = makeXTabAt(100);
        XTab x1 = makeXTabAt(200);

        YTab y0 = makeYTabAt(100);
        YTab y1 = makeYTabAt(200);

        // row1
        layoutSpec.addArea(new Area(left, top, x0, y0));
        layoutSpec.addArea(new Area(x0, top, x1, y0));
        layoutSpec.addArea(new Area(x1, top, right, y0));
        // row2
        layoutSpec.addArea(new Area(left, y0, x0, y1));
        layoutSpec.addArea(new Area(x0, y0, x1, y1));
        layoutSpec.addArea(new Area(x1, y0, right, y1));
        // row3
        layoutSpec.addArea(new Area(left, y1, x0, bottom));
        layoutSpec.addArea(new Area(x0, y1, x1, bottom));
        layoutSpec.addArea(new Area(x1, y1, right, bottom));


        AlgebraData data = new AlgebraData(layoutSpec, null);

        AlgebraSpec algebraSpec = new AlgebraSpec(data);
        algebraSpec.compress();

        System.out.println("Grid:");
        System.out.println(StringWriter.write(algebraSpec));

        assertEquals(1, algebraSpec.getFragments().size());
    }

    public void testGroupLayout() {
        LayoutSpec layoutSpec = getLayoutSpec(500, 500);

        XTab left = layoutSpec.getLeft();
        YTab top = layoutSpec.getTop();
        XTab right = layoutSpec.getRight();
        YTab bottom = layoutSpec.getBottom();

        XTab x0 = makeXTabAt(100);
        XTab x1 = makeXTabAt(200);
        XTab x2 = makeXTabAt(300);

        YTab y0 = makeYTabAt(100);
        YTab y1 = makeYTabAt(200);

        // row1
        layoutSpec.addArea(new Area(left, top, x0, y0));
        layoutSpec.addArea(new Area(x0, top, x1, y0));
        // row2
        layoutSpec.addArea(new Area(left, y0, x1, y1));

        // right
        layoutSpec.addArea(new Area(x1, top, x2, y1));
        layoutSpec.addArea(new Area(x2, top, right, y1));

        // bottom
        layoutSpec.addArea(new Area(left, y1, right, bottom));

        AlgebraData data = new AlgebraData(layoutSpec, null);

        AlgebraSpec algebraSpec = new AlgebraSpec(data);
        algebraSpec.compress();

        System.out.println("Group Layout:");
        System.out.println(StringWriter.write(algebraSpec));

        assertEquals(1, algebraSpec.getFragments().size());
    }
}
