/*
 * Copyright 2016.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm.algebra.trafo;


import nz.ac.auckland.alm.algebra.Fragment;
import nz.ac.auckland.alm.algebra.IDirection;

import java.util.ArrayList;
import java.util.List;

public class ColumnTrafo implements ITransformation {
    final private IDirection inDirection = Fragment.verticalDirection;
    final private IDirection containerDirection = Fragment.horizontalDirection;
    final private IDirection lineDirection = Fragment.verticalDirection;

    private void split(Fragment fragment, int breakPoint, List<Result> results) {
        Fragment container = Fragment.createEmptyFragment(containerDirection);

        // column1
        Fragment current = Fragment.createEmptyFragment(lineDirection);
        for (int i = 0; i < breakPoint; i++)
            current.add(fragment.getItemAt(i), false);
        if (current.size() == 1)
            container.add(current.getItemAt(0), false);
        else
            container.add(current, false);
        // column2
        current = Fragment.createEmptyFragment(lineDirection);
        for (int i = breakPoint; i < fragment.size(); i++)
            current.add(fragment.getItemAt(i), false);
        if (current.size() == 1)
            container.add(current.getItemAt(0), false);
        else
            container.add(current, false);

        results.add(new Result(1.1f, container));
    }

    @Override
    public List<Result> transform(Fragment fragment) {
        List<Result> results = new ArrayList<Result>();
        if (fragment.getDirection() != inDirection)
            return results;
        // at least 3 items otherwise it's a swap
        if (fragment.size() < 3)
            return results;

        final int NUMBER_BREAK_POINTS = 3;
        int start = fragment.size() / 2 - NUMBER_BREAK_POINTS / 2;
        if (start < 1)
            start = 1;
        int end = start + NUMBER_BREAK_POINTS;
        if (end >= fragment.size())
            end = fragment.size() - 1;
        for (int i = start; i <= end; i++)
            split(fragment, i, results);

        return results;
    }
}

