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

import java.util.List;
import java.util.Map;


public class TopDirection extends AbstractVerticalDirection {
  @Override
  public <Tab> Edge getEdge(IArea area, Map<Tab, Edge> map) {
    return map.get(area.getTop());
  }

  @Override
  public Variable getTab(IArea area) {
    return area.getTop();
  }

  @Override
  public Variable getOppositeTab(IArea area) {
    return area.getBottom();
  }

  @Override
  public Variable getTab(LayoutSpec layoutSpec) {
    return layoutSpec.getTop();
  }

  @Override
  public List<IArea> getAreas(Edge edge) {
    return edge.areas1;
  }

  @Override
  public List<IArea> getOppositeAreas(Edge edge) {
    return edge.areas2;
  }

  @Override
  public void setTab(IArea area, Variable tab) {
    area.setTopBottom((YTab)tab, area.getBottom());
  }

  @Override
  public void setOppositeTab(IArea area, Variable tab) {
    area.setTopBottom(area.getTop(), (YTab)tab);
  }

  @Override
  public void setTabs(IArea area, Variable tab, Variable orthTab1, Variable oppositeTab, Variable orthTab2) {
    area.setLeftRight((XTab)orthTab1, (XTab)orthTab2);
    area.setTopBottom((YTab)tab, (YTab)oppositeTab);
  }
}
