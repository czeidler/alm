package nz.ac.auckland.linsolve;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a linear constraint. The constraint can be in form
 * of a equality or an inequality. A penalty can be associated to the constraint
 * which specifies its priority. Higher priority means less chance to get
 * violated.
 */
public class Constraint implements Cloneable {
    public static final double MIN_PENALTY = 0.0;
    public static final double MAX_PENALTY = 1.0;

    public interface IDynamicRightSide {
        double getRightSide();
    }

    protected LinearSpec linearSpec; // linear spec which holds this constraint
    private Summand[] leftSide; // left side of the constraint
    private OperatorType op; // constraint operator (i.e. =, <-, >=)
    private boolean enabled = true; // is constraint enabled?
    private int unassignedVariables = 0; // number of unassigned variables
    private Summand pivotSummand;
    private Summand dNegSummand, dPosSummand;    // negative and positive slack variables. Only used in solver that needs it
    private double rightSide; // right side of the constraint
    private IDynamicRightSide dynamicRightSide; // if right side can vary
    private double penalty = Double.POSITIVE_INFINITY; // penalty, if the constraint gets violated
    private String name; // a string which specifies the constraint. useful for debugging

    /**
     * Constructor.
     */
    public Constraint(Summand[] summands, OperatorType op, double rightSide, double penalty) {
        setLeftSide(summands);
        setOp(op);
        setRightSide(rightSide);
        setPenalty(penalty);
    }

    /**
     * Creates an array of summands
     */
    private static Summand[] lhs(Summand... summands) {
        return summands;
    }

    /**
     * Creates a new hard constraint with the given values and adds it to the this
     * <c>LinearSpec</c>.
     *
     * @param lhs the left hand side of the constraint. Consists of all summands
     * @param op  the operator type. Can be le, ge or eq
     * @param rhs the right hand side of the constraint.
     */
    public Constraint(Summand[] lhs, OperatorType op, double rhs) {
        this(lhs, op, rhs, Constraint.MAX_PENALTY);
    }

    /**
     * creates a constraint with one summand at the lhs.
     *
     * @param coeff1 the coefficient of the first summand of the lhs
     * @param var1   the variable fo the first summand of the lhs
     */
    public Constraint(double coeff1, Variable var1, OperatorType op, double rhs) {
        this(lhs(new Summand(coeff1, var1)), op, rhs);
    }

    /**
     * creates a constraint with two summands at the lhs.
     */
    public Constraint(double coeff1, Variable var1, double coeff2, Variable var2, OperatorType op, double rhs) {
        this(lhs(new Summand(coeff1, var1), new Summand(coeff2, var2)), op, rhs);
    }

    /**
     * creates a constraint with three summands at the lhs.
     */
    public Constraint(double coeff1, Variable var1, double coeff2, Variable var2, double coeff3, Variable var3,
                      OperatorType op, double rhs) {
        this(lhs(new Summand(coeff1, var1), new Summand(coeff2, var2), new Summand(coeff3, var3)), op, rhs);
    }

    /**
     * creates a constraint with four summands at the lhs.
     */
    public Constraint(double coeff1, Variable var1, double coeff2, Variable var2, double coeff3, Variable var3,
                      double coeff4, Variable var4, OperatorType op, double rhs) {
        this(lhs(new Summand(coeff1, var1), new Summand(coeff2, var2), new Summand(coeff3, var3), new Summand(coeff4, var4)), op, rhs);
    }


    /**
     * creates a soft constraint with one summand at the lhs.
     */
    public Constraint(double coeff1, Variable var1, OperatorType op, double rhs, double penalty) {
        this(lhs(new Summand(coeff1, var1)), op, rhs, penalty);
    }

    /**
     * creates a soft constraint with two summands at the lhs.
     */
    public Constraint(double coeff1, Variable var1, double coeff2, Variable var2, OperatorType op, double rhs,
                      double penalty) {
        this(lhs(new Summand(coeff1, var1), new Summand(coeff2, var2)), op, rhs, penalty);
    }

    /**
     * creates a soft constraint with three summand at the lhs.
     */
    public Constraint(double coeff1, Variable var1, double coeff2, Variable var2, double coeff3, Variable var3,
                      OperatorType op, double rhs, double penalty) {
        this(lhs(new Summand(coeff1, var1), new Summand(coeff2, var2), new Summand(coeff3, var3)), op, rhs, penalty);
    }

