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

import nz.ac.auckland.alm.*;
import nz.ac.auckland.linsolve.Variable;

import java.util.List;
import java.util.Map;


public class TopDirection extends AbstractVerticalDirection {
  @Override
  public <Tab> Edge getEdge(IArea area, Map<Tab, Edge> map) {
    return map.get(area.getTop());
  }

  @Override
  public Variable getTab(IArea area) {
    return area.getTop();
  }

  @Override
  public Variable getOppositeTab(IArea area) {
    return area.getBottom();
  }

  @Override
  public Variable getTab(LayoutSpec layoutSpec) {
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
  public void setTab(IArea area, Variable tab) {
    area.setTopBottom((YTab)tab, area.getBottom());
  }

  @Override
  public void setOppositeTab(IArea area, Variable tab) {
    area.setTopBottom(area.getTop(), (YTab)tab);
  }

  @Override
  public void setTabs(IArea area, Variable tab, Variable orthTab1, Variable oppositeTab, Variable orthTab2) {
    area.setLeftRight((XTab)orthTab1, (XTab)orthTab2);
    area.setTopBottom((YTab)tab, (YTab)oppositeTab);
  }
}
