/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm;


public class TabArea implements IArea {
    // the boundaries (tabs) of the area
    XTab left;
    XTab right;
    YTab top;
    YTab bottom;

    @Override
    public XTab getLeft() {
        return left;
    }

    @Override
    public YTab getTop() {
        return top;
    }

    @Override
    public XTab getRight() {
        return right;
    }

    @Override
    public YTab getBottom() {
        return bottom;
    }

    @Override
    public void setLeft(XTab value) {
        this.left = value;
    }

    @Override
    public void setRight(XTab value) {
        this.right = value;
    }

    @Override
    public void setTop(YTab value) {
        this.top = value;
    }

    @Override
    public void setBottom(YTab value) {
        this.bottom = value;
    }

    @Override
    public void setLeftRight(XTab left, XTab right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public void setTopBottom(YTab top, YTab bottom) {
        this.top = top;
        this.bottom = bottom;
    }

    @Override
    public Area.Rect getRect() {
        return new Area.Rect((float)getLeft().getValue(), (float)getTop().getValue(), (float)getRight().getValue(),
                (float)getBottom().getValue());
    }
}
