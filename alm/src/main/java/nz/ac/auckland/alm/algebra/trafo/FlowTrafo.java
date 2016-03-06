/*
 * Copyright 2015.
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


public class FlowTrafo implements ITransformation {
    final private IDirection direction;

    public FlowTrafo(IDirection direction) {
        this.direction = direction;
    }

    private void split(Fragment fragment, int elementsPerLine, List<Result> results) {
        Fragment current = null;
        Fragment container;
        if (fragment.isHorizontalDirection())
            container = Fragment.verticalFragment();
        else
            container = Fragment.horizontalFragment();

        for (int i = 0; i < fragment.size(); i++) {
            if (i == 0 || i % elementsPerLine == 0) {
                if (fragment.isHorizontalDirection())
                    current = Fragment.horizontalFragment();
                else
                    current = Fragment.verticalFragment();
            }
            current.add(fragment.getItemAt(i), false);
            if ((i + 1) % elementsPerLine == 0 || i + 1 == fragment.size()) {
                if (current.size() == 1)
                    container.add(current.getItemAt(0), false);
                else
                    container.add(current, false);
            }
        }
        results.add(new Result(1.1f, container));
    }

    @Override
    public List<Result> transform(Fragment fragment) {
        List<Result> results = new ArrayList<Result>();
        if (fragment.getDirection() != direction)
            return results;
        // at least 3 items otherwise it's a swap
        if (fragment.size() < 3)
            return results;

        for (int i = 2; i <= (fragment.size() + 1) / 2; i++)
            split(fragment, i, results);

        return results;
    }
}
