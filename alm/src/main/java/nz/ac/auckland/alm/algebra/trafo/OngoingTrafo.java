/*
 * Copyright 2016.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm.algebra.trafo;

import nz.ac.auckland.alm.algebra.Fragment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Tree structure of all transformations.
 *
 * @param <T>
 */
public class OngoingTrafo<T> {
    final private TrafoHistory trafoHistory;
    final private Fragment alternative;
    final private T classification;
    // ref within the alternative
    final private List<FragmentRef> fragmentRefs;
    final private IPermutationSelector selector;
    final private Set<Integer> handledPermutations = new HashSet<Integer>();

    static class PermutationEncoding {
        /**
         * For example, if there are 10 trafo values, the array [2,3,5,1] will be encoded to the number 2351
         *
         * @param trafoValues
         * @param nTrafoValues the number of possible trafo values
         * @return
         */
        static int toPermutation(int[] trafoValues, int nTrafoValues) {
            int out = 0;
            for (int i = 0; i < trafoValues.length; i++) {
                int index = trafoValues[i];
                out += index * Math.pow(nTrafoValues, (trafoValues.length - 1 - i));
            }
            return out;
        }

        /**
         * Extract n entries from the permutation.
         *
         * @param permutation
         * @param nEntries
         * @param nTrafoValues the number of possible trafo values
         * @return
         */
        static int[] fromPermutation(int permutation, int nEntries, int nTrafoValues) {
            int[] trafoValues = new int[nEntries];
            for (int i = 0; i < nEntries - 1; i++) {
                int positionValue = (int)Math.pow(nTrafoValues, (nEntries - 1 - i));
                trafoValues[i] = permutation / positionValue;
                permutation -= positionValue * trafoValues[i];
            }
            trafoValues[nEntries - 1] = permutation;
            return trafoValues;
        }
    }

    public OngoingTrafo(TrafoHistory trafoHistory, Fragment alternative, T classification,
                        List<FragmentRef> fragmentRefs, IPermutationSelector selector) {
        this.trafoHistory = trafoHistory;
        this.alternative = alternative;
        this.classification = classification;
        this.fragmentRefs = fragmentRefs;
        this.selector = selector;
    }

    public TrafoHistory getTrafoHistory() {
        return trafoHistory;
    }

    public Fragment getAlternative() {
        return alternative;
    }

    public IPermutationSelector getSelector() {
        return selector;
    }

    public List<FragmentRef> getFragmentRefs() {
        return fragmentRefs;
    }

    public T getClassification() {
        return classification;
    }

    public Set<Integer> getHandledPermutations() {
        return handledPermutations;
    }

    public boolean done() {
        return selector.done(this);
    }

    public int getNPermutations(List<ITransformation> trafos) {
        return (int)Math.pow(trafos.size() + 1, fragmentRefs.size());
    }

    /**
     * Gets an ITransformation for each fragment ref.
     *
     * @param permutation
     * @param trafos
     * @return
     */
    public List<ITransformation> getTrafos(int permutation, List<ITransformation> trafos) {
        int[] trafoValues = PermutationEncoding.fromPermutation(permutation, fragmentRefs.size(),
                trafos.size() + 1);
        List<ITransformation> transformations = new ArrayList<ITransformation>();
        for (int a = 0; a < trafoValues.length; a++) {
            if (trafoValues[a] == 0)
                transformations.add(null);
            else
                transformations.add(trafos.get(trafoValues[a] - 1));
        }
        return transformations;
    }

    static public int getPermutationValue(List<ITransformation> transformations, List<ITransformation> trafos) {
        int[] indices = new int[transformations.size()];
        for (int i = 0 ; i < transformations.size(); i++) {
            ITransformation transformation = transformations.get(i);
            if (transformation == null)
                indices[i] = 0;
            else
                indices[i] = trafos.indexOf(transformation) + 1;
        }
        return PermutationEncoding.toPermutation(indices, trafos.size() + 1);
    }
}