    /**
     * creates a soft constraint with four summands at the lhs.
     */
    public Constraint(double coeff1, Variable var1, double coeff2, Variable var2, double coeff3, Variable var3,
                      double coeff4, Variable var4, OperatorType op, double rhs, double penalty) {
        this(lhs(new Summand(coeff1, var1), new Summand(coeff2, var2), new Summand(coeff3, var3),
                new Summand(coeff4, var4)), op, rhs, penalty);
    }

    protected void onConstraintAddedToLinearSpec(LinearSpec ls) {
        if (this.linearSpec != null)
            throw new RuntimeException("Constraint is already added to a LinearSpec!");
        this.linearSpec = ls;
        activateSummands();
    }

    protected void onConstraintRemovedFromLinearSpec(LinearSpec ls) {
        if (this.linearSpec != ls)
            throw new RuntimeException("Constraint is not attached to LinearSpec.");
        this.linearSpec = null;
        deactivateSummands();
    }

    /**
     * Returns the number of unassigned variables.
     */
    public int getUnassignedVariables() {
        return unassignedVariables;
    }

    /**
     * Sets the number of unassigned variables.
     */
    public void setUnassignedVariables(int unassignedVariables) {
        this.unassignedVariables = unassignedVariables;
    }

    public Summand getPivotSummand() {
        return pivotSummand;
    }

    public void setPivotSummand(Summand pivotSummand) {
        this.pivotSummand = pivotSummand;
    }

    /**
     * Retruns constraint's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets constraint's name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the summand for negative slack variables.
     */
    public void setdNegSummand(Summand dNegSummand) {
        this.dNegSummand = dNegSummand;
    }

    /**
     * Sets the summand for positive slack variables.
     */
    public void setdPosSummand(Summand dPosSummand) {
        this.dPosSummand = dPosSummand;
    }

    /**
     * Returns the summand for negative slack variables.
     */
    public Summand getdNegSummand() {
        return dNegSummand;
    }

    /**
     * Returns the summand for positive slack variables.
     */
    public Summand getdPosSummand() {
        return dPosSummand;
    }

    /**
     * Is this constraint enabled?
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Makes this constraint enabled/disabled.
     */

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Gets the index of the constraint.
     *
     * @return the index of the constraint or -1 if not found
     */
    public int getIndex() {
        int i = -1;
        if (linearSpec != null) {
            i = linearSpec.getConstraints().indexOf(this);
            if (i == -1)
                throw new RuntimeException("Constraint not part of linearSpec.constraints.");
        }
        return i + 1;
    }

    /**
     * Converts the constraint to a string. useful for debugging.
     */
    @Override
    public String toString() {
        StringBuffer strBuf = new StringBuffer();
        Summand[] summands = this.getLeftSide();
        for (int i = 0; i < summands.length - 1; i++) {
            if (summands[i].equals(pivotSummand))
                strBuf.append("#");
            strBuf.append(summands[i]);
            strBuf.append(" + ");
        }

        if (summands[summands.length - 1].equals(pivotSummand))
            strBuf.append("#");
        strBuf.append(summands[summands.length - 1]);
        strBuf.append(" ");
        strBuf.append(OperatorToString(this.getOp()));
        strBuf.append(" ");
        strBuf.append(this.getRightSide());
        strBuf.append("\tpenalty=");
        strBuf.append(this.getPenalty());
        strBuf.append("\terror=" + error());
        //strBuf.append("\tCurrentSolution=" + linearSpec.getCurrentSolution());
        if (name != null)
            strBuf.append("\tname=" + name);
        if (!isEnabled())
            strBuf.append("\tDISABLED");
        return strBuf.toString();
    }

    /**
     * Converts the constraint to a string. useful for generating test cases.
     */
    public String toCommand() {
        StringBuffer strBuf = new StringBuffer();
        Summand[] summands = this.getLeftSide();
        strBuf.append("linearSpec.addConstraint(");
        for (int i = 0; i < summands.length - 1; i++) {
            if (summands[i].equals(pivotSummand))
                strBuf.append("#");
            strBuf.append(summands[i].toCommand());
            strBuf.append(", ");
        }

        if (summands[summands.length - 1].equals(pivotSummand))
            strBuf.append("#");
        strBuf.append(summands[summands.length - 1].toCommand());
        strBuf.append(", ");
        strBuf.append(OperatorToCommand(this.getOp()));
        strBuf.append(", ");
        strBuf.append(this.getRightSide());
        strBuf.append(", ");
        if (this.getPenalty() == Double.POSITIVE_INFINITY)
            strBuf.append("Double.POSITIVE_INFINITY);");
        else
            strBuf.append(this.getPenalty() + ");");
        return strBuf.toString();
    }


