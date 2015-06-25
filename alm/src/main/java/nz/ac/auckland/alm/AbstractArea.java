package nz.ac.auckland.alm;


import nz.ac.auckland.linsolve.Constraint;

import java.util.ArrayList;
import java.util.List;

abstract class AbstractArea implements IArea {
    /**
     * The layout specification this area belongs to.
     */
    LayoutSpec layoutSpec;

    // A list of constraints which are removed form the specification when the area is removed.
    final List<Constraint> constraints = new ArrayList<Constraint>();

    protected void invalidateLayoutSpec() {
        if (layoutSpec != null)
            layoutSpec.invalidateLayout();
    }

    protected void addConstraint(Constraint constraint) {
        constraints.add(constraint);
        if (layoutSpec != null)
            layoutSpec.linearSpec.addConstraint(constraint);
    }

    @Override
    public void attachedToLayoutSpec(LayoutSpec layoutSpec) {
        if (this.layoutSpec != null)
            throw new RuntimeException("Area can't be attached to LinearSpec twice.");
        this.layoutSpec = layoutSpec;

        for (Constraint constraint : constraints)
            layoutSpec.linearSpec.addConstraint(constraint);
    }

    @Override
    public void detachedFromLinearSpec(LayoutSpec layoutSpec) {
        if (this.layoutSpec != layoutSpec)
            throw new RuntimeException("Area is attached to different LayoutSpec!");
        for (Constraint c : constraints)
            c.remove();
        this.layoutSpec = null;
    }
}
