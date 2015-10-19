/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.linsolve;

import junit.framework.TestCase;
import nz.ac.auckland.alm.*;

import java.util.List;


public class LinSolveTest extends TestCase {
    public void testInternal() {
        LinearSpec linearSpec = new LinearSpec();

        Constraint constraint = linearSpec.addConstraint(1, new Variable(), 2, new Variable(), OperatorType.EQ, 10);

        assertEquals(2, linearSpec.getVariables().size());
        assertEquals(1, linearSpec.getConstraints().size());

        linearSpec.removeConstraint(constraint);

        assertEquals(0, linearSpec.getVariables().size());
        assertEquals(0, linearSpec.getConstraints().size());
    }

    private Variable addInterval(LinearSpec linearSpec, Variable start, double min, double pref) {
        Variable next = new Variable();
        linearSpec.addConstraint(1, next, -1, start, OperatorType.GE, min);
        linearSpec.addConstraint(1, next, -1, start, OperatorType.EQ, pref, 0.5);
        return next;
    }

    private boolean fuzzyEquals(double v1, double v2) {
        return Math.abs(v1 - v2) < 1;
    }

    private double error2(LinearSpec linearSpec) {
        double error2 = 0d;
        for (Constraint constraint : linearSpec.getConstraints()) {
            if (constraint.isHard())
                continue;
            double leftSideSum = 0;
            for (Summand summand : constraint.getLeftSide())
                leftSideSum += summand.coeff * summand.var.getValue();
            error2 += Math.pow(leftSideSum - constraint.getRightSide(), 2);
        }
        return error2;
    }

    private void randomButtonSplit(LinearSpec linearSpec, Variable left, Variable top, Variable right, Variable bottom,
                                   int nButtons, boolean horizontal) {
        double width =  right.getValue() - left.getValue();
        double height = bottom.getValue() - top.getValue();
        width *= 0.75;
        height *= 0.75;
        if (nButtons == 1) {
            // min size
            linearSpec.addConstraint(1, right, -1, left, OperatorType.GE, 10);
            linearSpec.addConstraint(1, bottom, -1, top, OperatorType.GE, 10);

            // pref size
            linearSpec.addConstraint(1, right, -1, left, OperatorType.EQ, width, 0.5);
            linearSpec.addConstraint(1, bottom, -1, top, OperatorType.EQ, height, 0.5);
            return;
        }
        double splitPoint = 0.4d + 0.2d * Math.random();
        int half = (int)(nButtons * splitPoint);
        if (half == 0)
            half++;
        if (horizontal) {
            // horizontal
            XTab tab = new XTab();
            tab.setValue(left.getValue() + width * splitPoint);
            randomButtonSplit(linearSpec, left, top, tab, bottom, half, false);
            randomButtonSplit(linearSpec, tab, top, right, bottom, nButtons - half, false);
        } else {
            // vertical
            YTab tab = new YTab();
            tab.setValue(top.getValue() + height * splitPoint);
            randomButtonSplit(linearSpec, left, top, right, tab, half, true);
            randomButtonSplit(linearSpec, left, tab, right, bottom, nButtons - half, true);
        }
    }

    public void testConstraintRandomButtons() {
        final int nButtons = 100;
        System.out.println("testConstraintRandomButtons " + nButtons);

        LinearSpec linearSpec = new LinearSpec();

        Variable left = new Variable();
        left.setValue(0);
        Variable top = new Variable();
        top.setValue(0);
        Variable right = new Variable();
        right.setValue(1500);
        Variable bottom = new Variable();
        bottom.setValue(1200);

        linearSpec.addConstraint(1, left, OperatorType.EQ, left.getValue());
        linearSpec.addConstraint(1, top, OperatorType.EQ, top.getValue());
        linearSpec.addConstraint(1, right, OperatorType.EQ, right.getValue());
        linearSpec.addConstraint(1, bottom, OperatorType.EQ, bottom.getValue());

        randomButtonSplit(linearSpec, left, top, right, bottom, nButtons, true);

        benchmark(linearSpec);
    }

    public void testConstraintChains() {
        System.out.println("testConstraintChains");
        LinearSpec linearSpec = new LinearSpec();

        for (int a = 0; a < 1; a++) {
            Variable var = new Variable();
            linearSpec.addConstraint(1, var, OperatorType.EQ, 0);
            for (int i = 0; i < 50; i++)
                var = addInterval(linearSpec, var, 5 + Math.random() * 10, 5 + Math.random() * 50);
            linearSpec.addConstraint(1, var, OperatorType.EQ, 10000);
        }

        benchmark(linearSpec);
    }

    class BenchmarkResult {
        class Statistic {
            long maxTime = 0;
            long minTime = Long.MAX_VALUE;
            long n = 0;
            long sumTime = 0;

            public void addResult(long time) {
                if (maxTime < time)
                    maxTime = time;
                if (minTime > time)
                    minTime = time;
                sumTime += time;
                n++;
            }

            public long getAverageTime() {
                return sumTime / n;
            }

            @Override
            public String toString() {
                return "(" + minTime + "," + getAverageTime() + "," + maxTime + ")";
            }
        }

        final String name;
        final Statistic stat = new Statistic();
        final Statistic internalStat = new Statistic();

        public BenchmarkResult(String name) {
            this.name = name;
        }

        public void addResult(long time, long internalTime) {
            stat.addResult(time);
            internalStat.addResult(internalTime);
        }

        @Override
        public String toString() {
            return name + ": Time" + stat + " Internal time" + internalStat;
        }
    }

    private BenchmarkResult benchmark(String name, LinearSpec linearSpec, int n) {
        BenchmarkResult result = new BenchmarkResult(name);

        for (int i = 0; i < n; i++) {
            for (Variable variable : linearSpec.getVariables())
                variable.setValue(0.0);
            linearSpec.solve();
            result.addResult(linearSpec.getSolvingTime(), linearSpec.getInternalSolvingTime());
        }

        return result;
    }

    public void benchmark(LinearSpec linearSpec) {
        linearSpec.setTolerance(LinearSpec.DEFAULT_TOLERANCE);
        int n = 10;

        linearSpec.setSolver(new OJAlgoSolver());
        System.out.println(benchmark("OJAlgoSolver", linearSpec, n));
        System.out.println("soft error2: " + error2(linearSpec));

        List<Variable> variableList = linearSpec.getVariables();
        Double[] results1 = new Double[variableList.size()];
        for (int i = 0; i < variableList.size(); i++) {
            Variable variable = variableList.get(i);
            results1[i] = variable.getValue();
        }

        linearSpec.setSolver(new ForceSolver());
        System.out.println(benchmark("ForceSolver", linearSpec, n));
        System.out.println("soft error2: " + error2(linearSpec));

        linearSpec.setSolver(new ForceSolver2());
        System.out.println(benchmark("ForceSolver2", linearSpec, n));
        System.out.println("soft error2: " + error2(linearSpec));

        linearSpec.setSolver(new ForceSolver3());
        System.out.println(benchmark("ForceSolver3", linearSpec, n));
        System.out.println("soft error2: " + error2(linearSpec));

        linearSpec.setSolver(new OJAlgoSolver());
        System.out.println(benchmark("OJAlgoSolver", linearSpec, n));
        System.out.println("soft error2: " + error2(linearSpec));

        for (int i = 0; i < variableList.size(); i++) {
            //System.out.println(results1[i] + " " + variableList.get(i).getValue());
            assertTrue(fuzzyEquals(results1[i], variableList.get(i).getValue()));
        }
    }
}
