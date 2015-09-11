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
    }

    private class ForceSum {
        final private List<Force> forces = new ArrayList<>();

        private boolean valid = false;
        private double forceSum;
        private double kSum;

        public void addForce(Force force) {
            forces.add(force);
            valid = false;
        }

        public int size() {
            return forces.size();
        }

        private void validated() {
            if (valid)
                return;

            forceSum = 0;
            kSum = 0;

            for (Force force : forces) {
                forceSum += force.getForce();
                kSum += force.k;
            }

            valid = true;
        }

        public double getForceSum() {
            validated();
            return forceSum;
        }

        public double getKSum() {
            validated();
            return kSum;
        }
    }

    private class VariableForce {
        final public ForceSum leftForce = new ForceSum();
        final public ForceSum rightForce = new ForceSum();

        public double getZeroForceDisplacement() {
            if (leftForce.size() == 0 && rightForce.size() == 0)
                return 0;
            double leftDelta = 0;
            double rightDelta = 0;
            if (leftForce.size() != 0)
                leftDelta = leftForce.getForceSum() / leftForce.getKSum();
            if (rightForce.size() != 0)
                rightDelta = rightForce.getForceSum() / rightForce.getKSum();

            if (rightForce.size() == 0)
                return leftDelta;
            if (leftForce.size() == 0)
                return rightDelta;

            return (leftForce.getKSum() * leftDelta + rightForce.getKSum() * rightDelta)
                    / (leftForce.getKSum() + rightForce.getKSum());
        }
    }

    @Override
    protected ResultType doSolve() {
        initVariableValues();

        double cooling = 1.9d;
        final double COOLING_FACTOR = 0.9;
        final int MAX_ITERATION = 1000;

        // do an initial Kaczmarz
        doKaczmarzHard();

        final double tolerance = 0.1;
        double prevError = Double.MAX_VALUE;
        for (int i = 0; i < MAX_ITERATION; i++) {
            // Optimize soft constraints.
            doOptimizeForcesSoft(cooling);
            cooling *= COOLING_FACTOR;
            // Fix hard constraints using Kaczmarz.
            doKaczmarzHard();

            // check the result
            double error2 = error2SoftConstraints();
            double diff = Math.abs(prevError - error2);
            prevError = error2;
            if (diff < tolerance * tolerance) {
                System.out.println("Iterations: " + (i + 1));
                if (allHardConstraintsSatisfied())
                    return ResultType.OPTIMAL;
            }
        }

        if (!allHardConstraintsSatisfied())
            return ResultType.INFEASIBLE;

        return ResultType.SUBOPTIMAL;
    }

    private boolean allHardConstraintsSatisfied() {
        for (Constraint constraint : this.getLinearSpec().getConstraints()) {
            if (!constraint.isHard())
                continue;
            if (!constraint.isSatisfied())
                return false;
        }
        return true;
    }

    private double error2SoftConstraints() {
        double error2 = 0;
        for (Constraint constraint : this.getLinearSpec().getConstraints()) {
            if (constraint.isHard())
                continue;
            error2 += Math.pow(constraint.error(), 2);
        }
        return error2;
    }

    private void doOptimizeForcesSoft(double cooling) {
        Map<Variable, VariableForce> variableForceMap = new HashMap<>();

        // Calculate forces on each variable. The force is proportional to the displacement of the variable. The
        // displacement is calculated using the Kaczmarz projection.
        for (Constraint constraint : getLinearSpec().getConstraints()) {
            if (constraint.isSatisfied())
                continue;
            if (constraint.isHard())
                continue;

            double p = getKaczmarzProjection(constraint);
            for (Summand summand : constraint.getLeftSide()) {
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
        // Apply forces on the variables.
        for (Map.Entry<Variable, VariableForce> entry : variableForceMap.entrySet()) {
            Variable variable = entry.getKey();
            VariableForce variableForce = entry.getValue();
            double delta = cooling * variableForce.getZeroForceDisplacement();
            variable.setValue(variable.getValue() + delta);
        }
    }

    private void doKaczmarzHard() {
        for (Constraint constraint : getLinearSpec().getConstraints()) {
            if (constraint.isSatisfied())
                continue;
            if (!constraint.isHard())
                continue;

            double p = getKaczmarzProjection(constraint);
            for (Summand summand : constraint.getLeftSide()) {
                Variable variable = summand.getVar();
                variable.setValue(variable.getValue() + p * summand.getCoeff());
            }
        }
    }

    private double getKaczmarzProjection(Constraint constraint) {
        // calculate projection parameter (b_i - A_i*x)/|A_i|^2
        double b = constraint.getRightSide();
        Summand[] A = constraint.getLeftSide();
        return (b - scalarProduct(A)) / euclidianNorm(A);
    }

    /**
     * Translates a penalty value to a force value.
     *
     * @param penalty of an constraint
     * @return associated force value of the penalty
     */
    private double getK(double penalty) {
        // TODO: use a better translation
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
