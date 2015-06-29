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


public class RightDirection extends AbstractHorizontalDirection {
  @Override
  public Variable getTab(IArea area) {
    return area.getRight();
  }

  @Override
  public Variable getOppositeTab(IArea area) {
    return area.getLeft();
  }

  @Override
  public Variable getTab(LayoutSpec layoutSpec) {
    return layoutSpec.getRight();
  }

  @Override
  public List<IArea> getAreas(Edge edge) {
    return edge.areas2;
  }

  @Override
  public List<IArea> getOppositeAreas(Edge edge) {
    return edge.areas1;
  }

  @Override
  public IDirection getOppositeDirection() {
    return new LeftDirection();
  }

  @Override
  public void setTab(IArea area, Variable tab) {
    area.setLeftRight(area.getLeft(), (XTab)tab);
  }

  @Override
  public void setOppositeTab(IArea area, Variable tab) {
    area.setLeftRight((XTab)tab, area.getRight());
  }

  @Override
  public void setTabs(IArea area, Variable tab, Variable orthTab1, Variable oppositeTab, Variable orthTab2) {
    area.setLeftRight((XTab)oppositeTab, (XTab)tab);
    area.setTopBottom((YTab)orthTab1, (YTab)orthTab2);
  }
}
