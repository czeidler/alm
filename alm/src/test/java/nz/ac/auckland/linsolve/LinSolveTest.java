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
import nz.ac.auckland.linsolve.Constraint;
import nz.ac.auckland.linsolve.LinearSpec;
import nz.ac.auckland.linsolve.OperatorType;
import nz.ac.auckland.linsolve.Variable;

import javax.sound.sampled.Line;
import javax.swing.*;
import java.awt.*;
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
        LinearSpec linearSpec = new LinearSpec();

        for (int a = 0; a < 1; a++) {
            Variable var = new Variable();
            linearSpec.addConstraint(1, var, OperatorType.EQ, 0);
            for (int i = 0; i < 10; i++)
                var = addInterval(linearSpec, var, 5 + Math.random() * 10, 5 + Math.random() * 50);
            linearSpec.addConstraint(1, var, OperatorType.EQ, 10000);
        }

        benchmark(linearSpec);
    }

    public void benchmark(LinearSpec linearSpec) {
        linearSpec.setSolver(new OJAlgoSolver());
        linearSpec.setTolerance(LinearSpec.DEFAULT_TOLERANCE);

        List<Variable> variableList = linearSpec.getVariables();

        for (Variable variable : variableList)
            variable.setValue(0.0);
        linearSpec.solve();
        for (Variable variable : variableList)
            variable.setValue(0.0);
        linearSpec.solve();
        for (Variable variable : variableList)
            variable.setValue(0.0);
        linearSpec.solve();
        System.out.println("soft error2: " + error2(linearSpec));

        Double[] results1 = new Double[variableList.size()];
        for (int i = 0; i < linearSpec.getVariables().size(); i++) {
            Variable variable = linearSpec.getVariables().get(i);
            results1[i] = variable.getValue();
        }

        System.out.println("FORCE");
        linearSpec.setSolver(new ForceSolver2());
        for (Variable variable : variableList)
            variable.setValue(0.0);
        linearSpec.solve();
        for (Variable variable : variableList)
            variable.setValue(0.0);
        linearSpec.solve();
        for (Variable variable : variableList)
            variable.setValue(0.0);
        linearSpec.solve();
        System.out.println("soft error2: " + error2(linearSpec));

        for (int i = 0; i < variableList.size(); i++) {
            //System.out.println(results1[i] + " " + variableList.get(i).getValue());
            //assertTrue(fuzzyEquals(results1[i], variableList.get(i).getValue()));
        }
    }
}
