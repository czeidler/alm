/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.linsolve;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ForceSolver extends AbstractLinearSolver {
    private class Force {
        final public double k;
        final public double displacement;

        public Force(double k, double displacement) {
            this.k = k;
            this.displacement = displacement;
        }

        public double getForce() {
            return k * displacement;
        }

        public boolean isStrong() {
            return k < 0;
        }
    }

    private class ForceSum {
        final private List<Force> forces = new ArrayList<>();

        private boolean valid = false;
        private double averageDisplacement;
        private double effectiveK;
        private double averageForce;

        private boolean isStrong = false;

        public void addForce(Force force) {
            if (isStrong && !force.isStrong())
                return;
            if (!isStrong && force.isStrong()) {
                isStrong = true;
                forces.clear();
            }

            forces.add(force);
            valid = false;
        }

        public int size() {
            return forces.size();
        }

        private void validated() {
            if (valid)
                return;

            averageDisplacement = 0;
            averageForce = 0;
            effectiveK = 0;

            int i = 0;
            for (Force force : forces) {
                i++;
                averageDisplacement += force.displacement;
                averageForce += force.getForce();
            }

            averageDisplacement /= i;
            averageForce /= i;
            effectiveK = averageForce / averageDisplacement;

            valid = true;
        }

        public boolean isStrong() {
            return isStrong;
        }

        public double getAverageDisplacement() {
            validated();
            return averageDisplacement;
        }

        public double getEffectiveK() {
            validated();
            return effectiveK;
        }

        public double getAverageForce() {
            validated();
            return averageForce;
        }
    }

    private class VariableForce {
        final public ForceSum leftForce = new ForceSum();
        final public ForceSum rightForce = new ForceSum();

        public double getStrongDisplacement() {
            if (leftForce.size() > 0 && rightForce.size() > 0)
                return leftForce.getAverageDisplacement() + rightForce.getAverageDisplacement();
            if (leftForce.size() > 0)
                return leftForce.getAverageDisplacement();
            if (rightForce.size() > 0)
                return rightForce.getAverageDisplacement();
            return 0;
        }

        public double getEffectiveDisplacement() {
            if (leftForce.size() == 0 && rightForce.size() == 0)
                return 0;
            if (rightForce.size() == 0)
                return leftForce.getEffectiveK() * leftForce.getAverageDisplacement();
            if (leftForce.size() == 0)
                return rightForce.getEffectiveK() * rightForce.getAverageDisplacement();

            return (leftForce.getEffectiveK() * leftForce.getAverageDisplacement()
                    + rightForce.getEffectiveK() * rightForce.getAverageDisplacement())
                    / (leftForce.getEffectiveK() + rightForce.getEffectiveK());
        }
    }

    public static final int MAX_ITERATION = 1000;

    @Override
    protected ResultType doSolve() {
        initVariableValues();

        double cooling = 1.9d;
        double COOLING_FACTOR = 0.9;

        for (int i = 0; i < MAX_ITERATION; i++) {
            doIteration(cooling);
            cooling *= COOLING_FACTOR;
        }

        return ResultType.OPTIMAL;
    }

    private void doIteration(double cooling) {
        Map<Variable, VariableForce> variableForceMap = new HashMap<>();

        for (Constraint constraint : getLinearSpec().getConstraints()) {
            if (constraint.isSatisfied())
                continue;
            if (constraint.isHard())
                continue;

            // calculate projection parameter (b_i - A_i*x)/|A_i|^2
            double b = constraint.getRightSide();
            Summand[] A = constraint.getLeftSide();
            double p = (b - scalarProduct(A)) / euclidianNorm(A);

            for (Summand summand : A) {
                Variable variable = summand.getVar();
                VariableForce variableForce = variableForceMap.get(variable);
                if (variableForce == null) {
                    variableForce = new VariableForce();
                    variableForceMap.put(variable, variableForce);
                }

                double displacement = p * summand.getCoeff();
                Force force = new Force(getK(constraint.getPenalty()), displacement);
                if (displacement <= 0)
                    variableForce.leftForce.addForce(force);
                else
                    variableForce.rightForce.addForce(force);
            }
        }

        for (Map.Entry<Variable, VariableForce> entry : variableForceMap.entrySet()) {
            Variable variable = entry.getKey();
            VariableForce variableForce = entry.getValue();
            double delta;
            if (variableForce.leftForce.isStrong() || variableForce.rightForce.isStrong())
                delta = variableForce.getStrongDisplacement();
            else
                delta = cooling * variableForce.getEffectiveDisplacement();
            variable.setValue(variable.getValue() + delta);
        }

        for (Constraint constraint : getLinearSpec().getConstraints()) {
            if (constraint.isSatisfied())
                continue;
            if (!constraint.isHard())
                continue;

            // calculate projection parameter (b_i - A_i*x)/|A_i|^2
            double b = constraint.getRightSide();
            Summand[] A = constraint.getLeftSide();
            double p = (b - scalarProduct(A)) / euclidianNorm(A);

            for (Summand summand : A) {
                Variable variable = summand.getVar();
                variable.setValue(variable.getValue() + p * summand.getCoeff());
            }
        }
    }

    private double getK(double penalty) {
        if (penalty == 1)
            return -1;
        if (penalty > 0.8)
            return 1000;
        return penalty;
    }

    private double scalarProduct(Summand[] summands) {
        double ret = 0.0d;
        for (Summand s : summands) {
            ret += s.getCoeff() * s.getVar().getValue();
        }
        return ret;
    }

    private double euclidianNorm(Summand[] summands) {
        double ret = 0.0d;
        for (Summand s : summands) {
            ret += s.getCoeff() * s.getCoeff();
        }
        return ret;
    }
}
