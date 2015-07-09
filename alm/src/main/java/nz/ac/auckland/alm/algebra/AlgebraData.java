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

import java.util.*;


public class AlgebraData {
  final XTab left;
  final YTab top;
  final XTab right;
  final YTab bottom;
  final Map<XTab, Edge> xTabEdgeMap;
  final Map<YTab, Edge> yTabEdgeMap;
  List<XTab> sortedXTabs;
  List<YTab> sortedYTabs;
  final List<Area> areas;
  final List<EmptySpace> emptySpaces;

  Comparator<Variable> tabComparator = new Comparator<Variable>() {
    @Override
    public int compare(Variable variable, Variable variable2) {
      return variable.getValue() < variable2.getValue() ? -1 : 1;
    }
  };

  public AlgebraData(XTab left, YTab top, XTab right, YTab bottom) {
    this.left = left;
    this.top = top;
    this.right = right;
    this.bottom = bottom;

    this.xTabEdgeMap = new HashMap<XTab, Edge>();
    this.yTabEdgeMap = new HashMap<YTab, Edge>();

    this.areas = new ArrayList<Area>();
    this.emptySpaces = new ArrayList<EmptySpace>();

  }

  public AlgebraData(LayoutSpec layoutSpec, Area removedArea) {
    this.left = layoutSpec.getLeft();
    this.top = layoutSpec.getTop();
    this.right = layoutSpec.getRight();
    this.bottom = layoutSpec.getBottom();

    this.xTabEdgeMap = new HashMap<XTab, Edge>();
    this.yTabEdgeMap = new HashMap<YTab, Edge>();

    this.areas = new ArrayList<Area>();
    this.emptySpaces = new ArrayList<EmptySpace>();

    for (IArea area : layoutSpec.getAreas()) {
      if (area == removedArea)
        continue;
      if (area instanceof Area)
        this.areas.add((Area)area);
      if (area instanceof EmptySpace)
        this.emptySpaces.add((EmptySpace)area);
    }
    Edge.fillEdges(layoutSpec, xTabEdgeMap, yTabEdgeMap, removedArea);
    if (removedArea != null) {
      Edge.addArea(new EmptySpace(removedArea.getLeft(), removedArea.getTop(), removedArea.getRight(),
              removedArea.getBottom()), xTabEdgeMap, yTabEdgeMap);
    }
  }

  public void applyToLayoutSpec(LayoutSpec layoutSpec) {
    assert left == layoutSpec.getLeft();
    assert top == layoutSpec.getTop();
    assert right == layoutSpec.getRight();
    assert bottom == layoutSpec.getBottom();

    while (layoutSpec.getAreas().size() > 0)
      layoutSpec.removeArea(layoutSpec.getAreas().get(0));

    for (Area area : areas)
      layoutSpec.addArea(area);
    for (EmptySpace space : emptySpaces)
      layoutSpec.addArea(space);
  }

  public void addArea(IArea area) {
    Edge.addArea(area, xTabEdgeMap, yTabEdgeMap);

    if (area instanceof Area)
      areas.add((Area)area);
    if (area instanceof EmptySpace)
      emptySpaces.add((EmptySpace)area);

    invalidateTabs();
  }

  public <Tab extends Variable, OrthTab extends Variable>
  void mergeTabs(Tab tab, Tab goneTab, IDirection<Tab, OrthTab> direction) {
    assert tab != goneTab;

    Map<Tab, Edge> map = direction.getTabEdgeMap(this);
    Edge edge = map.get(tab);
    Edge goneEdge = map.get(goneTab);
    map.remove(goneEdge);

    List<IArea> goneAreas = direction.getAreas(goneEdge);
    List<IArea> areas = direction.getAreas(edge);
    for (IArea area : goneAreas) {
      direction.setOppositeTab(area, tab);
      areas.add(area);
    }
    IDirection<Tab, OrthTab> oppositeDirection = direction.getOppositeDirection();
    goneAreas = oppositeDirection.getAreas(goneEdge);
    areas = oppositeDirection.getAreas(edge);
    for (IArea area : goneAreas) {
      oppositeDirection.setOppositeTab(area, tab);
      areas.add(area);
    }

    invalidateTabs();
  }

  public void removeArea(IArea area) {
    Edge.removeArea(area, xTabEdgeMap, yTabEdgeMap);

    if (area instanceof Area)
      areas.remove(area);
    if (area instanceof EmptySpace)
      emptySpaces.remove(area);

    invalidateTabs();
  }

  public Iterable<IArea> getAllAreas() {
    return new JoinedList<IArea>(areas, emptySpaces);
  }

  public List<Area> getAreas() {
    return areas;
  }

  public List<EmptySpace> getEmptySpaces() {
    return emptySpaces;
  }

  public Map<XTab, Edge> getXTabEdges() {
    return xTabEdgeMap;
  }

  public Map<YTab, Edge> getYTabEdges() {
    return yTabEdgeMap;
  }

  public List<XTab> getSortedXTabs() {
    if (sortedXTabs != null)
      return sortedXTabs;
    sortedXTabs = new ArrayList<XTab>(xTabEdgeMap.keySet());
    Collections.sort(sortedXTabs, tabComparator);
    return sortedXTabs;
  }

  public List<YTab> getSortedYTabs() {
    if (sortedYTabs != null)
      return sortedYTabs;

    sortedYTabs = new ArrayList<YTab>(yTabEdgeMap.keySet());
    Collections.sort(sortedYTabs, tabComparator);
    return sortedYTabs;
  }

  private void invalidateTabs() {
    sortedXTabs = null;
    sortedYTabs = null;
  }

  public XTab getLeft() {
    return left;
  }

  public YTab getTop() {
    return top;
  }

  public XTab getRight() {
    return right;
  }

  public YTab getBottom() {
    return bottom;
  }
}
