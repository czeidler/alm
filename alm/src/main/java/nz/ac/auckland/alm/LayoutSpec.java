package nz.ac.auckland.alm;

import nz.ac.auckland.linsolve.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Layout specification.
 */
public class LayoutSpec {
    /**
     * For GUI problems 0.01 tolerance should be enough. If we want to test general problems then
     * we should increase tolerance.
     */
    public static final double GUI_TOLERANCE = 0.01;

    final LinearSpec linearSpec;
    /**
     * The areas that were added to the specification.
     */
    final List<IArea> areas = new ArrayList<IArea>();

    /**
     * X-tab for the left of the GUI.
     */
    XTab left;

    /**
     * X-tab for the right of the GUI.
     */
    XTab right;

    /**
     * Y-tab for the top of the GUI.
     */
    YTab top;

    /**
     * Y-tab for the bottom of the GUI.
     */
    YTab bottom;

    // frame of the layout, the outer tab stops are positioned within this frame according to the insets
    Area.Rect layoutFrame = new Area.Rect(0, 0, 0, 0);

    Constraint rightConstraint;
    Constraint bottomConstraint;
    Constraint leftConstraint;
    Constraint topConstraint;

    float leftInset;
    float topInset;
    float rightInset;
    float bottomInset;

    float horizontalSpacing;
    float verticalSpacing;

    final List<Constraint> customConstraints = new ArrayList<Constraint>();

    // cached layout values
    // need to be invalidated whenever the layout specification is changed
    Area.Size minSize = Area.UNDEFINED_SIZE;
    Area.Size maxSize = Area.UNDEFINED_SIZE;
    Area.Size preferredSize = Area.UNDEFINED_SIZE;
    // explicit size values
    Area.Size explicitMinSize = Area.UNDEFINED_SIZE;
    Area.Size explicitMaxSize = Area.UNDEFINED_SIZE;
    Area.Size explicitPreferredSize = Area.UNDEFINED_SIZE;

    /** Creates a new, empty layout specification containing only four tabstops left, right, top, bottom for the layout
     * boundaries. */
    public LayoutSpec() {
        this(null);
    }

    /**
     * Constructor for class <code>LayoutSpec</code>.
     *
     * @param solver the Linear Solver that defines the LayoutSpec
     */
    public LayoutSpec(LinearSolver solver) {
        if (solver == null)
            linearSpec = new LinearSpec();
        else
            linearSpec = new LinearSpec(solver);
        linearSpec.setTolerance(GUI_TOLERANCE);

        //Create the Tabs defining the edge of the layout.
        left = new XTab("left");
        top = new YTab("top");
        right = new XTab("right");
        bottom = new YTab("bottom");

        //Set Default Constraints
        leftConstraint = linearSpec.addConstraint(1, left, OperatorType.EQ, 0);
        setLeft(0);
        topConstraint = linearSpec.addConstraint(1, top, OperatorType.EQ, 0);
        setTop(0);
    }

    public LayoutSpec clone() {
        LayoutSpec layoutSpec = clone(getAreas(), customConstraints, getLeft(), getTop(), getRight(), getBottom());
        layoutSpec.linearSpec.setTolerance(linearSpec.getTolerance());
        return layoutSpec;
    }

