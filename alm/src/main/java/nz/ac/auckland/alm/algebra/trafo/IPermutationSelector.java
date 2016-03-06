/*
 * Copyright 2016.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm.algebra.trafo;

import java.util.List;


public interface IPermutationSelector<T> {
    List<List<ITransformation>> next(OngoingTrafo<T> state);
    boolean done(OngoingTrafo<T> state);
    int getPriority();
    IPermutationSelector create();
}
