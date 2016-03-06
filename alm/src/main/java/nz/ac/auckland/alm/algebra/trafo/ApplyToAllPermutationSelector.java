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


public class ApplyToAllPermutationSelector<T> implements IPermutationSelector<T> {
    final private List<ITransformation> trafos;
    final private ITransformation transformation;

    private int progress = 0;

    public ApplyToAllPermutationSelector(List<ITransformation> trafos, ITransformation transformation) {
        this.trafos = trafos;
        this.transformation = transformation;
    }

    @Override
    public List<List<ITransformation>> next(OngoingTrafo<T> state) {
        List<ITransformation> transformations = new ArrayList<ITransformation>();

        // first return the unmodified version
        if (progress == 0) {
            for (FragmentRef ref : state.getFragmentRefs())
                transformations.add(null);
        } else {
            for (FragmentRef ref : state.getFragmentRefs())
                transformations.add(transformation);
        }

        int permutation = OngoingTrafo.getPermutationValue(transformations, trafos);
        List<List<ITransformation>> out = new ArrayList<List<ITransformation>>();
        if (state.getHandledPermutations().contains(permutation))
            return out;
        out.add(transformations);
        progress ++;
        return out;
    }

    @Override
    public boolean done(OngoingTrafo<T> state) {
        return progress >= 2;
    }

    @Override
    public int getPriority() {
        return 9;
    }

    @Override
    public IPermutationSelector create() {
        return new ApplyToAllPermutationSelector(trafos, transformation);
    }
}
