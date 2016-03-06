/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.linsolve;

import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.SparseStore;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.convex.ConvexSolver;

import java.util.ArrayList;
import java.util.List;


public class OJAlgoSolverSparse extends AbstractLinearSolver {

    private void fillMatrix(SparseStore a, SparseStore b, List<Constraint> constraints) {
        for (int i = 0; i < constraints.size(); i++) {
            Constraint constraint = constraints.get(i);
            double factor = 1d;
            if (constraint.isHard() && constraint.getOp() == OperatorType.GE)
                factor = -1d;

            for (Summand summand : constraint.getLeftSide())
                a.set(i, summand.getVar().getIndex(), summand.getCoeff() * factor);
            b.set(i, 0, constraint.getRightSide() * factor);
        }
    }

    @Override
    protected ResultType doSolve() {
        List<Variable> variables = getLinearSpec().getVariables();

        List<Constraint> softConstraints = new ArrayList<Constraint>();
        List<Constraint> hardEqualityConstraints = new ArrayList<Constraint>();
        List<Constraint> hardInequalityConstraints = new ArrayList<Constraint>();
        for (Constraint constraint : getLinearSpec().getConstraints()) {
            if (constraint.isHard()) {
                if (constraint.getOp() == OperatorType.EQ)
                    hardEqualityConstraints.add(constraint);
                else
                    hardInequalityConstraints.add(constraint);
            } else
                softConstraints.add(constraint);
        }

        SparseStore<Double> hardEqA = SparseStore.makePrimitive(hardEqualityConstraints.size(),
                variables.size());
        SparseStore<Double> hardEqB = SparseStore.makePrimitive(hardEqualityConstraints.size(), 1);
        fillMatrix(hardEqA, hardEqB, hardEqualityConstraints);

        SparseStore<Double> hardIneqA = SparseStore.makePrimitive(hardInequalityConstraints.size(),
                variables.size());
        SparseStore<Double> hardIneqB = SparseStore.makePrimitive(hardInequalityConstraints.size(), 1);
        fillMatrix(hardIneqA, hardIneqB, hardInequalityConstraints);

        ConvexSolver.Builder builder = ConvexSolver.getBuilder().equalities(hardEqA, hardEqB)
                .inequalities(hardIneqA, hardIneqB);

        if (softConstraints.size() > 0) {
            SparseStore<Double> softA = SparseStore.makePrimitive(softConstraints.size(),
                    variables.size());
            SparseStore<Double> softB = SparseStore.makePrimitive(softConstraints.size(), 1);
            fillMatrix(softA, softB, softConstraints);
            MatrixStore<Double> Q = softA.transpose().multiply(softA);
            MatrixStore<Double> C = softA.transpose().multiply(softB);
            builder.objective(Q, C);
        }

        long startTime = System.currentTimeMillis();
        ConvexSolver solver = builder.build();
        final Optimisation.Result result = solver.solve();
        internalSolvingTime = System.currentTimeMillis() - startTime;

        for (int i = 0; i < variables.size(); i++)
            variables.get(i).setValue(result.doubleValue(i));

        return ResultType.OPTIMAL;
    }
}
