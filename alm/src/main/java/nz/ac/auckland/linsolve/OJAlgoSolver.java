/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.linsolve;


import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.convex.ConvexSolver;

import java.util.ArrayList;
import java.util.List;


public class OJAlgoSolver extends AbstractLinearSolver {

    private void fillMatrix(PrimitiveDenseStore a, PrimitiveDenseStore b, List<Constraint> constraints) {
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

        PrimitiveDenseStore hardEqA = PrimitiveDenseStore.FACTORY.makeZero(hardEqualityConstraints.size(),
                variables.size());
        PrimitiveDenseStore hardEqB = PrimitiveDenseStore.FACTORY.makeZero(hardEqualityConstraints.size(), 1);
        fillMatrix(hardEqA, hardEqB, hardEqualityConstraints);

        PrimitiveDenseStore hardIneqA = PrimitiveDenseStore.FACTORY.makeZero(hardInequalityConstraints.size(),
                variables.size());
        PrimitiveDenseStore hardIneqB = PrimitiveDenseStore.FACTORY.makeZero(hardInequalityConstraints.size(), 1);
        fillMatrix(hardIneqA, hardIneqB, hardInequalityConstraints);

        ConvexSolver.Builder builder = ConvexSolver.getBuilder().equalities(hardEqA, hardEqB)
                .inequalities(hardIneqA, hardIneqB);

        if (softConstraints.size() > 0) {
            PrimitiveDenseStore softA = PrimitiveDenseStore.FACTORY.makeZero(softConstraints.size(),
                    variables.size());
            PrimitiveDenseStore softB = PrimitiveDenseStore.FACTORY.makeZero(softConstraints.size(), 1);
            fillMatrix(softA, softB, softConstraints);
            MatrixStore<Double> Q = softA.transpose().multiply(softA);
            MatrixStore<Double> C = softA.transpose().multiply(softB);
            builder.objective(Q, C);
        }

        ConvexSolver solver = builder.build();

        long startTime = System.currentTimeMillis();
        final Optimisation.Result result = solver.solve();
        internalSolvingTime = System.currentTimeMillis() - startTime;

        for (int i = 0; i < variables.size(); i++)
            variables.get(i).setValue(result.doubleValue(i));

        return ResultType.OPTIMAL;
    }
}

