/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.linsolve;


import java.util.ArrayList;
import java.util.List;

public class ForceSolverSoftSlack extends AbstractLinearSolver {
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

    private List<Constraint> prepareConstraints(List<Variable> variablesOut) {
        variablesOut.addAll(getLinearSpec().getVariables());

        List<Constraint> out = new ArrayList<Constraint>();
        for (Constraint constraint : getLinearSpec().getConstraints()) {
            if (!constraint.isHard() && constraint.getOp() != OperatorType.EQ) {
                // create soft inequality constraint out of hard constraints and a soft eq constraint
                Variable slackVariable = new Variable();
                slackVariable.setValue(0.d);
                variablesOut.add(slackVariable);
                out.add(new Constraint(1, slackVariable, OperatorType.GE, 0));
                out.add(new Constraint(1, slackVariable, OperatorType.EQ, 0, constraint.getPenalty()));
                int slackCoeff;
                if (constraint.getOp() == OperatorType.LE)
                    slackCoeff = -1;
                else
                    slackCoeff = 1;
                Summand[] summands = new Summand[constraint.getLeftSide().length + 1];
                for (int i = 0; i < constraint.getLeftSide().length; i++) {
                    Summand summand = constraint.getLeftSide()[i];
                    summands[i] = summand;
                }
                summands[constraint.getLeftSide().length] = new Summand(slackCoeff, slackVariable);
                // hard constraint
                Constraint newConstraint = new Constraint(summands, constraint.getOp(), constraint.getRightSide(),
                        Constraint.AMOST_HARD_PENALTY);
                out.add(newConstraint);
            } else
                out.add(constraint);
        }
        return out;
    }

    @Override
    protected ResultType doSolve() {
        initVariableValues();
        List<Variable> variables = new ArrayList<Variable>();
        List<Constraint> constraints = prepareConstraints(variables);

        double cooling = 1.9d;
        final double COOLING_FACTOR = 1.d;
        final int MAX_ITERATION = 1000;

        for (Variable v : this.getLinearSpec().getVariables())
            v.setValue(0.0);

        // do an initial Kaczmarz
        //doKaczmarzHard();
        for (int i = 0; i < MAX_ITERATION; i++) {
            if (allHardConstraintsSatisfied()) {
                //System.out.println("Init Iterations: " + (i + 1));
                break;
            }
            doKaczmarzHard(constraints);
        }

        final double tolerance = getLinearSpec().getTolerance();
        double prevError2 = Double.MAX_VALUE;
        for (int i = 0; i < MAX_ITERATION; i++) {
            // Optimize soft constraints.
            doOptimizeForcesSoft(constraints, variables, cooling);
            // Fix hard constraints using Kaczmarz.
            //doKaczmarzHard();

            boolean feasible = false;
            for (int a = 0; a < MAX_ITERATION; a++) {
                if (allHardConstraintsSatisfied()) {
                    feasible = true;
                    break;
                }
                doKaczmarzHard(constraints);
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

    private void resetVariableForces(List<Variable> variables) {
        for (Variable variable : variables) {
            if (variable.solverCookie != null) {
                ((VariableForce)variable.solverCookie).reset();
            }
        }
    }

    private boolean isSoftSatisfied(Constraint constraint) {
        if (constraint.getOp() == OperatorType.EQ)
            return false;

        double leftSideSum = 0;
        for (Summand summand : constraint.getLeftSide())
            leftSideSum += summand.coeff * summand.var.getValue();
        if (constraint.getOp() == OperatorType.GE && leftSideSum >= constraint.getRightSide())
            return true;
        if (constraint.getOp() == OperatorType.LE && leftSideSum <= constraint.getRightSide())
            return true;
        return false;
    }

    private void doOptimizeForcesSoft(List<Constraint> constraints, List<Variable> variables, double cooling) {
        resetVariableForces(variables);

        // Calculate forces on each variable. The force is proportional to the displacement of the variable. The
        // displacement is calculated using the Kaczmarz projection.
        for (Constraint constraint : constraints) {
            if (constraint.isHard())
                continue;
            if (isSoftSatisfied(constraint))
                continue;
            //if (constraint.getOp() != OperatorType.EQ && constraint.isSatisfied(getLinearSpec().getTolerance()))
              //  continue;

            double p = getKaczmarzProjection(constraint);
            for (Summand summand : constraint.getLeftSide()) {
                Variable variable = summand.getVar();
                VariableForce variableForce = getVariableForce(variable);

                double displacement = p * summand.getCoeff();
                variableForce.addForce(getK(constraint.getPenalty()), displacement);
            }
        }
        // Apply forces on the variables.
        for (Variable variable : variables) {
            if (variable.solverCookie == null)
                continue;
            VariableForce variableForce = (VariableForce)variable.solverCookie;
            if (variableForce.nForces == 0)
                continue;

            double delta = cooling * variableForce.getZeroForceDisplacement();
            variable.setValue(variable.getValue() + delta);
        }
    }

    private void doKaczmarzHard(List<Constraint> constraints) {
        for (Constraint constraint : constraints) {
            if (!constraint.isHard() )
                continue;
            if (constraint.isSatisfied(getLinearSpec().getTolerance()))
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
            return 100000;
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
