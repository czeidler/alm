package nz.ac.auckland.linsolve;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Specification for a linear system. It contains variables and constraints.
 */
public class LinearSpec {
    /**
     * The standard tolerance should be close to the maximum precision of double
     * here it is set to 12 decimal digits
     * <p/>
     * TODO: must take largest coefficient into account
     * if this is bigger than 1000, then the precision must be adjusted
     */
    public static final double DEFAULT_TOLERANCE = 1E-13;//0.000,000,000,0001;

    /**
     * The tolerance value used by the relaxation solver.
     * This can be set by programs.
     */
    private double tolerance = DEFAULT_TOLERANCE;

    ArrayList<Variable> variables = new ArrayList<Variable>(); // list of variables in the spec
    ArrayList<Constraint> constraints = new ArrayList<Constraint>(); // list of constraints in the spec
    private LinearSolver solver; // the linear solver which is used for solving the spec
    private long solvingTime = 0l;

    public void setConstraints(ArrayList<Constraint> constraints) {
        this.constraints = constraints;
    }

    public LinearSolver getSolver() {
        return solver;
    }

    public void setSolver(LinearSolver solver) {
        this.solver = solver;
        solver.setLinearSpec(this);
    }

    public LinearSpec() {
        //this(new AddingSoftSolver(new KaczmarzSolver()));
        //this(new GroupingSoftSolver(new KaczmarzSolver()));
        //this(new KaczmarzLeastSquares());
        //this(new ForceSolverSoftSlack());
        this(new ForceSolver());
        //this(new ForceSolver2());
        //this(new ForceSolverPureSoft());
        //this(new OJAlgoSolver());
        //this(new GroupingSoftSolver(new KaczmarzLeastSquares()));
        //this(new AddingSoftSolver(new GaussSeidelSolver(new DeterministicPivotSummandSelector(), 500)));
    }

    public LinearSpec(LinearSolver solver) {
        this.solver = solver;
        solver.setLinearSpec(this);
    }

    public double getTolerance() {
        return tolerance;
    }

    public void setTolerance(double tolerance) {
        this.tolerance = tolerance;
    }

    void cleanSolverCookies() {
        for (Variable variable : getVariables())
            variable.setSolverCookie(null);
    }

    /**
     * Checks whether a value is smaller than tolerance.
     */
    public boolean equalZero(double x) {
        return Math.abs(x) < getTolerance();
    }

    /**
     * Checks whether the difference of two values is smaller than tolerance.
     */
    public boolean isFuzzyEqual(double x, double y) {
        return equalZero(y - x);
    }

    /**
     * Returns a list of variables defined in this spec.
     */
    public List<Variable> getVariables() {
        return variables;
    }

    /**
     * Adds a new variable to the specification.
     *
     * @return false if the variable is already in the specification
     */
    protected boolean addVariableIfNotInSpec(Variable variable) {
        if (variables.contains(variable))
            return false;
        variables.add(variable);
        getSolver().add(variable);
        return true;
    }

    /**
     * Removes a variable from spec.
     */
    protected boolean removeVariable(Variable variable) {
        if (!variables.remove(variable))
            return false;
        getSolver().remove(variable);
        return true;
    }

    /**
     * Returns a list of constraints defined in this spec.
     */
    public List<Constraint> getConstraints() {
        return constraints;
    }

    /**
     * Adds a new constraint to the specification.
     *
     * @return the new constraint
     */
    public boolean addConstraint(Constraint constraint) {
        if (constraints.contains(constraint))
            return false;
        constraints.add(constraint);
        solver.add(constraint);
        solver.removePresolved();
        constraint.onConstraintAddedToLinearSpec(this);
        return true;
    }

    /**
     * removes a constraint from specification.
     */
    public boolean removeConstraint(Constraint constraint) {
        if (!constraints.remove(constraint))
            return false;
        solver.remove(constraint);
        constraint.onConstraintRemovedFromLinearSpec(this);
        return true;
    }

    /**
     * Creates a summand with one variable.
     */
    public static Summand s(double coefficient, Variable variable) {
        return new Summand(coefficient, variable);
    }

    /**
     * Creates an array of summands
     */
    public static Summand[] lhs(Summand... summands) {
        return summands;
    }


    /**
     * Creates a new soft constraint with the given values and adds it to the this
     * <c>LinearSpec</c>.
     *
     * @param leftSide  the left hand side of the constraint. Consists of all summands
     * @param op        the operator type. Can be le, ge or eq
     * @param rightSide the right hand side of the constraint.
     * @param penalty   the penalty of this soft constraint
     */
    public Constraint addConstraint(Summand[] leftSide, OperatorType op, double rightSide, double penalty) {
        Constraint constraint = new Constraint(leftSide, op, rightSide, penalty);
        addConstraint(constraint);
        return constraint;
    }

