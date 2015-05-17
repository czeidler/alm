/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm.swing.test;

import nz.ac.auckland.alm.XTab;
import nz.ac.auckland.alm.YTab;
import nz.ac.auckland.alm.swing.ALMLayout;

import javax.swing.*;


public class SwingTests {
    static public void testThreeButtons() throws Exception {
        JDialog dialog = new JDialog();
        ALMLayout almLayout = new ALMLayout();
        dialog.setLayout(almLayout);

        XTab left = almLayout.getLeft();
        YTab top = almLayout.getTop();
        XTab right = almLayout.getRight();
        YTab bottom = almLayout.getBottom();

        XTab x1 = almLayout.addXTab();
        XTab x2 = almLayout.addXTab();

        dialog.add(new JButton("Button 1"), new ALMLayout.LayoutParams(left, top, x1, bottom));
        dialog.add(new JButton("Button 2"), new ALMLayout.LayoutParams(x1, top, x2, bottom));
        dialog.add(new JButton("Button 3"), new ALMLayout.LayoutParams(x2, top, right, bottom));

        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    static public void testPinWheel() throws Exception {
        JDialog dialog = new JDialog();
        ALMLayout almLayout = new ALMLayout();
        dialog.setLayout(almLayout);


        XTab left = almLayout.getLeft();
        YTab top = almLayout.getTop();
        XTab right = almLayout.getRight();
        YTab bottom = almLayout.getBottom();

        XTab x1 = almLayout.addXTab();
        XTab x2 = almLayout.addXTab();

        YTab y1 = almLayout.addYTab();
        YTab y2 = almLayout.addYTab();

        dialog.add(new JButton("Button 1"), new ALMLayout.LayoutParams(left, top, x2, y1));
        dialog.add(new JButton("Button 2"), new ALMLayout.LayoutParams(x2, top, right, y2));
        dialog.add(new JButton("Button 3"), new ALMLayout.LayoutParams(x1, y2, right, bottom));
        dialog.add(new JButton("Button 4"), new ALMLayout.LayoutParams(left, y1, x1, bottom));
        dialog.add(new JButton("Middle"), new ALMLayout.LayoutParams(x1, y1, x2, y2));

        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    public static void main(String[] args) {
        try {
            testThreeButtons();
            testPinWheel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
