/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm.algebra.trafo;

import nz.ac.auckland.alm.algebra.Fragment;

import java.util.List;


public interface ITransformation {
    class Result {
        final public float quality;
        final public Fragment fragment;

        public Result(float quality, Fragment fragment) {
            this.quality = quality;
            this.fragment = fragment;
        }
    }

    /**
     * Tries to transform a fragment and returns a list of possible transformations.
     * @param fragment the fragment to transform
     * @return list of possible transformations
     */
    List<Result> transform(Fragment fragment);
}
