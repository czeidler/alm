/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm;


public interface IArea {
    XTab getLeft();
    YTab getTop();
    XTab getRight();
    YTab getBottom();

    void setLeft(XTab value);
    void setRight(XTab value);
    void setTop(YTab value);
    void setBottom(YTab value);
    void setLeftRight(XTab left, XTab right);
    void setTopBottom(YTab top, YTab bottom);

    Area.Rect getRect();
}