    static public LayoutSpec clone(List<IArea> areas, List<Constraint> customConstraints, XTab left, YTab top,
                                   XTab right, YTab bottom) {
        LayoutSpec layoutSpec = new LayoutSpec();

        final Map<XTab, XTab> oldToCloneXTabs = new HashMap<XTab, XTab>();
        final Map<YTab, YTab> oldToCloneYTabs = new HashMap<YTab, YTab>();
        oldToCloneXTabs.put(left, layoutSpec.getLeft());
        oldToCloneYTabs.put(top, layoutSpec.getTop());
        oldToCloneXTabs.put(right, layoutSpec.getRight());
        oldToCloneYTabs.put(bottom, layoutSpec.getBottom());
        layoutSpec.setLeft(left.getValue());
        layoutSpec.setTop(top.getValue());
        layoutSpec.setRight(right.getValue());
        layoutSpec.setBottom(bottom.getValue());
        layoutSpec.getLeft().setValue(left.getValue());
        layoutSpec.getTop().setValue(top.getValue());
        layoutSpec.getRight().setValue(right.getValue());
        layoutSpec.getBottom().setValue(bottom.getValue());

        ITabCreator xTabCreator = new ITabCreator() {
            @Override
            public <Tab> Tab create(String name) {
                return (Tab)(new XTab(name));
            }
        };
        ITabCreator yTabCreator = new ITabCreator() {
            @Override
            public <Tab> Tab create(String name) {
                return (Tab)(new YTab(name));
            }
        };

        for (IArea area : areas) {
            XTab clonedLeft = getClonedTab(oldToCloneXTabs, area.getLeft(), xTabCreator);
            YTab clonedTop = getClonedTab(oldToCloneYTabs, area.getTop(), yTabCreator);
            XTab clonedRight = getClonedTab(oldToCloneXTabs, area.getRight(), xTabCreator);
            YTab clonedBottom = getClonedTab(oldToCloneYTabs, area.getBottom(), yTabCreator);

            ILayoutSpecArea clone = ((ILayoutSpecArea)area).clone(clonedLeft, clonedTop, clonedRight, clonedBottom);
            layoutSpec.addArea(clone);
        }

        final Map<Variable, Variable> oldToCloneVariable = new HashMap<Variable, Variable>();
        ITabCreator varCreator = new ITabCreator() {
            @Override
            public <Tab> Tab create(String name) {
                return (Tab)(new Variable(name));
            }
        };
        for (Constraint constraint : customConstraints) {
            Summand[] oldSummands = constraint.getLeftSide();
            Summand[] newSummands = new Summand[oldSummands.length];
            for (int i = 0; i < oldSummands.length; i++) {
                Summand summand = oldSummands[i];
                Variable existingVar = summand.getVar();
                Variable newVar;
                if (existingVar instanceof XTab)
                    newVar = getClonedTab(oldToCloneXTabs, (XTab) existingVar, xTabCreator);
                else if (existingVar instanceof YTab)
                    newVar = getClonedTab(oldToCloneYTabs, (YTab) existingVar, yTabCreator);
                else
                    newVar = getClonedTab(oldToCloneVariable, existingVar, varCreator);
                newSummands[i] = new Summand(summand.getCoeff(), newVar);
            }
            layoutSpec.addConstraint(newSummands, constraint.getOp(), constraint.getRightSide(),
                    constraint.getPenalty());
        }

        return layoutSpec;
    }

    private interface ITabCreator {
        <Tab> Tab create(String name);
    }

    static private <Tab extends Variable> Tab getClonedTab(Map<Tab, Tab> oldToCloneMap, Tab oldTab,
                                                           ITabCreator creator) {
        Tab tab = oldToCloneMap.get(oldTab);
        if (tab == null) {
            tab = creator.create(oldTab.getName());
            tab.setValue(oldTab.getValue());
            oldToCloneMap.put(oldTab, tab);
        }
        return tab;
    }

    static public <Tab extends Variable> boolean fuzzyEquals(Tab tab1, Tab tab2) {
        return fuzzyEquals(tab1.getValue(), tab2.getValue());
    }

    static public boolean fuzzyEquals(double tab1, double tab2) {
        return Math.abs(tab1 - tab2) < GUI_TOLERANCE;
    }

    public void release() {
        clear();

        for (Constraint constraint : customConstraints)
            linearSpec.removeConstraint(constraint);
        customConstraints.clear();

        // clean internal constraints and variables
        linearSpec.removeConstraint(rightConstraint);
        linearSpec.removeConstraint(bottomConstraint);
        linearSpec.removeConstraint(leftConstraint);
        linearSpec.removeConstraint(topConstraint);

        leftConstraint = null;
        topConstraint = null;
        rightConstraint = null;
        bottomConstraint = null;

        left = null;
        top = null;
        right = null;
        bottom = null;
    }

    /**
     * Set the X-tab for the right border of the GUI.
     *
     * @param right double which defines the X-tab
     */
    public void setRight(double right) {
        layoutFrame.right = right;
        if (rightConstraint == null || !linearSpec.getConstraints().contains(rightConstraint))
            rightConstraint = linearSpec.addConstraint(1, this.right, OperatorType.EQ, 0);

        rightConstraint.setRightSide(right - rightInset);
    }

    /**
     * Set the Y-tab for the bottom border of the GUI.
     *
     * @param bottom double which defines the Y-tab
     */
    public void setBottom(double bottom) {
        layoutFrame.bottom = bottom;
        if (bottomConstraint == null || !linearSpec.getConstraints().contains(bottomConstraint))
            bottomConstraint = linearSpec.addConstraint(1, this.bottom, OperatorType.EQ, 0);

        bottomConstraint.setRightSide(bottom - bottomInset);
    }

    /**
     * Set the X-tab for the left border of the GUI.
     *
     * @param left double which defines the X-tab
     */
    public void setLeft(double left) {
        layoutFrame.left = left;
        leftConstraint.setRightSide(left + leftInset);
    }

