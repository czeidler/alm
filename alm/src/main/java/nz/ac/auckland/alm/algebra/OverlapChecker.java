/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm.algebra;

import nz.ac.auckland.alm.IArea;
import nz.ac.auckland.alm.XTab;
import nz.ac.auckland.alm.YTab;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class OverlapChecker {
    static public boolean isNonOverlapping(LayoutStructure layoutStructure) {
        int numberOfAreas = layoutStructure.getAreas().size() + layoutStructure.getEmptySpaces().size();

        IDirection left = new LeftDirection();
        IDirection top = new TopDirection();
        IDirection right = new RightDirection();
        IDirection bottom = new BottomDirection();

        Map<XTab, Edge> xTabEdgeMap = layoutStructure.getXTabEdges();
        Map<YTab, Edge> yTabEdgeMap = layoutStructure.getYTabEdges();
        for (IArea area : layoutStructure.getAllAreas()) {
            List<IArea> connections = new ArrayList<IArea>();
            Edge.collectAreasInChain(left.getEdge(area, xTabEdgeMap), xTabEdgeMap, left, connections);
            Edge.collectAreasInChain(right.getEdge(area, xTabEdgeMap), xTabEdgeMap, right, connections);

            Edge.collectAreasInChain(top.getEdge(area, yTabEdgeMap), yTabEdgeMap, top, connections);
            Edge.collectAreasInChain(bottom.getEdge(area, yTabEdgeMap), yTabEdgeMap, bottom, connections);

            if (connections.size() != numberOfAreas - 1)
                return false;
        }
        return true;
    }
}
