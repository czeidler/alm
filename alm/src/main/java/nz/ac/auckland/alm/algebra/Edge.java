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


public class Edge {
    public List<IArea> areas1 = new ArrayList<IArea>();
    public List<IArea> areas2 = new ArrayList<IArea>();

    static private <Tab> Edge getEdge(Tab tab, Map<Tab, Edge> map) {
        Edge edge = map.get(tab);
        if (edge != null)
            return edge;
        edge = new Edge();
        map.put(tab, edge);
        return edge;
    }

    static public <Tab extends Variable> boolean isInChain(Edge edge, Variable tab, Map<Tab, Edge> edges,
                                                          IDirection direction) {
    for (IArea area : direction.getAreas(edge)) {
          Variable currentTab = direction.getTab(area);
          if (currentTab == tab)
              return true;
          if (isInChain(edges.get(currentTab), tab, edges, direction))
              return true;
        }
        return false;
    }

    static public <Tab extends Variable> boolean isInReverseChain(Edge edge, Variable tab, Map<Tab, Edge> edges,
                                                                  IDirection direction) {
        for (IArea area : direction.getOppositeAreas(edge)) {
            Variable currentTab = direction.getOppositeTab(area);
            if (currentTab == tab)
                return true;
            if (isInReverseChain(edges.get(currentTab), tab, edges, direction))
                return true;
        }
        return false;
    }

    static public void fillEdges(LayoutSpec layoutSpec, Map<XTab, Edge> xMap, Map<YTab, Edge> yMap, Area removedArea) {
        for (IArea area : layoutSpec.getAreas()) {
              if (area == removedArea)
                  continue;
            addArea(area, xMap, yMap);
        }
        // ensure that the border tabs are in the map
        getEdge(layoutSpec.getLeft(), xMap);
        getEdge(layoutSpec.getTop(), yMap);
        getEdge(layoutSpec.getRight(), xMap);
        getEdge(layoutSpec.getBottom(), yMap);
    }

    static public void addArea(IArea area, Map<XTab, Edge> xMap, Map<YTab, Edge> yMap) {
        Edge leftEdge = getEdge(area.getLeft(), xMap);
        leftEdge.areas2.add(area);
        Edge topEdge = getEdge(area.getTop(), yMap);
        topEdge.areas2.add(area);
        Edge rightEdge = getEdge(area.getRight(), xMap);
        rightEdge.areas1.add(area);
        Edge bottomEdge = getEdge(area.getBottom(), yMap);
        bottomEdge.areas1.add(area);
    }

    static public void removeArea(IArea area, Map<XTab, Edge> xMap, Map<YTab, Edge> yMap) {
        Edge leftEdge = getEdge(area.getLeft(), xMap);
        leftEdge.areas2.remove(area);
        Edge topEdge = getEdge(area.getTop(), yMap);
        topEdge.areas2.remove(area);
        Edge rightEdge = getEdge(area.getRight(), xMap);
        rightEdge.areas1.remove(area);
        Edge bottomEdge = getEdge(area.getBottom(), yMap);
        bottomEdge.areas1.remove(area);
    }
}
