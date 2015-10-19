package nz.ac.auckland.linsolve;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class KaczmarzLeastSquares extends AbstractLinearSolver {
    public static final int GUI_MAXITERATION = 5000;
    public static final double DEFAULT_LAMBDA = 1.9d;
    public static final double tolerance = 0.01;
    public static final double COOLING_FACTOR = 0.9;
    private double lambda = DEFAULT_LAMBDA;


    @Override
    protected ResultType doSolve() {
        double prevError = Double.MAX_VALUE;

        double maxError = Double.MAX_VALUE;
        lambda = DEFAULT_LAMBDA;
        initVariableValues();

        Collections.sort(this.getLinearSpec().getConstraints(), new Comparator<Constraint>() {
            @Override
            public int compare(Constraint constraint, Constraint constraint2) {
                return ((Double)constraint2.getPenalty()).compareTo(constraint.getPenalty());
            }
        });

        for (int i = 0; i < GUI_MAXITERATION; i++) {
            doIteration();
            double error2 = error2SoftConstraints();
            double diff = Math.abs(prevError - error2);
            if (diff < tolerance * tolerance) {
                System.out.println("Iterations 1: " + i);
                break;
            }
            prevError = error2;
        }

        if (maxError <= tolerance)
            return ResultType.OPTIMAL;
        return ResultType.SUBOPTIMAL;
    }

    private double error2SoftConstraints() {
        double error2 = 0;
        for (Constraint c : this.getLinearSpec().getConstraints()) {
            if (c.isHard())
                continue;
            error2 += Math.pow(c.error(), 2);
        }
        return error2;
    }

    protected void doIteration() {
        lambda = lambda * COOLING_FACTOR;
        double lambda1 = 1.0;
        for (Constraint c : this.getLinearSpec().getConstraints()) {
            // If constraint is enabled and operator is equality or constraint is
            // not satisfied then apply projection and use normal Kaczmarz
            /*if (c.isEnabled()
                    && (c.getOp() == OperatorType.EQ || !c.isSatisfied())) {
                projectConstraint(c, c.getPenalty() * lambda);

            }
            // Otherwise use Kaczmarz with cooling
            else {
                if (!c.isEnabled()) {
                    projectConstraint(c, c.getPenalty() * lambda);
                }
            }*/
            if (c.isSatisfied())
                continue;
            // hard or soft constraint?
            if (c.isHard())
                projectConstraint(c, lambda1);
            else
                projectConstraint(c, c.getPenalty() * lambda);
        }
    }

    /**
     * Constraint c has to have at least one summand with coefficient not zero
     *
     * @param c
     * @param lambda
     */
    protected double projectConstraint(Constraint c, double lambda) {
        // if Kaczmarz is used with COOLING_FACTOR, then constraints are
        // weighted according to their penalty.
        // Penalty is then expected to be between 1.0 (highest priority
        // constraint) and 0.0

        // calculate projection parameter (b_i - A_i*x)/|A_i|^2
        double b = c.getRightSide();
        Summand[] A = c.getLeftSide();
        double p = (b - scalarProduct(A)) / euclidianNorm(A);

        ArrayList<Double> residual = new ArrayList<Double>();
        for (Summand s : A) {
            //if (isFixed(s.getVar()))
              //  continue;
            double x = s.getVar().getValue();
            double y = x;

            // Actual projection formula
            //x = x + lambda * p * s.getCoeff();
            x = x + lambda * p * s.getCoeff();
            s.getVar().setValue(x);

            // difference between previous and current variables values
            residual.add(Math.abs(x - y));
        }
        // collecting maximum between previous and current variables values
        return 0;//Collections.max(residual);
    }

    private boolean isFixed(Variable variable) {
        for (Constraint constraint : variable.getActiveConstraints()) {
            if (!constraint.isSatisfied())
                continue;
            if (constraint.getOp() == OperatorType.EQ && constraint.getLeftSide().length == 1)
                return true;
        }
        return false;
    }

    protected double scalarProduct(Summand[] summands) {
        double ret = 0.0d;
        for (Summand s : summands) {
            ret += s.getCoeff() * s.getVar().getValue();
        }
        return ret;
    }

    protected double euclidianNorm(Summand[] summands) {
        double ret = 0.0d;
        for (Summand s : summands) {
            ret += s.getCoeff() * s.getCoeff();
        }
        return ret;
    }
}
