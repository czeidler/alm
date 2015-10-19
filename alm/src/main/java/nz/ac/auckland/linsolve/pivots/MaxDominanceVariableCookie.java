/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.linsolve.pivots;

import nz.ac.auckland.linsolve.Constraint;
import nz.ac.auckland.linsolve.Summand;


public class MaxDominanceVariableCookie {
    protected nz.ac.auckland.linsolve.Summand summandWhereMaxDominant;
    protected double maxDominance = 0;
    protected Constraint constraintWhereMaxDominant;

    public Summand getSummandWhereMaxDominant() {
        return summandWhereMaxDominant;
    }

    public void setSummandWhereMaxDominant(Summand summandWhereMaxDominant) {
        this.summandWhereMaxDominant = summandWhereMaxDominant;
    }

    public double getMaxDominance() {
        return maxDominance;
    }

    public void setMaxDominance(double maxDominance) {
        this.maxDominance = maxDominance;
    }

    public Constraint getConstraintWhereMaxDominant() {
        return constraintWhereMaxDominant;
    }

    public void setConstraintWhereMaxDominant(Constraint constraintWhereMaxDominant) {
        this.constraintWhereMaxDominant = constraintWhereMaxDominant;
    }
}
