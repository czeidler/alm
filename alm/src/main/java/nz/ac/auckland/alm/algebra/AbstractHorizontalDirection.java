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
import nz.ac.auckland.linsolve.Variable;

import java.util.Map;


abstract public class AbstractHorizontalDirection implements IDirection {
  @Override
  public <Tab> Edge getEdge(IArea area, Map<Tab, Edge> map) {
    return map.get(getTab(area));
  }

  @Override
  public <Tab> Edge getOppositeEdge(IArea area, Map<Tab, Edge> map) {
    return map.get(getOppositeTab(area));
  }

  @Override
  public Variable getOrthogonalTab1(IArea area) {
    return area.getTop();
  }

  @Override
  public Variable getOrthogonalTab2(IArea area) {
    return area.getBottom();
  }

  @Override
  public void setOrthogonalTab1(IArea area, Variable tab) {
    area.setTopBottom((YTab)tab, area.getBottom());
  }

  @Override
  public void setOrthogonalTab2(IArea area, Variable tab) {
    area.setTopBottom(area.getTop(), (YTab)tab);
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
  public Variable createTab() {
    return new XTab();
  }

  @Override
  public Variable createOrthogonalTab() {
    return new YTab();
  }

  @Override
  public <Tab> Edge getOrthogonalEdge1(IArea area, Map<Tab, Edge> map) {
    return map.get(getOrthogonalTab1(area));
  }

  @Override
  public <Tab> Edge getOrthogonalEdge2(IArea area, Map<Tab, Edge> map) {
    return map.get(getOrthogonalTab2(area));
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
