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


abstract class AbstractVerticalDirection implements IDirection {
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
}
