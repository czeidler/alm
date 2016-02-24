/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.linsolve;


public class ForceSolver extends AbstractLinearSolver {
    private class VariableForce {
        private int nForces = 0;
        private double forceSum = 0d;
        private double kSum = 0d;

        public void addForce(double k, double displacement) {
            forceSum += k * displacement;
            kSum += k;
            nForces ++;
        }

        public void reset() {
            nForces = 0;
            forceSum = 0d;
            kSum = 0d;
        }

        public double getZeroForceDisplacement() {
            if (nForces == 0)
                return 0;
            return forceSum / kSum;
        }
    }

    @Override
    protected ResultType doSolve() {
        initVariableValues();

        double cooling = 1.9d;
        final double COOLING_FACTOR = 1.d;
        final int MAX_ITERATION = 10000;

        for (Variable v : this.getLinearSpec().getVariables())
            v.setValue(0.0);

        // do an initial Kaczmarz
        /*doKaczmarzHard();
        for (int i = 0; i < MAX_ITERATION; i++) {
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
            boolean feasible = false;
            for (int a = 0; a < MAX_ITERATION; a++) {
                if (allHardConstraintsSatisfied()) {
                    feasible = true;
                    break;
                }
                doKaczmarzHard();
            }
            if (!feasible) {
                System.out.println("INFEASIBLE");
                return ResultType.INFEASIBLE;
            }

            cooling *= COOLING_FACTOR;

            // check the result
            double error2 = error2SoftConstraints();
            double diff = Math.abs(prevError2 - error2);
            prevError2 = error2;
            if (diff < Math.pow(tolerance, 2)) {
                //System.out.println("Iterations: " + (i + 1));
                return ResultType.OPTIMAL;
            }
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
            if (constraint.isHard())
                continue;
            if (constraint.isSatisfied())
                continue;

            double p = getKaczmarzProjection(constraint);
            for (Summand summand : constraint.getLeftSide()) {
                Variable variable = summand.getVar();
                VariableForce variableForce = getVariableForce(variable);

                double displacement = p * summand.getCoeff();
                variableForce.addForce(getK(constraint.getPenalty()), displacement);
            }
        }
        // Apply forces on the variables.
        for (Variable variable : getLinearSpec().getVariables()) {
            if (variable.solverCookie == null)
                continue;
            VariableForce variableForce = (VariableForce)variable.solverCookie;
            if (variableForce.nForces == 0)
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
