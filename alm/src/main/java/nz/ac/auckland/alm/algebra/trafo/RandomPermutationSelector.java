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


public class RandomPermutationSelector<T> implements IPermutationSelector<T> {
    final List<ITransformation> trafos;

    public RandomPermutationSelector(List<ITransformation> trafos) {
        this.trafos = trafos;
    }

    @Override
    public List<List<ITransformation>> next(OngoingTrafo<T> current) {
        List<List<ITransformation>> out = new ArrayList<List<ITransformation>>();

        final int OUT_PERMUTATIONS_THRESHOLD = 5;

        int nPossiblePermutations = current.getNPermutations(trafos);
        while (current.getHandledPermutations().size() != nPossiblePermutations
                && out.size() < OUT_PERMUTATIONS_THRESHOLD) {
            int randomPermutation = (int)(Math.random() * nPossiblePermutations);
            for (int i = randomPermutation; i < randomPermutation + nPossiblePermutations; i++) {
                int index = i;
                if (index >= nPossiblePermutations)
                    index %= nPossiblePermutations;
                if (current.getHandledPermutations().contains(index))
                    continue;
                List<ITransformation> transformations = current.getTrafos(index, trafos);
                out.add(transformations);
                if (out.size() >= OUT_PERMUTATIONS_THRESHOLD)
                    break;
            }
        }
        return out;
    }

    @Override
    public boolean done(OngoingTrafo<T> state) {
        return state.getHandledPermutations().size() == state.getNPermutations(trafos);
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public IPermutationSelector create() {
        return new RandomPermutationSelector(trafos);
    }
}
