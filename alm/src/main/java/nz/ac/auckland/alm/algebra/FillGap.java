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
import nz.ac.auckland.alm.algebra.*;
import nz.ac.auckland.linsolve.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class FillGap {
  static private boolean isInAGroup(List<List<Area>> groups, IArea area) {
    for (List<Area> group : groups) {
      if (group.contains(area))
        return true;
    }
    return false;
  }

  static private <Tab extends Variable, OrthTab extends Variable>
  List<List<Area>> collectGroups(AlgebraData data, Tab tab, IDirection<Tab, OrthTab> direction) {
    List<List<Area>> groups = new ArrayList<List<Area>>();
    Map<Tab, Edge> edgeMap = direction.getTabEdgeMap(data);
    Edge edge = edgeMap.get(tab);
    if (edge == null)
      return groups;
    for (IArea area : direction.getAreas(edge)) {
      if (!(area instanceof Area))
        continue;
      if (isInAGroup(groups, area))
        continue;

      List<Area> newGroup = LayoutItemPath.detect((Area)area, data.getXTabEdges(), data.getYTabEdges());
      groups.add(newGroup);
    }

    return groups;
  }

  static private <Tab extends Variable> boolean contains(Tab tab, List<Area> areas, IDirection direction) {
    for (Area area : areas) {
      if (direction.getTab(area) == tab)
        return true;
    }
    return false;
  }

  static public <Tab extends Variable, OrthTab extends Variable>
  boolean isConnectedToBorder1OrBorder2(AlgebraData data, List<Area> group, IDirection<Tab, OrthTab> direction) {
    IDirection<Tab, OrthTab> oppositeDirection = direction.getOppositeDirection();
    Tab border1 = direction.getTab(data);
    Tab border2 = oppositeDirection.getTab(data);
    if (!contains(border1, group, direction) && !contains(border2, group, oppositeDirection))
      return false;
    return true;
  }

  static public <Tab extends Variable, OrthTab extends Variable>
  void fill(AlgebraData data, Tab gapTab, IDirection<Tab, OrthTab> normalDirection, IDirection<Tab, OrthTab> moveDirection) {
    List<List<Area>> groups = collectGroups(data, gapTab, normalDirection);

    for (List<Area> group : groups) {
      if (isConnectedToBorder1OrBorder2(data, group, normalDirection))
        continue;

      List<EmptySpace> gapCandidates = new ArrayList<EmptySpace>();
      for (Area area : group) {
        EmptySpace space = extendAsFarAsPossible(data, area, moveDirection);
        // also extend into the other direction
        extendAsFarAsPossible(data, area, moveDirection.getOppositeDirection());
        if (space != null)
          gapCandidates.add(space);
      }

      assert gapCandidates.size() >= 1;

      // find best candidate
      double minDistance = Double.MAX_VALUE;
      EmptySpace minSpace = null;
      for (EmptySpace space : gapCandidates) {
        double extend = moveDirection.getExtent(space);
        if (extend < minDistance) {
          minDistance = extend;
          minSpace = space;
        }
      }

      EmptyAreaCleaner.simplify(data, minSpace, normalDirection.getOrthogonalDirection2());

      data.removeArea(minSpace);
      // merge tabs
      data.mergeTabs(moveDirection.getTab(minSpace), gapTab, moveDirection);
    }
  }

  static public void fill(AlgebraData data, IArea area) {
    fill(data, area.getLeft(), area.getTop(), area.getRight(), area.getBottom());
  }

  static public void fill(AlgebraData data, XTab left, YTab top, XTab right, YTab bottom) {
    IDirection leftDirection = new LeftDirection();
    IDirection rightDirection = new RightDirection();
    IDirection topDirection = new TopDirection();
    IDirection bottomDirection = new BottomDirection();
    fill(data, left, leftDirection, rightDirection);
    fill(data, top, topDirection, bottomDirection);
    fill(data, right, rightDirection, leftDirection);
    fill(data, bottom, bottomDirection, topDirection);
  }

  static private <Tab extends Variable, OrthTab extends Variable>
  EmptySpace extendAsFarAsPossible(AlgebraData data, Area area, IDirection<Tab, OrthTab> direction) {
    EmptySpace space = TilingAlgebra.makeSpace(data, area, direction);
    if (space == null)
      return null;
    while (TilingAlgebra.extend(data, space, direction))
      continue;
    return space;
  }
}
