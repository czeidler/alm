/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm.algebra;


import nz.ac.auckland.alm.Area;
import nz.ac.auckland.alm.EmptySpace;
import nz.ac.auckland.alm.XTab;
import nz.ac.auckland.alm.YTab;
import nz.ac.auckland.linsolve.Variable;

import java.util.Map;

public class AlgebraOperations {
    /**
     * Merge area2 into area1.
     *
     * @param area1
     * @param area2
     * @param direction must point from area1 to area2
     * @return
     */
    static public boolean merge(AlgebraData algebraData, EmptySpace area1, EmptySpace area2, IDirection direction) {
        assert (direction.getTab(area1) == direction.getOppositeTab(area2));

        if (direction.getOrthogonalTab1(area1) != direction.getOrthogonalTab1(area2))
            return false;
        if (direction.getOrthogonalTab2(area1) != direction.getOrthogonalTab2(area2))
            return false;

        algebraData.removeArea(area2);
        algebraData.removeArea(area1);
        direction.setTab(area1, direction.getTab(area2));
        algebraData.addArea(area1);
        return true;
    }

    /**
     * Splits an existing EmptySpace. The new EmptySpace is added in direction of direction.
     *
     * @param space
     * @param splitTab
     * @param tabMap
     * @param direction
     * @param <Tab>
     * @return
     */
    static public  <Tab extends Variable> EmptySpace split(AlgebraData algebraData, EmptySpace space,
                                                           Tab splitTab, Map<Tab, Edge> tabMap, IDirection direction) {
        Tab spaceTab = (Tab)direction.getTab(space);
        Tab oppositeSpaceTab = (Tab)direction.getOppositeTab(space);
        if (Edge.isInChain(spaceTab, splitTab, tabMap, direction))
            return null;
        if (Edge.isInChain(oppositeSpaceTab, splitTab, tabMap, direction.getOppositeDirection()))
            return null;

        algebraData.removeArea(space);

        direction.setTab(space, splitTab);

        EmptySpace newEmptySpace = new EmptySpace();
        direction.setOppositeTab(newEmptySpace, splitTab);
        direction.setTab(newEmptySpace, spaceTab);
        direction.setOrthogonalTab1(newEmptySpace, direction.getOrthogonalTab1(space));
        direction.setOrthogonalTab2(newEmptySpace, direction.getOrthogonalTab2(space));

        // update the layout structure
        algebraData.addArea(space);
        algebraData.addArea(newEmptySpace);

        return newEmptySpace;
    }

    static public void placeAreaInEmptySpace(AlgebraData algebraData, Area area, EmptySpace emptySpace) {
        // remove empty space and replace it
        algebraData.removeArea(emptySpace);
        algebraData.addArea(area);

        XTab leftLarge = emptySpace.getLeft();
        YTab topLarge = emptySpace.getTop();
        XTab rightLarge = emptySpace.getRight();
        YTab bottomLarge = emptySpace.getBottom();

        // add possible other empty spaces
        if (emptySpace.getLeft() != area.getLeft()) {
            EmptySpace gap = new EmptySpace(leftLarge, topLarge, area.getLeft(), bottomLarge);
            algebraData.addArea(gap);
            leftLarge = area.getLeft();
        }
        if (rightLarge != area.getRight()) {
            EmptySpace gap = new EmptySpace(area.getRight(), topLarge, rightLarge, bottomLarge);
            algebraData.addArea(gap);
            rightLarge = area.getRight();
        }
        if (topLarge != area.getTop()) {
            EmptySpace gap = new EmptySpace(leftLarge, topLarge, rightLarge, area.getTop());
            algebraData.addArea(gap);
        }
        if (bottomLarge != area.getBottom()) {
            EmptySpace gap = new EmptySpace(leftLarge, area.getBottom(), rightLarge, bottomLarge);
            algebraData.addArea(gap);
        }
    }

    static public void addAreaAtEmptySpace(AlgebraData algebraData, Area area, EmptySpace emptySpace) {
        if (emptySpace == null)
            return;
        algebraData.removeArea(emptySpace);
        area.setTo(emptySpace.getLeft(), emptySpace.getTop(), emptySpace.getRight(), emptySpace.getBottom());
        algebraData.addArea(area);
    }

    static public EmptySpace makeAreaEmpty(AlgebraData algebraData, Area area) {
        if (!algebraData.getAreas().contains(area))
            return null;
        algebraData.removeArea(area);
        EmptySpace space = new EmptySpace(area.getLeft(), area.getTop(), area.getRight(), area.getBottom());
        algebraData.addArea(space);
        return space;
    }
}
