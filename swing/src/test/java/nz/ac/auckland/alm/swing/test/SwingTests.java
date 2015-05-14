/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm.swing.test;

import junit.framework.TestCase;
import nz.ac.auckland.alm.XTab;
import nz.ac.auckland.alm.YTab;
import nz.ac.auckland.alm.swing.ALMLayout;

import javax.swing.*;


public class SwingTests extends TestCase {

    public void testThreeButtons() throws Exception {
        JDialog window = new JDialog();

        ALMLayout almLayout = new ALMLayout();

        XTab left = almLayout.getLeft();
        YTab top = almLayout.getTop();
        XTab right = almLayout.getRight();
        YTab bottom = almLayout.getBottom();

        XTab x1 = almLayout.addXTab();
        XTab x2 = almLayout.addXTab();

        almLayout.addArea(left, top, x1, bottom, new JButton("Button 1"));
        almLayout.addArea(x1, top, x2, bottom, new JButton("Button 2"));
        almLayout.addArea(x2, top, right, bottom, new JButton("Button 3"));

        window.setLayout(almLayout);
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);

        Thread.sleep(3000);
    }

    public void testPinWheelButtons() throws Exception {
        JDialog window = new JDialog();

        ALMLayout almLayout = new ALMLayout();

        XTab left = almLayout.getLeft();
        YTab top = almLayout.getTop();
        XTab right = almLayout.getRight();
        YTab bottom = almLayout.getBottom();

        XTab x1 = almLayout.addXTab();
        XTab x2 = almLayout.addXTab();

        YTab y1 = almLayout.addYTab();
        YTab y2 = almLayout.addYTab();

        almLayout.addArea(left, top, x2, y1, new JButton("Button 1"));
        almLayout.addArea(x2, top, right, y2, new JButton("Button 2"));
        almLayout.addArea(x1, y2, right, bottom, new JButton("Button 3"));
        almLayout.addArea(left, y1, x1, bottom, new JButton("Button 4"));
        almLayout.addArea(x1, y1, x2, y2, new JButton("Middle"));

        window.setLayout(almLayout);
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);

        Thread.sleep(3000);
    }
}
