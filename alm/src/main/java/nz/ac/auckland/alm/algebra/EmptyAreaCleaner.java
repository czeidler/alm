/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm.algebra;


import nz.ac.auckland.alm.EmptySpace;
import nz.ac.auckland.alm.IArea;
import nz.ac.auckland.alm.XTab;
import nz.ac.auckland.alm.YTab;
import nz.ac.auckland.linsolve.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Merge EmptyAreas that only refer to other EmptyAreas.
 */
public class EmptyAreaCleaner {
    static private boolean hasOnlyEmptySpaces(List<IArea> list) {
        for (IArea area : list) {
            if (!(area instanceof EmptySpace))
                return false;
        }
        return true;
    }

    static private <Tab extends Variable, OrthTab extends Variable>
    boolean clean(AlgebraData algebraData, IDirection direction, Map<Tab, Edge> map,  IDirection orthDirection,
                  Map<OrthTab, Edge> orthMap) {
        LambdaTransformation trafo = new LambdaTransformation(algebraData);
        // copy the list because we will change the original list
        List<Tab> tabs = new ArrayList<Tab>(map.keySet());
        for (Tab tab : tabs) {
            if (tab == algebraData.getLeft() || tab == algebraData.getRight()
                    || tab == algebraData.getTop() || tab == algebraData.getBottom())
                continue;
            Edge edge = map.get(tab);
            if (!hasOnlyEmptySpaces(edge.areas1) || !hasOnlyEmptySpaces(edge.areas2))
                continue;

            // copy the list because we will change the original list
            for (IArea area : new ArrayList<IArea>(direction.getOppositeAreas(edge))) {
                if (!trafo.extend((EmptySpace)area, direction, map, orthDirection, orthMap))
                    return false;
            }
        }

        return true;
    }

    static public boolean clean(AlgebraData algebraData) {
        Map<XTab, Edge> xTabEdgeMap = algebraData.getXTabEdges();
        Map<YTab, Edge> yTabEdgeMap = algebraData.getYTabEdges();
        if (!clean(algebraData, new RightDirection(), xTabEdgeMap, new BottomDirection(), yTabEdgeMap))
            return false;
        if (!clean(algebraData, new BottomDirection(), yTabEdgeMap, new RightDirection(), xTabEdgeMap))
            return false;

        simplify(algebraData, new LeftDirection());
        simplify(algebraData, new TopDirection());
        return true;
    }

    /**
     * Try to merge adjacent empty areas.
     *
     * @param data
     * @param direction horizontal or vertical direction
     * @param <Tab>
     * @param <OrthTab>
     * @return
     */
    static public <Tab extends Variable, OrthTab extends Variable>
    void simplify(AlgebraData data, IDirection<Tab, OrthTab> direction) {
        IDirection<Tab, OrthTab> oppositeDirection = direction.getOppositeDirection();
        IDirection<OrthTab, Tab> orth1Direction = direction.getOrthogonalDirection1();
        IDirection<OrthTab, Tab> orth2Direction = direction.getOrthogonalDirection2();
        List<EmptySpace> emptySpaces = data.getEmptySpaces();
        for (int i = 0; i < emptySpaces.size(); i++) {
            EmptySpace emptySpace = emptySpaces.get(i);
            Tab tab = direction.getTab(emptySpace);
            Tab oppositeTab = oppositeDirection.getTab(emptySpace);
            OrthTab orthTab1 = orth1Direction.getTab(emptySpace);
            OrthTab orthTab2 = orth2Direction.getTab(emptySpace);
            for (int a = 0; a < emptySpaces.size(); a++) {
                EmptySpace candidate = emptySpaces.get(a);
                if (emptySpace == candidate)
                    continue;
                if (orthTab1 != orth1Direction.getTab(candidate) || orthTab2 != orth2Direction.getTab(candidate))
                    continue;
                if (tab == oppositeDirection.getTab(candidate)) {
                    if (!TilingAlgebra.merge(data, emptySpace, candidate, direction))
                        throw new RuntimeException();
                    i = 0;
                    break;
                }
                if (oppositeTab == direction.getTab(candidate)) {
                    if (!TilingAlgebra.merge(data, emptySpace, candidate, oppositeDirection))
                        throw new RuntimeException();
                    i = 0;
                    break;
                }
            }
        }
    }
}
