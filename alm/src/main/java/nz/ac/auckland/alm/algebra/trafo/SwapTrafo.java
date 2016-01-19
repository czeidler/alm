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

import java.util.ArrayList;
import java.util.List;


public class SwapTrafo implements ITransformation {
    @Override
    public List<Result> transform(Fragment fragment) {
        List<Result> results = new ArrayList<Result>();
        Fragment trafo;
        if (fragment.isHorizontalDirection())
            trafo = Fragment.verticalFragment();
        else
            trafo = Fragment.horizontalFragment();

        for (IArea area : (Iterable<IArea>)fragment.getItems())
            trafo.add(area, false);

        results.add(new Result(1.1f, trafo));
        return results;
    }
}
