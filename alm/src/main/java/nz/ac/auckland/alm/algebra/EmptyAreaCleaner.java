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
    final AlgebraData algebraData;
    final LambdaTransformation trafo;

    public EmptyAreaCleaner(AlgebraData algebraData) {
        this.algebraData = algebraData;
        this.trafo = new LambdaTransformation(algebraData);
    }

    private boolean hasOnlyEmptySpaces(List<IArea> list) {
        for (IArea area : list) {
            if (!(area instanceof EmptySpace))
                return false;
        }
        return true;
    }

    private <Tab extends Variable, OrthTab extends Variable>
    boolean clean(IDirection direction, Map<Tab, Edge> map,  IDirection orthDirection, Map<OrthTab, Edge> orthMap) {
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
    public boolean clean() {
        Map<XTab, Edge> xTabEdgeMap = algebraData.getXTabEdges();
        Map<YTab, Edge> yTabEdgeMap = algebraData.getYTabEdges();
        if (!clean(new RightDirection(), xTabEdgeMap, new BottomDirection(), yTabEdgeMap))
            return false;
        if (!clean(new BottomDirection(), yTabEdgeMap, new RightDirection(), xTabEdgeMap))
            return false;

        return true;
    }
}
