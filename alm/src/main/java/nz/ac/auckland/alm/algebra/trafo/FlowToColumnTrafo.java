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
import java.util.Collections;
import java.util.List;


public class FlowToColumnTrafo implements ITransformation {
    final private IDirection inDirection;
    final private IDirection containerDirection;
    final private IDirection lineDirection;

    public FlowToColumnTrafo(IDirection inDirection, IDirection containerDirection, IDirection lineDirection) {
        this.inDirection = inDirection;
        this.containerDirection = containerDirection;
        this.lineDirection = lineDirection;
    }

    private void addAll(Fragment target, Fragment source) {
        for (int i = 0; i < source.size(); i++)
            target.add(source.getItemAt(i), false);
    }

    @Override
    public List<Result> transform(Fragment fragment) {
        List<Result> results = new ArrayList<Result>();
        if (fragment.getDirection() != inDirection)
            return results;
        if (fragment.size() < 2)
            return results;


        int itemsPerLine = -1;
        Fragment outContainer = Fragment.createEmptyFragment(containerDirection);
        for (int i = 0; i < fragment.size(); i++) {
            if (!(fragment.getItemAt(i) instanceof Fragment))
                return Collections.emptyList();
            Fragment line = (Fragment)fragment.getItemAt(i);
            if (line.getDirection() != lineDirection)
                return Collections.emptyList();
            if (itemsPerLine < 0) {
                itemsPerLine = line.size();
                if (itemsPerLine < 2)
                    return Collections.emptyList();
            } else if (i < fragment.size() - 1 && itemsPerLine != line.size()) {
                return Collections.emptyList();
            } else if (i == fragment.size() - 1 && itemsPerLine < line.size()) {
                return Collections.emptyList();
            }

            addAll(outContainer, line);
        }
        results.add(new Result(1.f, outContainer));
        return results;
    }
}
