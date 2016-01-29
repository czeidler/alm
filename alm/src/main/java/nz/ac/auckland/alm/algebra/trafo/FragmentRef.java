/*
 * Copyright 2015.
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
    // current position within the last level
    protected int currentPosition = 0;

    public int getCurrentPosition() {
        return currentPosition;
    }

    public List<Integer> getLevelPositions() {
        return fragLevelPosition;
    }

    public FragmentRef clone() {
        FragmentRef clone = new FragmentRef();
        clone.currentPosition = currentPosition;
        clone.fragLevelPosition.addAll(fragLevelPosition);
        return clone;
    }

    @Override
    public String toString() {
        String path = "/";
        for (Integer level : fragLevelPosition)
            path += level + "/";
        path += currentPosition;
        return path;
    }
}
