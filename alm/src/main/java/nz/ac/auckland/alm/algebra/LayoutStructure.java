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


public class LayoutStructure {
  final private LayoutSpec layoutSpec;
  final Map<XTab, Edge> xTabEdgeMap = new HashMap<XTab, Edge>();
  final Map<YTab, Edge> yTabEdgeMap = new HashMap<YTab, Edge>();
  final List<XTab> xTabs;
  final List<YTab> yTabs;
  final List<Area> areas = new ArrayList<Area>();
  final List<EmptySpace> emptySpaces = new ArrayList<EmptySpace>();

  public LayoutStructure(LayoutSpec layoutSpec, Area removedArea) {
    this.layoutSpec = layoutSpec;
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

    xTabs = new ArrayList<XTab>(xTabEdgeMap.keySet());
    yTabs = new ArrayList<YTab>(yTabEdgeMap.keySet());

    Comparator<Variable> tabComparator = new Comparator<Variable>() {
      @Override
      public int compare(Variable variable, Variable variable2) {
        return variable.getValue() < variable2.getValue() ? -1 : 1;
      }
    };
    Collections.sort(xTabs, tabComparator);
    Collections.sort(yTabs, tabComparator);
  }

  public void applyToLayoutSpec() {
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
  }

  public void removeArea(IArea area) {
    Edge.removeArea(area, xTabEdgeMap, yTabEdgeMap);

    if (area instanceof Area)
      areas.remove(area);
    if (area instanceof EmptySpace)
      emptySpaces.remove(area);
  }

  public LayoutSpec getLayoutSpec() {
    return layoutSpec;
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

  public XTab findTabLeftOf(double x) {
    return findFirstSmallerTab(x, xTabs);
  }

  public XTab findTabRightOf(double x) {
    return findFirstLargerTab(x, xTabs);
  }

  public YTab findTabAbove(double y) {
    return findFirstSmallerTab(y, yTabs);
  }

  public YTab findTabBellow(double y) {
    return findFirstLargerTab(y, yTabs);
  }

  public List<XTab> getXTabs() {
    return xTabs;
  }

  public List<YTab> getYTabs() {
    return yTabs;
  }

  private <Tab extends Variable> Tab findFirstLargerTab(double value, List<Tab> tabs) {
    for (int i = 0; i < tabs.size(); i++) {
      Tab tab = tabs.get(i);
      if (tab.getValue() > value)
        return tab;
    }
    return null;
  }

  private <Tab extends Variable> Tab findFirstSmallerTab(double value, List<Tab> tabs) {
    for (int i = tabs.size() - 1; i >= 0; i--) {
      Tab tab = tabs.get(i);
      if (tab.getValue() < value)
        return tab;
    }
    return null;
  }

  public Area findContentAreaAt(float x, float y) {
    for (Area area : getAreas()) {
      if (area.getContentRect().contains(x, y))
        return area;
    }
    return null;
  }

  public Area findAreaAt(float x, float y) {
    for (Area area : getAreas()) {
      if (area.getRect().contains(x, y))
        return area;
    }
    return null;
  }
}
