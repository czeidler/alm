package nz.ac.auckland.alm.swing;

import nz.ac.auckland.alm.*;
import nz.ac.auckland.linsolve.LinearSolver;

import java.awt.*;
import java.awt.Dimension;
import java.util.*;


/**
 * A GUI layout engine using the ALM.
 *
 * @author Christof
 * @author Ted
 * @author Sarah
 * @author Clemens clemens.zeidler@aucklanduni.ac.nz
 */
public class ALMLayout implements LayoutManager2 {
    static public class LayoutParams {
        final public XTab left;
        final public YTab top;
        final public XTab right;
        final public YTab bottom;

        public LayoutParams(XTab left, YTab top, XTab right, YTab bottom) {
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
        }
    }

    /**
     * The possibilities for adjusting a GUI's layout.
     * Either change the child controls so that they fit into their parent control,
     * or adjust the size of the parent control so that the children have as much space
     * as they want.
     */
    enum LayoutStyleType {
        FIT_TO_SIZE, ADJUST_SIZE
    }

    private boolean activated = true;

    /** The specification used for calculating the layout. */
    final private LayoutSpec layoutSpec;

    final Map<Component, Area> areaMap = new HashMap<Component, Area>();

    /**
     * The manner in which the GUI is dynamically adjusted. The default is to
     * fit the child controls into their parent.
     */
    private LayoutStyleType LayoutStyle = LayoutStyleType.FIT_TO_SIZE;

    /** Creates a new layout engine with an empty specification. */
    public ALMLayout() {
        layoutSpec = new LayoutSpec();
    }

    /** Creates a new layout engine with a specific solver. */
    public ALMLayout(LinearSolver solver) {
        layoutSpec = new LayoutSpec(solver);
    }

    /**
     * Creates a new layout engine with the given specification.
     *
     * @param ls the layout specification
     */
    public ALMLayout(LayoutSpec ls) {
        layoutSpec = ls;
    }

    /**
     * Place the area content accordantly to the solved layout specifications.
     * You should never need to call it by your self.
     */
	void layoutComponent(Component component, Area area) {
        Area.Rect frame = area.getContentRect();

		// set content location and size
		component.setLocation(new Point((int)Math.round(frame.left), (int)Math.round(frame.top)));
		component.setSize((int)Math.round(frame.getWidth()), (int)Math.round(frame.getHeight()));
	}

    /**
     * Implementation of the same method inherited from LayoutManager.
     * Lays out the specified container. It adds the components into the parent,
     * and calls layout() to lay them out.
     *
     * @param parent the container to be laid out
     */
    @Override
    public void layoutContainer(Container parent) {
        // make sure that layout events occurring during layout are ignored
        // i.e. activated is set to false during layout calculation
        if (!activated)
            return;
        activated = false;

        //Change the LayoutStyle flag to ADJUST_SIZE if the width or height of the container is 0
        if (parent.getBounds().getWidth() == 0 || parent.getBounds().getHeight() == 0) {
            LayoutStyle = LayoutStyleType.ADJUST_SIZE;
        }

        // if the layout engine is set to fit the GUI to the given size,
        // then the given size is enforced by setting absolute positions for
        // Right and Bottom
        // (Default)
        if (LayoutStyle == LayoutStyleType.FIT_TO_SIZE) {
            layoutSpec.setRight(parent.getBounds().getWidth());
            layoutSpec.setBottom(parent.getBounds().getHeight());
        }

        layoutSpec.solve();

        // change the size of the GUI according to the calculated size
        // if the layout engine was configured to do so
        if (LayoutStyle == LayoutStyleType.ADJUST_SIZE) {
            parent.setSize(preferredLayoutSize(parent));
            LayoutStyle = LayoutStyleType.FIT_TO_SIZE;
        }
        // set the calculated positions and sizes for every area
        for (Map.Entry<Component, Area> entry : areaMap.entrySet()) {
            Component component = entry.getKey();
            Area area = entry.getValue();
            layoutComponent(component, area);
        }
        activated = true; // now layout calculation is allowed to run again
    }

    /**
     * Method not implemented
     */
    @Override
    public void addLayoutComponent(String name, Component comp) {
        throw new RuntimeException("Don't use this method for ALM");
    }

