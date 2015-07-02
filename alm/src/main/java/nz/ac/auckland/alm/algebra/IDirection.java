/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm.algebra;

import nz.ac.auckland.alm.Area;
import nz.ac.auckland.alm.IArea;
import nz.ac.auckland.alm.LayoutSpec;
import nz.ac.auckland.linsolve.Variable;

import java.util.List;
import java.util.Map;


public interface IDirection<Tab extends Variable, OrthTab extends Variable> {
  Edge getEdge(IArea area, Map<Tab, Edge> map);
  Edge getOppositeEdge(IArea area, Map<Tab, Edge> map);
  Edge getOrthogonalEdge1(IArea area, Map<OrthTab, Edge> map);
  Edge getOrthogonalEdge2(IArea area, Map<OrthTab, Edge> map);
  Tab getTab(IArea area);
  Tab getOppositeTab(IArea area);
  OrthTab getOrthogonalTab1(IArea area);
  OrthTab getOrthogonalTab2(IArea area);
  Tab getTab(LayoutSpec layoutSpec);
  List<IArea> getAreas(Edge edge);
  List<IArea> getOppositeAreas(Edge edge);

  IDirection getOppositeDirection();
  IDirection getOrthogonalDirection1();
  IDirection getOrthogonalDirection2();

  double getExtent(Area.Size size);
  double getOrthogonalExtent(Area.Size size);

  Tab createTab();
  OrthTab createOrthogonalTab();

  void setTab(IArea area, Tab tab);
  void setOppositeTab(IArea area, Tab tab);
  void setOrthogonalTab1(IArea area, OrthTab tab);
  void setOrthogonalTab2(IArea area, OrthTab tab);
  void setTabs(IArea area, Tab tab, OrthTab orthTab1, Tab oppositeTab, OrthTab orthTab2);
}