    /**
     * Converts an operator to a string (e.g. OperatorType.GE -> ">=")
     */
    public String OperatorToString(OperatorType o) {
        if (o == OperatorType.EQ) {
            return "=";
        } else if (o == OperatorType.GE) {
            return ">=";
        } else if (o == OperatorType.LE) {
            return "<=";
        } else {
            return "UNKNOWN OPERATOR";
        }
    }

    /**
     * Converts an operator to its equivalent string (e.g. OperatorType.GE -> "OperatorType.GE")
     */
    public String OperatorToCommand(OperatorType o) {
        if (o == OperatorType.EQ) {
            return "OperatorType.EQ";
        } else if (o == OperatorType.GE) {
            return "OperatorType.GE";
        } else if (o == OperatorType.LE) {
            return "OperatorType.LE";
        } else {
            return "UNKNOWN OPERATOR";
        }
    }

    /**
     * Converts a string to its equivalent operator (e.g. ">=" -> OperatorType.GE)
     */
    public OperatorType StringToOperator(String s) {
        if (s.equals("=")) {
            return OperatorType.EQ;
        } else if (s.equals(">=")) {
            return OperatorType.GE;
        } else if (s.equals("<=")) {
            return OperatorType.LE;
        } else {
            System.out
                    .print("INCORRECT STRING PASSED INTO Constraint.StringToOperator()");
            return OperatorType.EQ;
        }
    }

    /**
     * Returns the number of positive coefficients in the left side of the constraint.
     */
    public int getNumberOfPositiveCoefficients() {
        int count = 0;
        for (Summand s : leftSide) {
            if (s.getCoeff() > 0)
                count++;
        }
        return count;
    }

    /**
     * Returns the number of negative coefficients in the left side of the constraint.
     */
    public int getNumberOfNegativeCoefficients() {
        int count = 0;
        for (Summand s : leftSide) {
            if (s.getCoeff() < 0)
                count++;
        }
        return count;
    }

    /**
     * Returns summands with positive coefficients.
     */
    public Summand[] getLeftSideWithPositiveCoefficients() {
        Summand[] positiveLeftSide = new Summand[getNumberOfPositiveCoefficients()];
        int positiveIndex = 0;
        for (Summand s : leftSide) {
            if (s.getCoeff() > 0) {
                positiveLeftSide[positiveIndex] = s;
                positiveIndex++;
            }
        }
        return positiveLeftSide;
    }

    /**
     * Returns summands with negative coefficients.
     */
    public Summand[] getLeftSideWithNegativeCoefficients() {
        Summand[] negativeLeftSide = new Summand[getNumberOfNegativeCoefficients()];
        int negativeIndex = 0;
        for (Summand s : leftSide) {
            if (s.getCoeff() < 0) {
                negativeLeftSide[negativeIndex] = s;
                negativeIndex++;
            }
        }
        return negativeLeftSide;
    }

    /**
     * Returns coefficients of the left side of the constraint in form of an array.
     */
    public double[] getLeftSideCoefficients() {
        double[] coefficients = new double[leftSide.length];
        for (int i = 0; i < leftSide.length; i++) {
            coefficients[i] = leftSide[i].getCoeff();
        }
        return coefficients;
    }

    private void notifyConstraintUpdated() {
        if (linearSpec == null)
            return;
        linearSpec.getSolver().update(this);
    }

    /**
     * Gets the left side of the constraint.
     *
     * @return the summands on the left side of the constraint
     */
    public Summand[] getLeftSide() {
        return leftSide;
    }

    /**
     * Sets the summands on the left side of the constraint. The old summands
     * are NOT deleted.
     *
     * @param summands an array of Summand objects that make up the new left side
     */
    public void setLeftSide(Summand... summands) {
        if (linearSpec != null)
            deactivateSummands();
        leftSide = summands;
        activateSummands();

        notifyConstraintUpdated();
    }

    private void deactivateSummands() {
        if (leftSide == null)
            return;

        for (int i = 0; i < leftSide.length; i++)
            leftSide[i].getVar().onConstraintDeactivated(this);
    }

    private void activateSummands() {
        if (leftSide == null || linearSpec == null)
            return;
        for (int i = 0; i < leftSide.length; i++)
            leftSide[i].getVar().onConstraintActivated(this);
    }

