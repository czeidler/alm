/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm;


public interface ILayoutSpecArea extends IArea {
    ILayoutSpecArea clone(XTab clonedLeft, YTab clonedTop, XTab clonedRight, YTab clonedBottom);

    void attachedToLayoutSpec(LayoutSpec layoutSpec);
    void detachedFromLinearSpec(LayoutSpec layoutSpec);
}
