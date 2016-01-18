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


public class ColumnOneToTwoTrafo implements ITransformation {
    @Override
    public List<Result> transform(Fragment fragment) {
        List<Result> results = new ArrayList<Result>();
        if (fragment.isHorizontalDirection())
            return results;
        // at least 3 items otherwise it's a swap
        if (fragment.getItems().size() < 3)
            return results;
        int splitPoint = fragment.getItems().size() / 2 + 1;
        Fragment column1 = Fragment.verticalFragment();
        Fragment column2 = Fragment.verticalFragment();
        Fragment hFragment = Fragment.horizontalFragment();
        hFragment.add(column1, false);
        hFragment.add(column2, false);
        List<IArea> items = fragment.getItems();
        for (int i = 0; i < splitPoint; i++)
            column1.add(items.get(i), false);
        for (int i = splitPoint; i < items.size(); i++)
            column2.add(items.get(i), false);

        results.add(new Result(1.1f, hFragment));
        return results;
    }
}