    /**
     * Creates a new hard constraint with the given values and adds it to the this
     * <c>LinearSpec</c>.
     *
     * @param lhs the left hand side of the constraint. Consists of all summands
     * @param op  the operator type. Can be le, ge or eq
     * @param rhs the right hand side of the constraint.
     */
    public Constraint addConstraint(Summand[] lhs, OperatorType op, double rhs) {
        return addConstraint(lhs, op, rhs, Constraint.MAX_PENALTY);
    }

    /**
     * creates a constraint with one summand at the lhs.
     *
     * @param coeff1 the coefficient of the first summand of the lhs
     * @param var1   the variable fo the first summand of the lhs
     */
    public Constraint addConstraint(double coeff1, Variable var1, OperatorType op, double rhs) {
        return addConstraint(lhs(s(coeff1, var1)), op, rhs);
    }

    /**
     * creates a constraint with two summands at the lhs.
     */
    public Constraint addConstraint(double coeff1, Variable var1,
                                    double coeff2, Variable var2, OperatorType op, double rhs) {
        return addConstraint(lhs(s(coeff1, var1), s(coeff2, var2)), op, rhs);
    }

    /**
     * creates a constraint with three summands at the lhs.
     */
    public Constraint addConstraint(double coeff1, Variable var1,
                                    double coeff2, Variable var2, double coeff3, Variable var3,
                                    OperatorType op, double rhs) {
        return addConstraint(lhs(s(coeff1, var1), s(coeff2, var2), s(coeff3, var3)), op, rhs);
    }

    /**
     * creates a constraint with four summands at the lhs.
     */
    public Constraint addConstraint(double coeff1, Variable var1,
                                    double coeff2, Variable var2, double coeff3, Variable var3,
                                    double coeff4, Variable var4, OperatorType op, double rhs) {
        return addConstraint(lhs(s(coeff1, var1), s(coeff2, var2), s(coeff3, var3), s(coeff4, var4)), op, rhs);
    }


    /**
     * creates a soft constraint with one summand at the lhs.
     */
    public Constraint addConstraint(double coeff1, Variable var1,
                                    OperatorType op, double rhs, double penalty) {
        return addConstraint(lhs(s(coeff1, var1)), op, rhs, penalty);
    }

    /**
     * creates a soft constraint with two summands at the lhs.
     */
    public Constraint addConstraint(double coeff1, Variable var1,
                                    double coeff2, Variable var2, OperatorType op, double rhs,
                                    double penalty) {
        return addConstraint(lhs(s(coeff1, var1), s(coeff2, var2)), op, rhs, penalty);
    }

    /**
     * creates a soft constraint with three summand at the lhs.
     */
    public Constraint addConstraint(double coeff1, Variable var1,
                                    double coeff2, Variable var2, double coeff3, Variable var3,
                                    OperatorType op, double rhs, double penalty) {
        return addConstraint(lhs(s(coeff1, var1), s(coeff2, var2), s(coeff3, var3)), op, rhs, penalty);
    }

    /**
     * creates a soft constraint with four summands at the lhs.
     */
    public Constraint addConstraint(double coeff1, Variable var1,
                                    double coeff2, Variable var2, double coeff3, Variable var3,
                                    double coeff4, Variable var4, OperatorType op, double rhs,
                                    double penalty) {
        return addConstraint(lhs(s(coeff1, var1), s(coeff2, var2), s(coeff3, var3), s(coeff4, var4)), op, rhs, penalty);
    }
    
    public double getMaxConstraintError() {
        double maxError = 0;
        for (Constraint constraint : constraints) {
            if (!constraint.isEnabled())
                continue;
            double error = constraint.error();
            maxError = Math.max(maxError, error);
        }
        return maxError;
    }

    public double getMaxHardConstraintError() {
        double maxError = 0;
        for (Constraint constraint : constraints) {
            if (!constraint.isEnabled() || !constraint.isHard())
                continue;
            double error = constraint.error();
            maxError = Math.max(maxError, error);
        }
        return maxError;
    }

    public void sortConstraintsByDescendingPenalty() {
        Collections.sort(constraints, new ConstraintComparatorByPenalty());
    }

    public ResultType solve() {
        long start = System.currentTimeMillis();
        ResultType resultType = solver.solve();
        solvingTime = System.currentTimeMillis() - start;
        solver.onSolveFinished();
        return resultType;
    }

    /**
     * Gets the last solving time.
     *
     * @return the last solving time
     */
    public long getSolvingTime() {
        return solvingTime;
    }

    public long getInternalSolvingTime() {
        long internalSolvingTime = solver.getInternalSolvingTime();
        if (internalSolvingTime < 0)
            return getSolvingTime();
        return internalSolvingTime;
    }

    /**
     * Returns the solution in form of a string.
     */
    public String getCurrentSolution() {
        String solution = "";
        for (Variable v : variables)
            solution += v.toString() + "=" + v.getValue() + " ";
        return solution;
    }

    /**
     * Returns list of constraints in form of a string. useful for debugging.
     */
    public String toString() {
        StringBuffer s = new StringBuffer();
        int i = 1;
        for (Constraint c : constraints) {
            s.append(i + ".  " + c + "\n");
            i++;
        }
        return s.toString();

    }
}
