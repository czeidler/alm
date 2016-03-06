/*
 * Copyright 2016.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm.algebra.trafo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ChainPermutationSelector<T> implements IPermutationSelector<T> {
    final private List<IPermutationSelector<T>> selectors;
    int currentSelector = 0;

    public ChainPermutationSelector(IPermutationSelector<T>... selectors) {
        this.selectors = Arrays.asList(selectors);
    }

    private ChainPermutationSelector(List<IPermutationSelector<T>> selectors) {
        this.selectors = selectors;
    }

    @Override
    public List<List<ITransformation>> next(OngoingTrafo<T> state) {
        IPermutationSelector<T> current = selectors.get(currentSelector);
        List<List<ITransformation>> permutations = current.next(state);
        if (current.done(state))
            currentSelector++;
        return permutations;
    }

    @Override
    public boolean done(OngoingTrafo<T> state) {
        if (currentSelector >= selectors.size())
            return true;
        return selectors.get(currentSelector).done(state);
    }

    @Override
    public int getPriority() {
        if (currentSelector >= selectors.size())
            return -1;
        return selectors.get(currentSelector).getPriority();
    }

    @Override
    public IPermutationSelector create() {
        List<IPermutationSelector<T>> clone
                = new ArrayList<IPermutationSelector<T>>();
        for (IPermutationSelector<T> selector : selectors)
            clone.add(selector.create());
        return new ChainPermutationSelector(clone);
    }
}