    /**
     * Return the minimum layout - Size
     *
     * @param parent the parent container of this layout spec.
     * @return The minimum size dimension.
     */
    @Override
    public Dimension minimumLayoutSize(Container parent) {
        Area.Size size = layoutSpec.getMinSize();
        return new Dimension((int) size.getWidth(), (int) size.getHeight());
	}

    /**
     * Return the prefer layout - Size
     *
     * @param parent the parent container of this layout spec.
     * @return The preferred size dimension.
     */
    @Override
	public Dimension preferredLayoutSize(Container parent) {
        Area.Size size = layoutSpec.getPreferredSize();
		return new Dimension((int) size.getWidth(), (int) size.getHeight());
	}

    @Override
    public void removeLayoutComponent(Component component) {
        areaMap.remove(component);
    }

    @Override
    public void addLayoutComponent(Component component, Object cookie) {
        if (!(cookie instanceof ALMLayout.LayoutParams))
            throw new RuntimeException("bad layout parameters");
        LayoutParams params = (LayoutParams)cookie;
        addComponent(component, params.left, params.top, params.right, params.bottom);
    }

    @Override
    public Dimension maximumLayoutSize(Container container) {
        throw new RuntimeException("implement me!");
    }

    @Override
    public float getLayoutAlignmentX(Container container) {
        return 0;
    }

    @Override
    public float getLayoutAlignmentY(Container container) {
        return 0;
    }

    @Override
    public void invalidateLayout(Container container) {
        for (Map.Entry<Component, Area> entry : areaMap.entrySet()) {
            Component component = entry.getKey();
            Area area = entry.getValue();
            updateArea(area, component);
        }
    }

    /**
     * Return the current layout spec
     *
     * @return LayoutSpec
     */
    public LayoutSpec getLayoutSpec() {
        return layoutSpec;
    }

    /**
     * Adds a new area to the specification, automatically setting preferred
     * size constraints.
     *
     * @param component the control which is the area content
     * @param left    left border
     * @param top     top border
     * @param right   right border
     * @param bottom  bottom border
     * @return the new area
     */
    private Area addComponent(Component component, XTab left, YTab top, XTab right, YTab bottom) {
        Area area = layoutSpec.addArea(left, top, right, bottom);

        updateArea(area, component);

        areaMap.put(component, area);
        return area;
    }

    private void updateArea(Area area, Component component) {
        Dimension minSize = component.getMinimumSize();
        Dimension prefSize = component.getPreferredSize();
        Dimension maxSize = component.getMaximumSize();

        area.setMinSize(minSize.getWidth(), minSize.getHeight());
        area.setPreferredSize(prefSize.getWidth(), prefSize.getHeight());
        area.setMaxSize(maxSize.getWidth(), maxSize.getHeight());
    }

    /**
     * Finds the area that contains the given control.
     *
     * @param component the control to look for
     * @return the area that contains the control
     */
    public Area areaOf(Component component) {
        return areaMap.get(component);
    }

    /**
     * Get the X-tab for the left border of the GUI
     *
     * @return X-tab for the left border of the GUI
     */
    public XTab getLeft() {
        return layoutSpec.getLeft();
    }

    /**
     * Get the X-tab for the right border of the GUI
     *
     * @return X-tab for the right border of the GUI
     */
    public XTab getRight() {
        return layoutSpec.getRight();
    }

    /**
     * Get the Y-tab for the top border of the GUI
     *
     * @return Y-tab for the top border of the GUI
     */
    public YTab getTop() {
        return layoutSpec.getTop();
    }

    /**
     * Get the Y-tab for the bottom border of the GUI
     *
     * @return Y-tab for the bottom border of the GUI
     */
    public YTab getBottom() {
        return layoutSpec.getBottom();
    }

    public void setSpacing(float horizontalSpacing, float verticalSpacing) {
        layoutSpec.setHorizontalSpacing(horizontalSpacing);
        layoutSpec.setVerticalSpacing(verticalSpacing);
    }

    public void setSpacing(float spacing) {
        setSpacing(spacing, spacing);
    }

    public void setInset(float left, float top, float right, float bottom) {
        layoutSpec.setLeftInset(left);
        layoutSpec.setTopInset(top);
        layoutSpec.setRightInset(right);
        layoutSpec.setBottomInset(bottom);
    }

}