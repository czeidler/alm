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


public interface IDirection {
  <Tab> Edge getEdge(IArea area, Map<Tab, Edge> map);
  Variable getTab(IArea area);
  Variable getOppositeTab(IArea area);
  Variable getOrthogonalTab1(IArea area);
  Variable getOrthogonalTab2(IArea area);
  Variable getTab(LayoutSpec layoutSpec);
  List<IArea> getAreas(Edge edge);
  List<IArea> getOppositeAreas(Edge edge);

  double getExtent(Area.Size size);
  double getOrthogonalExtent(Area.Size size);

  Variable createTab();
  Variable createOrthogonalTab();

  void setTab(IArea area, Variable tab);
  void setOppositeTab(IArea area, Variable tab);
  void setOrthogonalTab1(IArea area, Variable tab);
  void setOrthogonalTab2(IArea area, Variable tab);
  void setTabs(IArea area, Variable tab, Variable orthTab1, Variable oppositeTab, Variable orthTab2);
}