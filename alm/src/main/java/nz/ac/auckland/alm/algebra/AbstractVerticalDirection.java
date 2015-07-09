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
import nz.ac.auckland.alm.XTab;
import nz.ac.auckland.alm.YTab;

import java.util.Map;


abstract class AbstractVerticalDirection implements IDirection<YTab, XTab> {
  @Override
  public Edge getEdge(IArea area, Map<YTab, Edge> map) {
    return map.get(getTab(area));
  }

  @Override
  public Edge getOppositeEdge(IArea area, Map<YTab, Edge> map) {
    return map.get(getOppositeTab(area));
  }

  @Override
  public Edge getOrthogonalEdge1(IArea area, Map<XTab, Edge> map) {
    return map.get(getOrthogonalTab1(area));
  }

  @Override
  public Edge getOrthogonalEdge2(IArea area, Map<XTab, Edge> map) {
    return map.get(getOrthogonalTab2(area));
  }


  @Override
  public XTab getOrthogonalTab1(IArea area) {
    return area.getLeft();
  }

  @Override
  public XTab getOrthogonalTab2(IArea area) {
    return area.getRight();
  }

  @Override
  public void setOrthogonalTab1(IArea area, XTab tab) {
    area.setLeftRight(tab, area.getRight());
  }

  @Override
  public void setOrthogonalTab2(IArea area, XTab tab) {
    area.setLeftRight(area.getLeft(), tab);
  }

  @Override
  public double getExtent(IArea area) {
    return area.getBottom().getValue() - area.getTop().getValue();
  }

  @Override
  public double getExtent(Area.Size size) {
    return size.getHeight();
  }

  @Override
  public YTab createTab() {
    return new YTab();
  }

  @Override
  public XTab createOrthogonalTab() {
    return new XTab();
  }

  @Override
  public Map<YTab, Edge> getTabEdgeMap(AlgebraData data) {
    return data.getYTabEdges();
  }

  @Override
  public Map<XTab, Edge> getOrthTabEdgeMap(AlgebraData data) {
    return data.getXTabEdges();
  }

  @Override
  public IDirection<XTab, YTab> getOrthogonalDirection1() {
    return new LeftDirection();
  }

  @Override
  public IDirection<XTab, YTab> getOrthogonalDirection2() {
    return new RightDirection();
  }
}
