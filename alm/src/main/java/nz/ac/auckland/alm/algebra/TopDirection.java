/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm.algebra;

import nz.ac.auckland.alm.*;

import java.util.List;


public class TopDirection extends AbstractVerticalDirection {
  @Override
  public YTab getTab(IArea area) {
    return area.getTop();
  }

  @Override
  public YTab getOppositeTab(IArea area) {
    return area.getBottom();
  }

  @Override
  public YTab getTab(LayoutSpec layoutSpec) {
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
  public IDirection getOppositeDirection() {
    return new BottomDirection();
  }

  @Override
  public void setTab(IArea area, YTab tab) {
    area.setTopBottom(tab, area.getBottom());
  }

  @Override
  public void setOppositeTab(IArea area, YTab tab) {
    area.setTopBottom(area.getTop(), tab);
  }

  @Override
  public void setTabs(IArea area, YTab tab, XTab orthTab1, YTab oppositeTab, XTab orthTab2) {
    area.setLeftRight(orthTab1, orthTab2);
    area.setTopBottom(tab, oppositeTab);
  }
}