    /**
     * Set the Y-tab for the top border of the GUI.
     *
     * @param top double which defines the X-tab
     */
    public void setTop(double top) {
        layoutFrame.top = top;
        topConstraint.setRightSide(top + topInset);
    }

    /**
     * Solve the linear equation with LinearSpec.
     */
    public ResultType solve() {
        return linearSpec.solve();
    }

    /**
     * If the layout is solved previously the cached mininum size,
     * maximum size and preferred size are invalidated;
     * so they need be recalculated when accessing them next time.
     */
    void invalidateLayout() {
        minSize = Area.UNDEFINED_SIZE;
        maxSize = Area.UNDEFINED_SIZE;
        preferredSize = Area.UNDEFINED_SIZE;
    }

    private Area.Size composeSize(Area.Size explicitSize, Area.Size size) {
        Area.Size composedSize = new Area.Size(size);
        if (explicitSize.getWidth() != Area.Size.UNDEFINED)
            composedSize.setWidth(explicitSize.getWidth());
        if (explicitSize.getHeight() != Area.Size.UNDEFINED)
            composedSize.setHeight(explicitSize.getHeight());
        return composedSize;
    }

    private void addInsets(Area.Size size) {
        size.setWidth(size.getWidth() + leftInset + rightInset);
        size.setWidth(size.getWidth() + topInset + bottomInset);
    }

    /**
     * Get the cached minimal size of the GUI, if there was none it will be calculated.
     * To invalidate the cache use invalidateLayout().
     *
     * @return Size defining the minimal size of the GUI
     */
    public Area.Size getMinSize() {
        if (explicitMinSize.getWidth() != Area.Size.UNDEFINED && explicitMinSize.getHeight() != Area.Size.UNDEFINED)
            return explicitMinSize;
        if (minSize == Area.UNDEFINED_SIZE)
            minSize = calculateMinSize();
        return composeSize(explicitMinSize, minSize);
    }

    /**
     * Get the cached maximal size of the GUI, if there was none it will be calculated.
     * To invalidate the cache use invalidateLayout().
     *
     * @return Size defining the maximal size of the GUI
     */
    public Area.Size getMaxSize() {
        if (explicitMaxSize.getWidth() != Area.Size.UNDEFINED && explicitMaxSize.getHeight() != Area.Size.UNDEFINED)
            return explicitMaxSize;
        if (maxSize == Area.UNDEFINED_SIZE)
            maxSize = calculateMaxSize();
        return composeSize(explicitMaxSize, maxSize);
    }

    /**
     * Get the cached preferred size of the GUI, if there was none it will be calculated.
     * To invalidate the cache use invalidateLayout().
     *
     * @return Size defining the preferred size of the GUI
     */
    public Area.Size getPreferredSize() {
        if (explicitPreferredSize.getWidth() != Area.Size.UNDEFINED
                && explicitPreferredSize.getHeight() != Area.Size.UNDEFINED)
            return explicitPreferredSize;
        if (preferredSize == Area.UNDEFINED_SIZE)
            preferredSize = calculatePreferredSize();
        return composeSize(explicitPreferredSize, preferredSize);
    }

    /**
     * Set explicit layout minimal size.
     *
     * This overrides the calculated value.
     *
     * @param explicitMinSize The explicit size.
     */
    public void setExplicitMinSize(Area.Size explicitMinSize) {
        this.explicitMinSize = explicitMinSize;
    }

    /**
     * Set explicit layout maximal size.
     *
     * This overrides the calculated value.
     *
     * @param explicitMaxSize The explicit size.
     */
    public void setExplicitMaxSize(Area.Size explicitMaxSize) {
        this.explicitMaxSize = explicitMaxSize;
    }

    /**
     * Set explicit layout preferred size.
     *
     * This overrides the calculated value.
     *
     * @param explicitPreferredSize The explicit size.
     */
    public void setExplicitPreferredSize(Area.Size explicitPreferredSize) {
        this.explicitPreferredSize = explicitPreferredSize;
    }

    private double[] getVariableValues() {
        double[] values = new double[linearSpec.getVariables().size()];
        for (int i = 0; i < linearSpec.getVariables().size(); i++)
            values[i] = linearSpec.getVariables().get(i).getValue();
        return values;
    }

    private void applyVariableValues(double[] variableValues) {
        for (int i = 0; i < linearSpec.getVariables().size(); i++)
            linearSpec.getVariables().get(i).setValue(variableValues[i]);
    }

