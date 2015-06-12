/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm.test;

import nz.ac.auckland.alm.Area;
import nz.ac.auckland.alm.LayoutSpec;

import nz.ac.auckland.alm.XTab;
import nz.ac.auckland.alm.YTab;
import junit.framework.TestCase;
import nz.ac.auckland.linsolve.Constraint;
import nz.ac.auckland.linsolve.Summand;


public class ALM extends TestCase {
    void assertFuzzyEqual(double expected, double value) throws Exception {
        if (Math.abs(expected - value) < 0.01)
            return;
        throw new Exception("assertFuzzyEqual: expected = " + expected + " value = " + value);
    }

    Area addDefaultArea(LayoutSpec layoutSpec, XTab left, YTab top, XTab right, YTab bottom) {
        Area area = layoutSpec.addArea(left, top, right, bottom);
        area.setMinSize(10, 10);
        area.setMaxSize(-1, -1);
        area.setPreferredSize(20, 50);
        return area;
    }

    public void testThreeButtons() throws Exception {
        final int LAYOUT_WIDTH = 300;
        final int LAYOUT_HEIGHT = 50;

        LayoutSpec layoutSpec = new LayoutSpec();
        layoutSpec.setRight(LAYOUT_WIDTH);
        layoutSpec.setBottom(LAYOUT_HEIGHT);

        XTab left = layoutSpec.getLeft();
        YTab top = layoutSpec.getTop();
        XTab right = layoutSpec.getRight();
        YTab bottom = layoutSpec.getBottom();

        XTab x1 = new XTab();
        XTab x2 = new XTab();

        addDefaultArea(layoutSpec, left, top, x1, bottom);
        addDefaultArea(layoutSpec, x1, top, x2, bottom);
        addDefaultArea(layoutSpec, x2, top, right, bottom);

        long time = System.currentTimeMillis();
        layoutSpec.solve();
        System.out.println("ThreeButtons solving time: " + (System.currentTimeMillis() - time) + "ms");

        assertFuzzyEqual(0, left.getValue());
        assertFuzzyEqual(0, top.getValue());
        assertFuzzyEqual(LAYOUT_WIDTH, right.getValue());
        assertFuzzyEqual(LAYOUT_HEIGHT, bottom.getValue());

        assertTrue(left.getValue() < x1.getValue());
        assertTrue(x1.getValue() < x2.getValue());
        assertTrue(x2.getValue() < right.getValue());

        //assertFuzzyEqual(LAYOUT_WIDTH / 3, x1.getValue());
        //assertFuzzyEqual(LAYOUT_WIDTH * 2 / 3, x1.getValue());
    }

    public void testPinWheel() throws Exception {
        final int LAYOUT_WIDTH = 300;
        final int LAYOUT_HEIGHT = 300;

        LayoutSpec layoutSpec = new LayoutSpec();
        layoutSpec.setRight(LAYOUT_WIDTH);
        layoutSpec.setBottom(LAYOUT_HEIGHT);

        XTab left = layoutSpec.getLeft();
        YTab top = layoutSpec.getTop();
        XTab right = layoutSpec.getRight();
        YTab bottom = layoutSpec.getBottom();

        XTab x1 = new XTab();
        XTab x2 = new XTab();

        YTab y1 = new YTab();
        YTab y2 = new YTab();

        // outer areas
        addDefaultArea(layoutSpec, left, top, x2, y1);
        addDefaultArea(layoutSpec, x2, top, right, y2);
        addDefaultArea(layoutSpec, x1, y2, right, bottom);
        addDefaultArea(layoutSpec, left, y1, x1, bottom);
        // middle
        addDefaultArea(layoutSpec, x1, y1, x2, y2);

        long time = System.currentTimeMillis();
        layoutSpec.solve();
        System.out.println("PinWheel solving time: " + (System.currentTimeMillis() - time) + "ms");

        assertFuzzyEqual(0, left.getValue());
        assertFuzzyEqual(0, top.getValue());
        assertFuzzyEqual(LAYOUT_WIDTH, right.getValue());
        assertFuzzyEqual(LAYOUT_HEIGHT, bottom.getValue());

        assertTrue(left.getValue() < x1.getValue());
        assertTrue(x1.getValue() < x2.getValue());
        assertTrue(x2.getValue() < right.getValue());

        assertTrue(top.getValue() < y1.getValue());
        assertTrue(y1.getValue() < y2.getValue());
        assertTrue(y2.getValue() < bottom.getValue());
    }

