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


public class LeftDirection extends AbstractHorizontalDirection {
  @Override
  public XTab getTab(IArea area) {
    return area.getLeft();
  }

  @Override
  public XTab getOppositeTab(IArea area) {
    return area.getRight();
  }

  @Override
  public XTab getTab(LayoutSpec layoutSpec) {
    return layoutSpec.getLeft();
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
  public IDirection<XTab, YTab> getOppositeDirection() {
    return new RightDirection();
  }

  @Override
  public XTab getTab(AlgebraData data) {
    return data.getLeft();
  }

  @Override
  public void setTab(IArea area, XTab tab) {
    area.setLeftRight(tab, area.getRight());
  }

  @Override
  public void setOppositeTab(IArea area, XTab tab) {
    area.setLeftRight(area.getLeft(), tab);
  }

  @Override
  public void setTabs(IArea area, XTab tab, YTab orthTab1, XTab oppositeTab, YTab orthTab2) {
    area.setLeftRight(tab, oppositeTab);
    area.setTopBottom(orthTab1, orthTab2);
  }
}
