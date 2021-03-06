/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm.algebra.trafo;

import nz.ac.auckland.alm.algebra.Fragment;

import java.util.Comparator;
import java.util.List;


public interface IAlternativeClassifier<T> {
    double INVALID_OBJECTIVE = 10000000000d;
    T coarseClassify(Fragment fragment, TrafoHistory trafoHistory);
    boolean fineClassify(Fragment fragment, T classification);
    double objectiveValue(T classification);
}