    /**
     * Calculate the minimal size of the GUI.
     * If the specifications have not changed use getMinSize to get an
     * cached value for the minimal size and save some CPU cycles.
     *
     * @return Size defining the minimal size of the GUI
     */
    private Area.Size calculateMinSize() {
        double[] oldVariableValues = getVariableValues();

        //Store the preferred sizes and temporarily set the preferred size to the min size.
        Area.Size[] store = new Area.Size[areas.size()];
        for (int i = 0; i < areas.size(); i++) {
            if (!(areas.get(i) instanceof Area))
                continue;
            Area a = (Area)areas.get(i);
            store[i] = a.getPreferredSize();
            a.setPreferredSize(a.getMinSize());
        }

        //Calculate the preferred container size with the min sizes set as preferred sizes.
        Area.Size min = calculatePreferredSize();

        //Restore the original preferredSizeValues.
        for (int i = 0; i < areas.size(); i++) {
            if (!(areas.get(i) instanceof Area))
                continue;
            Area a = (Area)areas.get(i);
            a.setPreferredSize(store[i]);
        }

        applyVariableValues(oldVariableValues);
        return min;
    }

    /**
     * Calculate the maximal size of the GUI.
     *
     * @return Size defining the maximal size of the GUI
     */
    private Area.Size calculateMaxSize() {
        /*
		 * List<Summand> buf = getObjFunctionSummands();
		 * setObjFunctionSummands(new ArrayList<Summand>());
		 * addObjFunctionSummand(-1, right); addObjFunctionSummand(-1, bottom);
		 * solveLayout(); setObjFunctionSummands(buf); updateObjFunction();
		 */

        return new Area.Size(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    /**
     * Calculate the preferred size of the GUI.
     * If the specifications have not changed use getPreferredSize to get an cached value
     * for the preferred size and save some CPU cycles.
     * TODO - It returns the current size.
     *
     * @return Size defining the preferred size of the GUI
     */
    private Area.Size calculatePreferredSize() {
        double[] oldVariableValues = getVariableValues();

        //Store the current constraint values and reset GUI edge tabs and constraints to default.
        double leftValue = leftConstraint.getRightSide();
        double topValue = topConstraint.getRightSide();
        double rightValue = Double.NaN;
        double bottomValue = Double.NaN;
        right.setValue(Double.NaN);
        bottom.setValue(Double.NaN);
        //Set top/left values to default
        setLeft(0);
        setTop(0);
        //Remove the constraints from the specification once the values are stored.
        if (rightConstraint != null) {
            rightValue = rightConstraint.getRightSide();
            linearSpec.removeConstraint(rightConstraint);
            rightConstraint = null;
        }

        if (bottomConstraint != null) {
            bottomValue = bottomConstraint.getRightSide();
            linearSpec.removeConstraint(bottomConstraint);
            bottomConstraint = null;
        }

        //Solve with no fixed GUI size
        solve();

        //Resulting value for left/right is the preferred size of the GUI
        Area.Size prefSize = new Area.Size(0, 0);
        prefSize.width = (int) Math.round(right.getValue() - left.getValue());
        prefSize.height = (int) Math.round(bottom.getValue() - top.getValue());


        //reset the values to the state they were in before this method was called
        setLeft(leftValue);
        setTop(topValue);
        //Will execute with everything except NaN
        if (rightValue == rightValue) {
            setRight(rightValue);
        }
        if (bottomValue == bottomValue) {
            setBottom(bottomValue);
        }

        applyVariableValues(oldVariableValues);
        return prefSize;
    }

    /**
     * Adds a new area to the specification, automatically setting preferred
     * size constraints.
     *
     * @param left    left border
     * @param top     top border
     * @param right   right border
     * @param bottom  bottom border
     * @return the new area
     */
    public Area addArea(XTab left, YTab top, XTab right, YTab bottom) {
        Area area = new Area(left, top, right, bottom);
        addArea(area);
        return area;
    }

    public void addArea(ILayoutSpecArea area) {
        invalidateLayout();
        areas.add(area);
        area.attachedToLayoutSpec(this);
    }

    public void removeArea(ILayoutSpecArea area) {
        getAreas().remove(area);
        area.detachedFromLinearSpec(this);
    }

    /**
     * Removes all Areas from the layout.
     */
    public void clear() {
        for (Constraint constraint : customConstraints)
            constraint.remove();
        customConstraints.clear();

        while (areas.size() > 0)
            removeArea((ILayoutSpecArea)areas.get(0));
    }

    /**
     * Get the areas that were added to the specification.
     *
     * @return the areas that were added to the specification.
     */
    public List<IArea> getAreas() {
        return areas;
    }

    public List<Constraint> getCustomConstraints() {
        return customConstraints;
    }

    /**
     * Get the X-tab for the left of the GUI.
     *
     * @return the x-tab.
     */
    public XTab getLeft() {
        return left;
    }

    /**
     * Get the X-tab for the right of the GUI.
     *
     * @return the x-tab.
     */
    public XTab getRight() {
        return right;
    }

    /**
     * Get the Y-tab for the top of the GUI.
     *
     * @return the y-tab.
     */
    public YTab getTop() {
        return top;
    }

    /**
     * Get the Y-tab for the bottom of the GUI.
     *
     * @return the y-tab.
     */
    public YTab getBottom() {
        return bottom;
    }

    public float getLeftInset() {
        return leftInset;
    }

    public void setLeftInset(float leftInset) {
        this.leftInset = leftInset;
        setLeft(layoutFrame.left);
    }

    public float getTopInset() {
        return topInset;
    }

    public void setTopInset(float topInset) {
        this.topInset = topInset;
        setTop(layoutFrame.top);
    }

    public float getRightInset() {
        return rightInset;
    }

    public void setRightInset(float rightInset) {
        this.rightInset = rightInset;
        setRight(layoutFrame.right);
    }

    public float getBottomInset() {
        return bottomInset;
    }

    public void setBottomInset(float bottomInset) {
        this.bottomInset = bottomInset;
        setBottom(layoutFrame.bottom);
    }

    public float getHorizontalSpacing() {
        return horizontalSpacing;
    }

    public void setHorizontalSpacing(float horizontalSpacing) {
        this.horizontalSpacing = horizontalSpacing;
    }

    public float getVerticalSpacing() {
        return verticalSpacing;
    }

    public void setVerticalSpacing(float verticalSpacing) {
        this.verticalSpacing = verticalSpacing;
    }

    public void setSpacing(float spacing) {
        setHorizontalSpacing(spacing);
        setVerticalSpacing(spacing);
    }

    /**
     * Creates a new hard constraint with the given values and adds it to the this
     * <c>LinearSpec</c>.
     *
     * @param leftSide the left hand side of the constraint. Consists of all summands
     * @param operator  the operator type. Can be le, ge or eq
     * @param rightSide the right hand side of the constraint.
     */
    public Constraint addConstraint(Summand[] leftSide, OperatorType operator, double rightSide) {
        Constraint constraint = linearSpec.addConstraint(leftSide, operator, rightSide, Constraint.MAX_PENALTY);
        customConstraints.add(constraint);
        return constraint;
    }

    /**
     * Creates a new soft constraint with the given values and adds it to the this
     * <c>LinearSpec</c>.
     *
     * @param leftSide     the left hand side of the constraint. Consists of all summands
     * @param operator      the operator type. Can be le, ge or eq
     * @param rightSide     the right hand side of the constraint.
     * @param penalty the penalty of this soft constraint
     */
    public Constraint addConstraint(Summand[] leftSide, OperatorType operator, double rightSide, double penalty) {
        Constraint constraint = linearSpec.addConstraint(leftSide, operator, rightSide, penalty);
        customConstraints.add(constraint);
        return constraint;
    }

    public boolean removeConstraint(Constraint constraint) {
        if (!customConstraints.remove(constraint))
            return false;
        linearSpec.removeConstraint(constraint);
        return true;
    }

    private Summand[] toArray(Summand ... summands) {
        return summands;
    }

    public Constraint addConstraint(double coeff1, Variable var1, OperatorType operator, double rightSide) {
        return addConstraint(toArray(new Summand(coeff1, var1)), operator, rightSide);
    }

    public Constraint addConstraint(double coeff1, Variable var1, double coeff2, Variable var2, OperatorType operator,
                                    double rightSide) {
        return addConstraint(toArray(new Summand(coeff1, var1), new Summand(coeff2, var2)), operator, rightSide);
    }

    public Constraint addConstraint(double coeff1, Variable var1, OperatorType operator, double rightSide,
                                    double penalty) {
        return addConstraint(toArray(new Summand(coeff1, var1)), operator, rightSide, penalty);
    }

    public Constraint addConstraint(double coeff1, Variable var1, double coeff2, Variable var2, OperatorType operator,
                                    double rightSide, double penalty) {
        return addConstraint(toArray(new Summand(coeff1, var1), new Summand(coeff2, var2)), operator, rightSide,
                penalty);
    }

    @Override
    public String toString() {
        String out = "Areas:\n";
        for (IArea area : getAreas())
            out += area.toString() + "\n";

        out += "Custom Constraints:\n";
        for (Constraint constraint : getCustomConstraints())
            out += constraint.toString() + "\n";

        return out;
    }
}
