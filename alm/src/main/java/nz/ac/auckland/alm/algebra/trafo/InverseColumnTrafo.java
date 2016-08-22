/*
 * Copyright 2016.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm.algebra.trafo;

import nz.ac.auckland.alm.algebra.Fragment;


public class InverseColumnTrafo extends FlowToColumnTrafo {
    public InverseColumnTrafo() {
        super(Fragment.horizontalDirection, Fragment.verticalDirection, Fragment.verticalDirection);
    }
}
