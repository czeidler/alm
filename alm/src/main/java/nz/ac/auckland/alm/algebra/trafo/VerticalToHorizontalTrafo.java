/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm.algebra.trafo;

import nz.ac.auckland.alm.algebra.Fragment;

import java.util.Collections;
import java.util.List;


public class VerticalToHorizontalTrafo implements ITransformation {
    @Override
    public List<Result> transform(Fragment fragment) {
        if (fragment.size() <= 1)
            return Collections.emptyList();
        if (fragment.isHorizontalDirection())
            return Collections.emptyList();

        Fragment trafo = fragment.clone();
        trafo.setHorizontalDirection();

        return Collections.singletonList(new Result(1.1f, trafo));
    }
}
