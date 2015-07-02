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

        AlgebraData algebraData = new AlgebraData(left, top, right, bottom);
        // add first empty element
        algebraData.addArea(new EmptySpace(left, top, right, bottom));

        LambdaTransformation trafo = new LambdaTransformation(algebraData);

        Map<XTab, Edge> xTabEdgeMap = algebraData.getXTabEdges();
        Map<YTab, Edge> yTabEdgeMap = algebraData.getYTabEdges();
        for (Area area : areas) {
            XTab leftLarge = findEmptySpaceTap(area, xTabEdgeMap, leftDirection, algebraData.getEmptySpaces());
            YTab topLarge = findEmptySpaceTap(area, yTabEdgeMap, topDirection, algebraData.getEmptySpaces());
            XTab rightLarge = findEmptySpaceTap(area, xTabEdgeMap, rightDirection, algebraData.getEmptySpaces());
            YTab bottomLarge = findEmptySpaceTap(area, yTabEdgeMap, bottomDirection, algebraData.getEmptySpaces());

            EmptySpace emptySpace = trafo.makeSpace(leftLarge, topLarge, rightLarge, bottomLarge);
            if (emptySpace == null)
                return false;

            TilingAlgebra.placeAreaInEmptySpace(algebraData, area, emptySpace);
        }

        algebraData.applyToLayoutSpec(layoutSpec);
        return true;
    }
}
