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


abstract public class AbstractHorizontalDirection implements IDirection<XTab, YTab> {
  @Override
  public Edge getEdge(IArea area, Map<XTab, Edge> map) {
    return map.get(getTab(area));
  }

  @Override
  public Edge getOppositeEdge(IArea area, Map<XTab, Edge> map) {
    return map.get(getOppositeTab(area));
  }

  @Override
  public Edge getOrthogonalEdge1(IArea area, Map<YTab, Edge> map) {
    return map.get(getOrthogonalTab1(area));
  }

  @Override
  public Edge getOrthogonalEdge2(IArea area, Map<YTab, Edge> map) {
    return map.get(getOrthogonalTab2(area));
  }

  @Override
  public YTab getOrthogonalTab1(IArea area) {
    return area.getTop();
  }

  @Override
  public YTab getOrthogonalTab2(IArea area) {
    return area.getBottom();
  }

  @Override
  public void setOrthogonalTab1(IArea area, YTab tab) {
    area.setTopBottom(tab, area.getBottom());
  }

  @Override
  public void setOrthogonalTab2(IArea area, YTab tab) {
    area.setTopBottom(area.getTop(), tab);
  }

  @Override
  public double getExtent(Area.Size size) {
    return size.getWidth();
  }

  @Override
  public double getOrthogonalExtent(Area.Size size) {
    return size.getHeight();
  }

  @Override
  public XTab createTab() {
    return new XTab();
  }

  @Override
  public YTab createOrthogonalTab() {
    return new YTab();
  }

  @Override
  public Map<XTab, Edge> getTabEdgeMap(AlgebraData data) {
    return data.getXTabEdges();
  }

  @Override
  public Map<YTab, Edge> getOrthTabEdgeMap(AlgebraData data) {
    return data.getYTabEdges();
  }

  @Override
  public IDirection getOrthogonalDirection1() {
    return new TopDirection();
  }

  @Override
  public IDirection getOrthogonalDirection2() {
    return new BottomDirection();
  }
}
