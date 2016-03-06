/*
 * Copyright 2016.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm.algebra.trafo;

import java.util.ArrayList;
import java.util.List;


public class FragmentRef {
    // list of indices at what position the next level has been entered
    final protected List<Integer> fragLevelPosition = new ArrayList<Integer>();

    public List<Integer> getLevelPositions() {
        return fragLevelPosition;
    }

    public FragmentRef clone() {
        FragmentRef clone = new FragmentRef();
        clone.fragLevelPosition.addAll(fragLevelPosition);
        return clone;
    }

    @Override
    public String toString() {
        if (fragLevelPosition.size() == 0)
            return "/";
        String path = "";
        for (Integer level : fragLevelPosition)
            path += "/" + level;
        return path;
    }

    /**
     * Make the ref point to the fragment at index.
     *
     * @param index the index of the fragment the FragmentRef should point to
     */
    public void enterFragment(int index) {
        fragLevelPosition.add(index);
    }
}
