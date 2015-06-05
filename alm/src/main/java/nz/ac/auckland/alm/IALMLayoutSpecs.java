/*
 * Copyright 2015.
 * Distributed under the terms of the LGPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm;

import nz.ac.auckland.linsolve.Constraint;

import java.util.List;


public interface IALMLayoutSpecs {
    /**
     * Get the X-tab for the left border of the GUI
     *
     * @return X-tab for the left border of the GUI
     */
    XTab getLeftTab();

    /**
     * Get the X-tab for the right border of the GUI
     *
     * @return X-tab for the right border of the GUI
     */
    XTab getRightTab();

    /**
     * Get the Y-tab for the top border of the GUI
     *
     * @return Y-tab for the top border of the GUI
     */
    YTab getTopTab();

    /**
     * Get the Y-tab for the bottom border of the GUI
     *
     * @return Y-tab for the bottom border of the GUI
     */
    YTab getBottomTab();

    Area getArea(Object object);
    List<Area> getAreas();
    List<Constraint> getCustomConstraints();
}
