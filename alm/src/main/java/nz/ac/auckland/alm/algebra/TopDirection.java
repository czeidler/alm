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
  public IDirection<YTab, XTab> getOppositeDirection() {
    return new BottomDirection();
  }

  @Override
  public YTab getTab(AlgebraData data) {
    return data.getTop();
  }

  @Override
  public void setTab(IArea area, YTab tab) {
    area.setTop(tab);
  }

  @Override
  public void setOppositeTab(IArea area, YTab tab) {
    area.setBottom(tab);
  }

  @Override
  public void setTabs(IArea area, YTab tab, XTab orthTab1, YTab oppositeTab, XTab orthTab2) {
    area.setLeft(orthTab1);
    area.setRight(orthTab2);
    area.setTop(tab);
    area.setBottom(oppositeTab);
  }
}
