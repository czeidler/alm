/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm.algebra.trafo;

import nz.ac.auckland.alm.IArea;
import nz.ac.auckland.alm.algebra.Fragment;

import java.util.Comparator;


public class AreaComparator implements Comparator<IArea> {
    private String fragmentId(Fragment fragment) {
        String id = "";
        if (fragment.isHorizontalDirection())
            id += "h";
        else
            id += "v";
        for (IArea item : (Iterable<IArea>)fragment.getItems()) {
            if (item instanceof Fragment)
                id += fragmentId((Fragment)item);
            else
                id += item.getId();
        }
        return id;
    }

    @Override
    public int compare(IArea area0, IArea area1) {
        String area0Id;
        String area1Id;
        if (area0 instanceof Fragment)
            area0Id = fragmentId((Fragment)area0);
        else
            area0Id = area0.getId();

        if (area1 instanceof Fragment)
            area1Id = fragmentId((Fragment)area1);
        else
            area1Id = area1.getId();

        if (area0Id.equals(area1Id))
            return 0;
        return -1;
    }
}
