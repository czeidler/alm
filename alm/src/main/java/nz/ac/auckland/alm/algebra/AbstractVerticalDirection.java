/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
