/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm.algebra;


import nz.ac.auckland.alm.*;
import nz.ac.auckland.linsolve.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SoundLayoutBuilder {

    static private <Tab extends Variable> Tab findEmptySpaceTap(Area target, Map<Tab, Edge> tabEdgeMap,
                                                                IDirection direction, List<EmptySpace> emptySpaces) {
        if (tabEdgeMap.containsKey(direction.getTab(target)))
            return (Tab)direction.getTab(target);

        int directionFactor = 1;
        if (direction instanceof LeftDirection || direction instanceof TopDirection)
            directionFactor = -1;
        Tab targetTab = (Tab)direction.getTab(target);
        Tab closestTab = null;
        double minDist = Double.MAX_VALUE;
        for (EmptySpace space : emptySpaces) {
            Tab currentTab = (Tab)direction.getTab(space);
            double distance = directionFactor * (currentTab.getValue() - targetTab.getValue());
            if (distance < 0)
                continue;
            if (distance < minDist) {
                minDist = distance;
                closestTab = currentTab;
            }
        }
        return closestTab;
    }

    static public boolean fillWithEmptySpaces(LayoutSpec layoutSpec) {
        List<Area> areas = new ArrayList<Area>();
        for (IArea area : layoutSpec.getAreas()) {
            if (area instanceof Area)
                areas.add((Area)area);
        }

        IDirection leftDirection = new LeftDirection();
        IDirection topDirection = new TopDirection();
        IDirection rightDirection = new RightDirection();
        IDirection bottomDirection = new BottomDirection();

        XTab left = layoutSpec.getLeft();
        YTab top = layoutSpec.getTop();
        XTab right = layoutSpec.getRight();
        YTab bottom = layoutSpec.getBottom();

        LayoutStructure layoutStructure = new LayoutStructure(left, top, right, bottom);
        // add first empty element
        layoutStructure.addArea(new EmptySpace(left, top, right, bottom));

        LambdaTransformation trafo = new LambdaTransformation(layoutStructure);

        Map<XTab, Edge> xTabEdgeMap = layoutStructure.getXTabEdges();
        Map<YTab, Edge> yTabEdgeMap = layoutStructure.getYTabEdges();
        for (Area area : areas) {
            XTab leftLarge = findEmptySpaceTap(area, xTabEdgeMap, leftDirection, layoutStructure.getEmptySpaces());
            YTab topLarge = findEmptySpaceTap(area, yTabEdgeMap, topDirection, layoutStructure.getEmptySpaces());
            XTab rightLarge = findEmptySpaceTap(area, xTabEdgeMap, rightDirection, layoutStructure.getEmptySpaces());
            YTab bottomLarge = findEmptySpaceTap(area, yTabEdgeMap, bottomDirection, layoutStructure.getEmptySpaces());

            EmptySpace emptySpace = trafo.makeSpace(leftLarge, topLarge, rightLarge, bottomLarge);
            if (emptySpace == null)
                return false;
            // remove empty space and replace it
            layoutStructure.removeArea(emptySpace);
            layoutStructure.addArea(area);

            // add possible other empty spaces
            if (leftLarge != area.getLeft()) {
                EmptySpace gap = new EmptySpace(leftLarge, topLarge, area.getLeft(), bottomLarge);
                layoutStructure.addArea(gap);
                leftLarge = area.getLeft();
            }
            if (rightLarge != area.getRight()) {
                EmptySpace gap = new EmptySpace(area.getRight(), topLarge, rightLarge, bottomLarge);
                layoutStructure.addArea(gap);
                rightLarge = area.getRight();
            }
            if (topLarge != area.getTop()) {
                EmptySpace gap = new EmptySpace(leftLarge, topLarge, rightLarge, area.getTop());
                layoutStructure.addArea(gap);
            }
            if (bottomLarge != area.getBottom()) {
                EmptySpace gap = new EmptySpace(leftLarge, area.getBottom(), rightLarge, bottomLarge);
                layoutStructure.addArea(gap);
            }
        }

        layoutStructure.applyToLayoutSpec(layoutSpec);
        return true;
    }
}
