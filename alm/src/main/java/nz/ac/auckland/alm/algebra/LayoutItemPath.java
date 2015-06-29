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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Definition (Layout Item Path). A layout item path p is a set of layout items that fulfills the following properties.
 * Two layout items A and B belong to the same path p if there is one of the following fragments in the specification:
 * (A|B)/C or C/(A|B) or (A/B)|C or C|(A/B).
 * Transitivity: if A, B are on a path p and B, C are on the same path p, then also A, C are on path p.
 */
public class LayoutItemPath {
    static private <Tab extends Variable> void collect(Area seed, IDirection direction, Map<Tab, Edge> map,
                                                       List<Area> out, List<Area> seeds) {
        Edge edge = direction.getEdge(seed, map);
        Tab orthTab1 = (Tab)direction.getOrthogonalTab1(seed);
        Tab orthTab2 = (Tab)direction.getOrthogonalTab2(seed);
        for (IArea area : direction.getOppositeAreas(edge)) {
            if (area == seed)
                continue;
            if (!(area instanceof Area))
                continue;
            if (direction.getOrthogonalTab2(area) == orthTab1 || direction.getOrthogonalTab1(area) == orthTab2) {
                if (!out.contains(area)) {
                    out.add((Area) area);
                    if (!seeds.contains(area))
                        seeds.add((Area) area);
                }
            }
        }
    }

    static public List<Area> detect(Area seed, Map<XTab, Edge> xTabEdgeMap, Map<YTab, Edge> yTabEdgeMap) {
        List<Area> out = new ArrayList<Area>();
        out.add(seed);
        List<Area> seeds = new ArrayList<Area>();
        seeds.add(seed);

        while (seeds.size() > 0) {
            Area current = seeds.remove(0);
            collect(current, new LeftDirection(), xTabEdgeMap, out, seeds);
            collect(current, new RightDirection(), xTabEdgeMap, out, seeds);
            collect(current, new TopDirection(), yTabEdgeMap, out, seeds);
            collect(current, new BottomDirection(), yTabEdgeMap, out, seeds);
        }

        return out;
    }
}