    /**
     * Checks to see whether a variable (v) has been appeared in the left side of
     * the constraint.
     */
    public boolean leftSideContains(Variable v) {
        for (Summand s : leftSide) {
            if (s.getVar() == v) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sum of the absolute value of all coefficients in the left side of the constraint.
     */
    public double sumOfAllAbsoluteCoefficients() {
        double sum = 0.0d;
        for (Summand s : leftSide) {
            sum += Math.abs(s.coeff);
        }

        return sum;
    }

    /**
     * Replaces left side of the constraint with a new constraint with
     * one summand.
     */
    public void setLeftSide(double coeff1, Variable var1) {
        setLeftSide(new Summand(coeff1, var1));
    }

    /**
     * Replaces left side of the constraint with a new constraint with
     * two summands.
     */
    public void setLeftSide(double coeff1, Variable var1, double coeff2,
                            Variable var2) {
        setLeftSide(new Summand(coeff1, var1), new Summand(coeff2, var2));
    }

    /**
     * Replaces left side of the constraint with a new constraint with
     * three summands.
     */
    public void setLeftSide(double coeff1, Variable var1, double coeff2,
                            Variable var2, double coeff3, Variable var3) {
        setLeftSide(new Summand(coeff1, var1), new Summand(coeff2, var2), new Summand(coeff3, var3));
    }

    /**
     * Replaces left side of the constraint with a new constraint with
     * four summands.
     */
    public void setLeftSide(double coeff1, Variable var1, double coeff2,
                            Variable var2, double coeff3, Variable var3, double coeff4,
                            Variable var4) {
        setLeftSide(new Summand(coeff1, var1), new Summand(coeff2, var2), new Summand(coeff3, var3),
                new Summand(coeff4, var4));
    }

    /**
     * Gets the operator used for this constraint.
     *
     * @return the operator used for this constraint
     */
    public OperatorType getOp() {
        return op;
    }

    /**
     * Sets the operator used for this constraint.
     *
     * @param value operator
     */
    public void setOp(OperatorType value) {
        op = value;
        notifyConstraintUpdated();
    }

    /**
     * Gets the constant value that is on the right side of the operator.
     *
     * @return the constant value that is on the right side of the operator
     */
    public double getRightSide() {
        if (dynamicRightSide != null)
            return dynamicRightSide.getRightSide();
        return rightSide;
    }

    /**
     * Sets the constant value that is on the right side of the operator.
     *
     * @param value constant value that is on the right side of the operator
     */
    public void setRightSide(double value) {
        rightSide = value;
        notifyConstraintUpdated();
    }

    public void setRightSide(IDynamicRightSide dynamicRightSide) {
        this.dynamicRightSide = dynamicRightSide;
        notifyConstraintUpdated();
    }

    /**
     * Gets the coefficient of positive summand.
     *
     * @return the coefficient of positive summand.
     */
    public double getPenalty() {
        return penalty;
    }

    public boolean isHard() {
        return penalty == 1.f;
    }

    /**
     * The penalty coefficient for deviations from the soft constraint's exact
     * solution.
     *
     * @param value coefficient of penalty <code>double</code>
     */
    public void setPenalty(double value) {
        penalty = value;
        notifyConstraintUpdated();
    }

    public boolean isSatisfied(double tolerance) {
        return Math.abs(residual()) < tolerance;
    }

    public boolean isSatisfied() {
        return isSatisfied(linearSpec.getTolerance());
    }

    /**
     * Here we calculate residual for Linear relaxation.
     */
    public double residual() {
        double leftSideSum = 0;
        for (Summand summand : leftSide)
            leftSideSum += summand.coeff * summand.var.getValue();
        if (op == OperatorType.GE && leftSideSum >= getRightSide())
            return 0;
        if (op == OperatorType.LE && leftSideSum <= getRightSide())
            return 0;
        return getRightSide() - leftSideSum;
    }

    public double error() {
        return Math.abs(residual());
    }

    /**
     * Calculates the value for a variable in the constraint.
     */
    public double newVarValue(Summand chosenSummand) {
        double sumOfOtherSummands = 0;
        for (Summand summand : getLeftSide()) {

            // keep chosen summand on the left side
            if (chosenSummand == summand)
                continue;
            sumOfOtherSummands += summand.coeff * summand.var.getValue();
        }

        return (getRightSide() - sumOfOtherSummands) / chosenSummand.coeff;
    }

    /**
     * creates a clone of the constraint.
     */
    public Constraint clone() {
        try {
            return (Constraint) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns a list of variables in this constraint.
     */
    public List<Variable> getVariables() {
        List<Variable> variables = new ArrayList<Variable>();
        for (Summand s : leftSide)
            variables.add(s.var);
        return variables;
    }

    public void remove() {
        if (linearSpec == null)
            return;
        linearSpec.removeConstraint(this);
    }
}
