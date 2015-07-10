/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.linsolve;

import junit.framework.TestCase;
import nz.ac.auckland.linsolve.Constraint;
import nz.ac.auckland.linsolve.LinearSpec;
import nz.ac.auckland.linsolve.OperatorType;
import nz.ac.auckland.linsolve.Variable;


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
}