    private LayoutSpec createPinWheel() {
        final int LAYOUT_WIDTH = 300;
        final int LAYOUT_HEIGHT = 300;

        LayoutSpec layoutSpec = new LayoutSpec();
        layoutSpec.setRight(LAYOUT_WIDTH);
        layoutSpec.setBottom(LAYOUT_HEIGHT);

        XTab left = layoutSpec.getLeft();
        YTab top = layoutSpec.getTop();
        XTab right = layoutSpec.getRight();
        YTab bottom = layoutSpec.getBottom();

        XTab x1 = new XTab();
        XTab x2 = new XTab();

        YTab y1 = new YTab();
        YTab y2 = new YTab();

        // outer areas
        addDefaultArea(layoutSpec, left, top, x2, y1);
        addDefaultArea(layoutSpec, x2, top, right, y2);
        addDefaultArea(layoutSpec, x1, y2, right, bottom);
        addDefaultArea(layoutSpec, left, y1, x1, bottom);
        // middle
        addDefaultArea(layoutSpec, x1, y1, x2, y2);
        return layoutSpec;
    }

    private void assertEqualClones(LayoutSpec layoutSpec, LayoutSpec clone) throws Exception {
        assertEquals(layoutSpec.getAreas().size(), clone.getAreas().size());
        assertEquals(layoutSpec.getCustomConstraints().size(), clone.getCustomConstraints().size());

        assertFuzzyEqual(layoutSpec.getLeft().getValue(), clone.getLeft().getValue());
        assertFuzzyEqual(layoutSpec.getTop().getValue(), clone.getTop().getValue());
        assertFuzzyEqual(layoutSpec.getRight().getValue(), clone.getRight().getValue());
        assertFuzzyEqual(layoutSpec.getBottom().getValue(), clone.getBottom().getValue());

        for (int i = 0; i < layoutSpec.getAreas().size(); i++) {
            Area area = layoutSpec.getAreas().get(i);
            Area clonedArea = clone.getAreas().get(i);
            assertTrue(area.getMinSize().equals(clonedArea.getMinSize()));
            assertTrue(area.getPreferredSize().equals(clonedArea.getPreferredSize()));
            assertTrue(area.getMaxSize().equals(clonedArea.getMaxSize()));
            assertEquals(area.getHorizontalAlignment(), clonedArea.getHorizontalAlignment());
            assertEquals(area.getVerticalAlignment(), clonedArea.getVerticalAlignment());

            assertFuzzyEqual(area.getLeft().getValue(), clonedArea.getLeft().getValue());
            assertFuzzyEqual(area.getTop().getValue(), clonedArea.getTop().getValue());
            assertFuzzyEqual(area.getRight().getValue(), clonedArea.getRight().getValue());
            assertFuzzyEqual(area.getBottom().getValue(), clonedArea.getBottom().getValue());
        }

        for (int i = 0; i < layoutSpec.getCustomConstraints().size(); i++) {
            Constraint constraint = layoutSpec.getCustomConstraints().get(i);
            Constraint clonedConstraint = clone.getCustomConstraints().get(i);
            assertEquals(constraint.getLeftSide().length, clonedConstraint.getLeftSide().length);
            assertEquals(constraint.getOp(), clonedConstraint.getOp());
            assertEquals(constraint.getRightSide(), clonedConstraint.getRightSide());
            assertEquals(constraint.getPenalty(), clonedConstraint.getPenalty());

            for (int a = 0; a < constraint.getLeftSide().length; a++) {
                Summand summand = constraint.getLeftSide()[a];
                Summand clonedSummand = clonedConstraint.getLeftSide()[a];
                assertEquals(summand.getCoeff(), clonedSummand.getCoeff());
                assertFuzzyEqual(summand.getVar().getValue(), clonedSummand.getVar().getValue());
            }
        }
    }

    public void testClone() throws Exception {
        LayoutSpec layoutSpec = createPinWheel();
        layoutSpec.solve();
        LayoutSpec clone = layoutSpec.clone();
        clone.solve();
        // we just assume that both layouts have the same values after solving
        assertEqualClones(layoutSpec, clone);
    }
}
