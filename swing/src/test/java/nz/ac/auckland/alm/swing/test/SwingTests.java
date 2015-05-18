/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm.swing.test;

import nz.ac.auckland.alm.*;
import nz.ac.auckland.alm.swing.ALMLayout;

import javax.swing.*;


public class SwingTests {
    static public void testThreeButtons() {
        JDialog dialog = new JDialog();
        ALMLayout almLayout = new ALMLayout();
        dialog.setLayout(almLayout);

        XTab left = almLayout.getLeft();
        YTab top = almLayout.getTop();
        XTab right = almLayout.getRight();
        YTab bottom = almLayout.getBottom();

        XTab x1 = almLayout.addXTab();
        XTab x2 = almLayout.addXTab();

        JButton button1 = new JButton("Button 1");
        JButton button2 = new JButton("Button 2");
        JButton button3 = new JButton("Button 3");

        dialog.add(button1, new ALMLayout.LayoutParams(left, top, x1, bottom));
        dialog.add(button2, new ALMLayout.LayoutParams(x1, top, x2, bottom));
        dialog.add(button3, new ALMLayout.LayoutParams(x2, top, right, bottom));

        almLayout.areaOf(button1).setAlignment(HorizontalAlignment.FILL, VerticalAlignment.FILL);
        almLayout.areaOf(button2).setAlignment(HorizontalAlignment.FILL, VerticalAlignment.FILL);
        almLayout.areaOf(button3).setAlignment(HorizontalAlignment.FILL, VerticalAlignment.FILL);

        dialog.setMinimumSize(almLayout.minimumLayoutSize(dialog));
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    static public void testPinWheel() {
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

        JButton button1 = new JButton("Button 1");
        JButton button2 = new JButton("Button 2");
        JButton button3 = new JButton("Button 3");
        JButton button4 = new JButton("Button 4");
        JButton buttonMiddle = new JButton("Middle");

        dialog.add(button1, new ALMLayout.LayoutParams(left, top, x2, y1));
        dialog.add(button2, new ALMLayout.LayoutParams(x2, top, right, y2));
        dialog.add(button3, new ALMLayout.LayoutParams(x1, y2, right, bottom));
        dialog.add(button4, new ALMLayout.LayoutParams(left, y1, x1, bottom));
        dialog.add(buttonMiddle, new ALMLayout.LayoutParams(x1, y1, x2, y2));

        almLayout.areaOf(button1).setAlignment(HorizontalAlignment.FILL, VerticalAlignment.FILL);
        almLayout.areaOf(button2).setAlignment(HorizontalAlignment.FILL, VerticalAlignment.FILL);
        almLayout.areaOf(button3).setAlignment(HorizontalAlignment.FILL, VerticalAlignment.FILL);
        almLayout.areaOf(button4).setAlignment(HorizontalAlignment.FILL, VerticalAlignment.FILL);
        almLayout.areaOf(buttonMiddle).setAlignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

        dialog.setMinimumSize(almLayout.minimumLayoutSize(dialog));
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    public static void main(String[] args) {
        testThreeButtons();
        testPinWheel();
    }
}
