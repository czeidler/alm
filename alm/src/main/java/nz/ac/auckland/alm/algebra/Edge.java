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

    static public <Tab extends Variable> boolean isInChain(Variable startTab, Variable tab, Map<Tab, Edge> edges,
                                                          IDirection direction) {
        if (startTab == tab)
            return true;
        Edge edge = edges.get(startTab);
        for (IArea area : direction.getAreas(edge)) {
          Variable currentTab = direction.getTab(area);
          if (currentTab == tab)
              return true;
          if (isInChain(currentTab, tab, edges, direction))
              return true;
        }
        return false;
    }

    static public <Tab extends Variable> void collectAreasInChain(Edge edgeToCheck, Map<Tab, Edge> edgeMap,
                                                                  IDirection direction, List<IArea> areas) {
        List<Edge> edgesToCheck = new ArrayList<Edge>();
        edgesToCheck.add(edgeToCheck);
        collectAreasInChain(edgesToCheck, edgeMap, direction, areas);
    }

    static public <Tab extends Variable> void collectAreasInChain(List<Edge> edgesToCheck, Map<Tab, Edge> edgeMap,
                                                                  IDirection direction, List<IArea> areas) {
        List<Edge> handledEdges = new ArrayList<Edge>();
        while (edgesToCheck.size() > 0) {
            Edge edge = edgesToCheck.remove(0);
            List<IArea> neighbours = direction.getAreas(edge);
            for (IArea neighbour : neighbours) {
                ensureInList(areas, neighbour);
                Edge newEdge = direction.getEdge(neighbour, edgeMap);
                if (!handledEdges.contains(newEdge))
                    ensureInList(edgesToCheck, newEdge);
            }
            handledEdges.add(edge);
        }
    }

    static private <T> void ensureInList(List<T> list, T object) {
        if (!list.contains(object))
            list.add(object);
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

    static private <Tab> void removeArea(IArea area, IDirection direction, Map<Tab, Edge> map) {
        Edge edge = direction.getEdge(area, map);
        direction.getOppositeAreas(edge).remove(area);
        if (edge.areas1.size() == 0 && edge.areas2.size() == 0)
            map.remove(direction.getTab(area));
    }

    static public void removeArea(IArea area, Map<XTab, Edge> xMap, Map<YTab, Edge> yMap) {
        removeArea(area, new LeftDirection(), xMap);
        removeArea(area, new RightDirection(), xMap);
        removeArea(area, new TopDirection(), yMap);
        removeArea(area, new BottomDirection(), yMap);
    }
}
