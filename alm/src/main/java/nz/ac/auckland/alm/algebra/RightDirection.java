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


public class RightDirection extends AbstractHorizontalDirection {
  @Override
  public XTab getTab(IArea area) {
    return area.getRight();
  }

  @Override
  public XTab getOppositeTab(IArea area) {
    return area.getLeft();
  }

  @Override
  public XTab getTab(LayoutSpec layoutSpec) {
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
  public void setTab(IArea area, XTab tab) {
    area.setLeftRight(area.getLeft(), tab);
  }

  @Override
  public void setOppositeTab(IArea area, XTab tab) {
    area.setLeftRight(tab, area.getRight());
  }

  @Override
  public void setTabs(IArea area, XTab tab, YTab orthTab1, XTab oppositeTab, YTab orthTab2) {
    area.setLeftRight(oppositeTab, tab);
    area.setTopBottom(orthTab1, orthTab2);
  }
}
