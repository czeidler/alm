package nz.ac.auckland.linsolve;

/**
 * An interface for linear solvers. AbstractLinearSolver implements this interface. New solvers
 * should extend AbstractLinearSolver.
 */
public interface LinearSolver {

    /**
     * returns linear spec which is used by this solver.
     */
    LinearSpec getLinearSpec();

    /**
     * Replaces the linear spec which is used by this solver.
     */
    void setLinearSpec(LinearSpec linearSpec);

    void presolve();

    /**
     * Remove a cached presolved model, if existent.
     * This is automatically done each time after the model has been changed,
     * to avoid an old cached presolved model getting out of sync.
     */
    void removePresolved();

    /**
     * Adds a constraint to this solver. Some solvers keep their own set of constraints.
     */

    void add(Constraint c);

    /**
     * Updates a constraint in this solver. Some solvers keep their own set of constraints.
     */
    void update(Constraint c);

    /**
     * Removes a constraint from this solver. Some solvers keep their own set of constraints.
     */
    void remove(Constraint c);

    /**
     * Adds a variable to this solver.
     */
    void add(Variable v);

    /**
     * Removes a variable from this solver.
     */
    void remove(Variable v);

    /**
     * Tries to solve the linear problem.
     * If a cached simplified version of the problem exists, it is used instead.
     *
     * @return the result of the solving attempt
     */
    ResultType solve();

    void onSolveFinished();

    /**
     * Total time of last solving of the attempt in milliseconds.
     *
     * The time should not include the time to convert the constraint system to the solver internal problem. For
     * example, when using an external solver the time to describe the problem to the external solver should be
     * excluded.
     *
     * @return time in milliseconds, -1 if there is no internal solving time, i.e. the constraint system is used
     * directly
     */
    long getInternalSolvingTime();

    /**
     * Result of last solving attempt (e.g. OPTIMAL, INFEASIBLE, ...).
     *
     * @return result
     */
    ResultType getSolvingResult();
}
