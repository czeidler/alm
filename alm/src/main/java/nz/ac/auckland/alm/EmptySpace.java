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

public class EmptySpace extends AbstractLayoutSpecArea {
    Constraint minWidthConstraint;
    Constraint minHeightConstraint;

    public EmptySpace() {

    }

    public EmptySpace(XTab left, YTab top, XTab right, YTab bottom) {
        setLeft(left);
        setRight(right);
        setTop(top);
        setBottom(bottom);
    }

    @Override
    public ILayoutSpecArea clone(XTab clonedLeft, YTab clonedTop, XTab clonedRight, YTab clonedBottom) {
        EmptySpace clone =  new EmptySpace(clonedLeft, clonedTop, clonedRight, clonedBottom);
        clone.setId(getId());
        clone.setCookie(getCookie());
        return clone;
    }

    @Override
    public void setLeft(XTab value) {
        super.setLeft(value);
        updateHorizontalConstraintVars();
    }

    @Override
    public void setRight(XTab value) {
        super.setRight(value);
        updateHorizontalConstraintVars();
    }

    @Override
    public void setTop(YTab value) {
        super.setTop(value);
        updateVerticalConstraintVars();
    }

    @Override
    public void setBottom(YTab value) {
        super.setBottom(value);
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
