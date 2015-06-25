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


abstract public class AbstractHorizontalDirection implements IDirection {
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
}
