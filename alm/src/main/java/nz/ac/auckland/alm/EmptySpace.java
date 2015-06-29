/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm;


import nz.ac.auckland.linsolve.Constraint;
import nz.ac.auckland.linsolve.OperatorType;

public class EmptySpace extends AbstractArea {
    private XTab left;
    private YTab top;
    private XTab right;
    private YTab bottom;

    Constraint minWidthConstraint;
    Constraint minHeightConstraint;

    public EmptySpace() {

    }

    public EmptySpace(XTab left, YTab top, XTab right, YTab bottom) {
        setLeftRight(left, right);
        setTopBottom(top, bottom);
    }

    @Override
    public IArea clone(XTab clonedLeft, YTab clonedTop, XTab clonedRight, YTab clonedBottom) {
        return new EmptySpace(clonedLeft, clonedTop, clonedRight, clonedBottom);
    }

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
        left = value;
        updateHorizontalConstraintVars();
    }

    @Override
    public void setRight(XTab value) {
        right = value;
        updateHorizontalConstraintVars();
    }

    @Override
    public void setTop(YTab value) {
        top = value;
        updateVerticalConstraintVars();
    }

    @Override
    public void setBottom(YTab value) {
        bottom = value;
        updateVerticalConstraintVars();
    }

    @Override
    public void setLeftRight(XTab left, XTab right) {
        this.left = left;
        this.right = right;
        updateHorizontalConstraintVars();
    }

    @Override
    public void setTopBottom(YTab top, YTab bottom) {
        this.top = top;
        this.bottom = bottom;
        updateVerticalConstraintVars();
    }

    private void updateHorizontalConstraintVars() {
        if (minWidthConstraint == null) {
            minWidthConstraint = new Constraint(-1, left, 1, right, OperatorType.GE, 0);
            minWidthConstraint.setName("minWidthConstraint");
            addConstraint(minWidthConstraint);
        }
        minWidthConstraint.setLeftSide(-1, left, 1, right);

        invalidateLayoutSpec();
    }

    private void updateVerticalConstraintVars() {
        if (minHeightConstraint == null) {
            minHeightConstraint = new Constraint(-1, top, 1, bottom, OperatorType.GE, 0);
            minHeightConstraint.setName("minHeightConstraint");
            addConstraint(minHeightConstraint);
        }
        // the respective minimum constraint needs to use the new tab
        minHeightConstraint.setLeftSide(-1, top, 1, bottom);

        invalidateLayoutSpec();
    }

    public String toString() {
        return "EmptySpace(" + left.toString() + "," + top.toString() + ","
                + right.toString() + "," + bottom.toString() + ")";
    }
}
