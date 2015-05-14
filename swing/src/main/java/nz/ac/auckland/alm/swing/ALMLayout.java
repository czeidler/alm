package nz.ac.auckland.alm.swing;

import nz.ac.auckland.alm.*;
import nz.ac.auckland.linsolve.AbstractLinearSolver;

import javax.swing.*;
import java.awt.*;
import java.awt.Dimension;
import java.util.*;
import java.util.List;


/**
 * A GUI layout engine using the ALM.
 *
 * @author Christof
 * @author Ted
 * @author Sarah
 * @author Clemens clemens.zeidler@aucklanduni.ac.nz
 */
public class ALMLayout implements LayoutManager {
    private boolean activated = true;

    /** The specification used for calculating the layout. */
    private LayoutSpec layoutSpec;

    final List<JComponent> componentsToAdd = new ArrayList<JComponent>();
    final Map<JComponent, Area> areaMap = new HashMap<JComponent, Area>();

    /**
     * The manner in which the GUI is dynamically adjusted. The default is to
     * fit the child controls into their parent.
     */
    private LayoutStyleType LayoutStyle = LayoutStyleType.FIT_TO_SIZE;

    /** Creates a new layout engine with an empty specification. */
    public ALMLayout() {
        super();
        initLayout();
    }

    /** Creates a new layout engine with a specific solver. */
    public ALMLayout(AbstractLinearSolver solver) {
        super();
        initLayout(solver);
    }

    /**
     * Creates a new layout engine with the given specification.
     *
     * @param ls the layout specification
     */
    public ALMLayout(LayoutSpec ls) {
        this();
        layoutSpec = ls;
    }

    /**
     * Initializes the layout.
     */
    private void initLayout() {
        layoutSpec = new LayoutSpec();
    }

    /**
     * Initializes the layout with a specific solver
     */
    private void initLayout(AbstractLinearSolver solver) {
        layoutSpec = new LayoutSpec(solver);
    }

    /**
     * Calculate and set the layout. If no layout specification is given, a
     * specification is reverse engineered automatically.
     *
     * @param parent the parent control of the controls in the layout
     */
    void layout(Container parent) {
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
        for (Map.Entry<JComponent, Area> entry : areaMap.entrySet()) {
            JComponent component = entry.getKey();
            Area area = entry.getValue();
            doLayout(component, area);
        }
        activated = true; // now layout calculation is allowed to run again
    }

    /**
     * Place the area content accordantly to the solved layout specifications.
     * You should never need to call it by your self.
     */
	void doLayout(JComponent component, Area area) {
		// set content location and size
		component.setLocation(new Point((int) Math.round(area.getLeft().getValue()),
                (int) Math.round(area.getTop().getValue())));
		int width = (int) Math.round(area.getRight().getValue() - area.getLeft().getValue());
		int height = (int) Math.round(area.getBottom().getValue() - area.getTop().getValue());
		component.setSize(width, height);
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
        try {
            for (Component c : componentsToAdd) {
                // make sure container's parent is not added to itself
                if (c != parent) {
                    parent.add(c);
                }
            }
            componentsToAdd.clear();
        } catch (NullPointerException ne) {
            System.err.println("Setting layout container failure");
            System.err.println("> Method: LayoutContainer");
            System.err.println("> Class: ALMLayout");
            ne.printStackTrace();
        }
        try {
            layout(parent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method not implemented
     */
    @Override
    public void addLayoutComponent(String name, Component comp) {
        throw new RuntimeException("Don't use this method for ALM");
    }

    /**
     * Return the minimum layout - Dimension
     *
     * @param parent the parent container of this layout spec.
     * @return The minimum size dimension.
     */
    @Override
    public Dimension minimumLayoutSize(Container parent) {
        nz.ac.auckland.alm.Dimension dimension = layoutSpec.getMinSize();
        return new Dimension((int)dimension.getWidth(), (int)dimension.getHeight());
	}

    /**
     * Return the prefer layout - Dimension
     *
     * @param parent the parent container of this layout spec.
     * @return The preferred size dimension.
     */
    @Override
	public Dimension preferredLayoutSize(Container parent) {
        nz.ac.auckland.alm.Dimension dimension = layoutSpec.getPreferredSize();
		return new Dimension((int)dimension.getWidth(), (int)dimension.getHeight());
	}

    @Override
    public void removeLayoutComponent(Component component) {
        Area area = areaMap.remove(component);
        if (area == null)
            return;
        component.getParent().remove(component);
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
     * Sets the current layout spec
     *
     * @param ls The layoutspec to be used.
     */
    public void setLayoutSpec(LayoutSpec ls) {
        layoutSpec = ls;
    }


    /**
     * Adds a new area to the specification, automatically setting preferred
     * size constraints.
     *
     * @param left    left border
     * @param top     top border
     * @param right   right border
     * @param bottom  bottom border
     * @param content the control which is the area content
     * @return the new area
     */
    public Area addArea(XTab left, YTab top, XTab right, YTab bottom,
                        JComponent content) {
        componentsToAdd.add(content);
        Area area = layoutSpec.addArea(left, top, right, bottom);

        Dimension minSize = content.getMinimumSize();
        Dimension prefSize = content.getPreferredSize();
        Dimension maxSize = content.getMaximumSize();

        area.setMinContentSize(minSize.getWidth(), minSize.getHeight());
        area.setPreferredContentSize(prefSize.getWidth(), prefSize.getHeight());
        area.setMaxContentSize(maxSize.getWidth(), maxSize.getHeight());

        areaMap.put(content, area);
        return area;
    }

    /**
     * Adds a new x-tab to the specification.
     *
     * @return the new x-tab
     */
    public XTab addXTab() {
        return layoutSpec.addXTab();
    }

    /**
     * Adds a new x-tab to the specification.
     *
     * @param name String define the name of resulting x-tab
     * @return the new x-tab
     */
    public XTab addXTab(String name) {
        return layoutSpec.addXTab(name);
    }

    /**
     * Adds a new y-tab to the specification.
     *
     * @return the new y-tab
     */
    public YTab addYTab() {
        return layoutSpec.addYTab();
    }

    /**
     * Adds a new y-tab to the specification.
     *
     * @param name String define the name of resulting y-tab
     * @return the new y-tab
     */
    public YTab addYTab(String name) {
        return layoutSpec.addYTab(name);
    }

    /**
     * Finds the area that contains the given control.
     *
     * @param component the control to look for
     * @return the area that contains the control
     */
    public Area areaOf(JComponent component) {
        return areaMap.remove(component);
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


}