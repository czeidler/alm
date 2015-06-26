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


abstract class AbstractVerticalDirection implements IDirection {
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
    return area.getLeft();
  }

  @Override
  public Variable getOrthogonalTab2(IArea area) {
    return area.getRight();
  }

  @Override
  public void setOrthogonalTab1(IArea area, Variable tab) {
    area.setLeftRight((XTab)tab, area.getRight());
  }

  @Override
  public void setOrthogonalTab2(IArea area, Variable tab) {
    area.setLeftRight(area.getLeft(), (XTab)tab);
  }

  @Override
  public double getExtent(Area.Size size) {
    return size.getHeight();
  }

  @Override
  public double getOrthogonalExtent(Area.Size size) {
    return size.getWidth();
  }

  @Override
  public Variable createTab() {
    return new YTab();
  }

  @Override
  public Variable createOrthogonalTab() {
    return new XTab();
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
    return new LeftDirection();
  }

  @Override
  public IDirection getOrthogonalDirection2() {
    return new RightDirection();
  }
}
