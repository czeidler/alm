/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.linsolve;


public class ForceSolver3 extends AbstractLinearSolver {
    private class ForceSum {
        private int nForces = 0;
        private double forceSum = 0d;
        private double kSum = 0d;

        public void addForce(double k, double displacement) {
            forceSum += k * displacement;
            kSum += k;
            nForces ++;
        }

        public int size() {
            return nForces;
        }

        public double getForceSum() {
            return forceSum;
        }

        public double getKSum() {
            return kSum;
        }

        public void reset() {
            nForces = 0;
            forceSum = 0d;
            kSum = 0d;
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

        public void reset() {
            leftForce.reset();
            rightForce.reset();
        }
    }

    @Override
    protected ResultType doSolve() {
        initVariableValues();

        double cooling = 1.95d;
        final double COOLING_FACTOR = 1.d;
        final int MAX_ITERATION = 10000;

        for (Variable v : this.getLinearSpec().getVariables())
            v.setValue(0.0);

        // do an initial Kaczmarz
        /*for (int i = 0; i < MAX_ITERATION; i++) {
            if (allHardConstraintsSatisfied()) {
                System.out.println("Init Iterations: " + (i + 1));
                break;
            }
            doKaczmarzHard();
        }*/

        final double tolerance = getLinearSpec().getTolerance();
        double prevError2 = Double.MAX_VALUE;
        for (int i = 0; i < MAX_ITERATION; i++) {
            // Optimize soft constraints.
            doOptimizeForcesSoft(cooling);
            // Fix hard constraints using Kaczmarz.
            doKaczmarzHard();

            cooling *= COOLING_FACTOR;

            // check the result
            double error2 = error2SoftConstraints();
            double diff = Math.abs(prevError2 - error2);
            prevError2 = error2;
            if (diff < Math.pow(tolerance, 2)) {
                //System.out.println("Iterations: " + (i + 1));
                if (allHardConstraintsSatisfied())
                    return ResultType.OPTIMAL;
            }
        }

        if (!allHardConstraintsSatisfied()) {
            System.out.println("INFEASIBLE");
            return ResultType.INFEASIBLE;
        }

        System.out.println("SUBOPTIMAL");
        return ResultType.SUBOPTIMAL;
    }

    @Override
    public void onSolveFinished() {
        getLinearSpec().cleanSolverCookies();
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

    private VariableForce getVariableForce(Variable variable) {
        if (variable.solverCookie == null)
            variable.solverCookie = new VariableForce();
        return (VariableForce)variable.solverCookie;
    }

    private void resetVariableForces() {
        for (Variable variable : getLinearSpec().getVariables()) {
            if (variable.solverCookie != null) {
                ((VariableForce)variable.solverCookie).reset();
            }
        }
    }

    private void doOptimizeForcesSoft(double cooling) {
        resetVariableForces();

        // Calculate forces on each variable. The force is proportional to the displacement of the variable. The
        // displacement is calculated using the Kaczmarz projection.
        for (Constraint constraint : getLinearSpec().getConstraints()) {
            //if (constraint.isSatisfied() && constraint.isHard() && constraint.getOp() != OperatorType.EQ)
              //  continue;
            if (constraint.isSatisfied())
                continue;
            if (constraint.isHard())
                continue;

            double p = getKaczmarzProjection(constraint);
            for (Summand summand : constraint.getLeftSide()) {
                Variable variable = summand.getVar();
                VariableForce variableForce = getVariableForce(variable);

                double displacement = p * summand.getCoeff();
                if (displacement <= 0)
                    variableForce.leftForce.addForce(getK(constraint.getPenalty()), displacement);
                else
                    variableForce.rightForce.addForce(getK(constraint.getPenalty()), displacement);
            }
        }
        // Apply forces on the variables.
        for (Variable variable : getLinearSpec().getVariables()) {
            if (variable.solverCookie == null)
                continue;
            VariableForce variableForce = (VariableForce)variable.solverCookie;
            if (variableForce.leftForce.forceSum == 0 && variableForce.rightForce.forceSum == 0)
                continue;

            double delta = cooling * variableForce.getZeroForceDisplacement();
            variable.setValue(variable.getValue() + delta);
        }
    }

    private void doKaczmarzHard() {
        for (Constraint constraint : getLinearSpec().getConstraints()) {
            if (!constraint.isHard() )
                continue;
            if (constraint.isSatisfied())
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
        if (penalty >= 1.d)
            return 100000000;
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
