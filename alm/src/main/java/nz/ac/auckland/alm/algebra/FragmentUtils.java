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
import nz.ac.auckland.alm.ILayoutSpecArea;
import nz.ac.auckland.alm.LayoutSpec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class FragmentUtils {
    static public List<Area> getAreas(Fragment fragment) {
        List<Area> areas = new ArrayList<Area>();
        getAreas(fragment, areas);
        return areas;
    }

    static public void getAreas(Fragment fragment, List<Area> areas) {
        for (IArea area : (Iterable<IArea>)fragment.getItems()) {
            if (area instanceof Fragment)
                getAreas((Fragment)area, areas);
            else if (area instanceof Area && !areas.contains(area))
                areas.add((Area)area);
        }
    }

    static public int countLevels(Fragment fragment) {
        int nLevels = 0;
        for (IArea area : (Iterable<IArea>)fragment.getItems()) {
            if (!(area instanceof Fragment))
                continue;
            nLevels = Math.max(nLevels, countLevels((Fragment)area) + 1);
        }
        return nLevels;
    }

    static public boolean childrenAreFragments(Fragment fragment) {
        for (IArea area : (Iterable<IArea>) fragment.getItems()) {
            if (!(area instanceof Fragment))
                return false;
        }
        return true;
    }

    static public List<Fragment> nextLevel(List<Fragment> currentLevel) {
        List<Fragment> level = new ArrayList<Fragment>();
        for (Fragment fragment : currentLevel) {
            for (IArea area : (Iterable<IArea>) fragment.getItems()) {
                if (!(area instanceof Fragment))
                    continue;
                level.add((Fragment)area);
            }
        }
        return level;
    }

    static public int countAreas(Fragment fragment) {
        int areas = 0;
        for (IArea area : (Iterable<IArea>)fragment.getItems()) {
            if (area instanceof Fragment)
                areas += countAreas((Fragment)area);
            else
                areas++;
        }
        return areas;
    }

    static public LayoutSpec toLayoutSpec(Fragment fragment) {
        fragment.applySpecsToChild();

        LayoutSpec layoutSpec = new LayoutSpec();
        fragment.setLeft(layoutSpec.getLeft());
        fragment.setTop(layoutSpec.getTop());
        fragment.setRight(layoutSpec.getRight());
        fragment.setBottom(layoutSpec.getBottom());

        List<Area> areas = getAreas(fragment);
        for (ILayoutSpecArea area : areas)
            layoutSpec.addArea(area);

        return layoutSpec;
    }
}
