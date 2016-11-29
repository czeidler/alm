/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm.swing.test;

import junit.framework.TestCase;
import nz.ac.auckland.alm.*;
import nz.ac.auckland.alm.swing.ALMLayout;
import nz.ac.auckland.linsolve.ForceSolver3;
import nz.ac.auckland.linsolve.LinearSolver;
import nz.ac.auckland.linsolve.SinglePatternLMS;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;


public class SwingTests extends TestCase {
    List<JDialog> dialogList = new ArrayList<JDialog>();

    private LinearSolver getSolver() {
        return new ForceSolver3();
    }

    private void addDialog(final JDialog dialog) {
        dialogList.add(dialog);

        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e);

                dialogList.remove(dialog);
                if (dialogList.size() == 0)
                    System.exit(0);
            }
        });
    }

    public void testThreeButtons() {
        JDialog dialog = new JDialog();
        addDialog(dialog);
        dialog.setTitle("Three Buttons");

        ALMLayout almLayout = new ALMLayout(getSolver());
        dialog.setLayout(almLayout);

        XTab left = almLayout.getLeft();
        YTab top = almLayout.getTop();
        XTab right = almLayout.getRight();
        YTab bottom = almLayout.getBottom();

        XTab x1 = new XTab();
        XTab x2 = new XTab();

        JButton button1 = new JButton("Button 1");
        JButton button2 = new JButton("Button 2");
        JButton button3 = new JButton("Button 3");
        button1.setMinimumSize(new Dimension(10, (int)button1.getMinimumSize().getHeight()));
        button2.setMinimumSize(new Dimension(200, (int)button2.getMinimumSize().getHeight()));
        button3.setMinimumSize(new Dimension(10, (int)button3.getMinimumSize().getHeight()));

        dialog.add(button1, new ALMLayout.LayoutParams(left, top, x1, bottom));
        dialog.add(button2, new ALMLayout.LayoutParams(x1, top, x2, bottom));
        dialog.add(button3, new ALMLayout.LayoutParams(x2, top, right, bottom));

        dialog.setMinimumSize(almLayout.minimumLayoutSize(dialog));
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);

        // test remove all and re-add
        dialog.getContentPane().removeAll();

        dialog.add(button1, new ALMLayout.LayoutParams(left, top, x1, bottom));
        dialog.add(button2, new ALMLayout.LayoutParams(x1, top, x2, bottom));
        dialog.add(button3, new ALMLayout.LayoutParams(x2, top, right, bottom));

        almLayout.areaOf(button1).setAlignment(HorizontalAlignment.FILL, VerticalAlignment.FILL);
        almLayout.areaOf(button2).setAlignment(HorizontalAlignment.FILL, VerticalAlignment.FILL);
        almLayout.areaOf(button3).setAlignment(HorizontalAlignment.FILL, VerticalAlignment.FILL);
    }

    public void testPinWheel() {
        JDialog dialog = new JDialog();
        addDialog(dialog);
        dialog.setTitle("Pin Wheel");

        ALMLayout almLayout = new ALMLayout(getSolver());
        dialog.setLayout(almLayout);

        XTab left = almLayout.getLeft();
        YTab top = almLayout.getTop();
        XTab right = almLayout.getRight();
        YTab bottom = almLayout.getBottom();

        XTab x1 = new XTab();
        XTab x2 = new XTab();

        YTab y1 = new YTab();
        YTab y2 = new YTab();

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

        // test removing and re-adding
        dialog.remove(buttonMiddle);
        dialog.invalidate();

        dialog.add(buttonMiddle, new ALMLayout.LayoutParams(x1, y1, x2, y2));
        dialog.invalidate();
    }

    public void testInsetsAndSpacing() {
        JDialog dialog = new JDialog();
        addDialog(dialog);
        dialog.setTitle("Insets And Spacing");

        ALMLayout almLayout = new ALMLayout(getSolver());
        dialog.setLayout(almLayout);

        almLayout.setSpacing(5, 10);
        almLayout.setInset(10, 20, 30, 40);

        XTab left = almLayout.getLeft();
        YTab top = almLayout.getTop();
        XTab right = almLayout.getRight();
        YTab bottom = almLayout.getBottom();

        XTab x1 = new XTab();

        YTab y1 = new YTab();

        JButton button1 = new JButton("Left Inset");
        JButton button2 = new JButton("Right Inset");
        JButton button3 = new JButton("Top Inset");
        JButton button4 = new JButton("Bottom Inset");

        dialog.add(button1, new ALMLayout.LayoutParams(left, top, x1, y1));
        dialog.add(button2, new ALMLayout.LayoutParams(x1, top, right, y1));
        dialog.add(button3, new ALMLayout.LayoutParams(left, y1, x1, bottom));
        dialog.add(button4, new ALMLayout.LayoutParams(x1, y1, right, bottom));

        Area area1 = almLayout.areaOf(button1);
        area1.setAlignment(HorizontalAlignment.FILL, VerticalAlignment.FILL);
        area1.setLeftInset(10);
        Area area2 = almLayout.areaOf(button2);
        area2.setAlignment(HorizontalAlignment.FILL, VerticalAlignment.FILL);
        area2.setRightInset(10);
        Area area3 = almLayout.areaOf(button3);
        area3.setAlignment(HorizontalAlignment.FILL, VerticalAlignment.FILL);
        area3.setTopInset(10);
        Area area4 = almLayout.areaOf(button4);
        area4.setAlignment(HorizontalAlignment.FILL, VerticalAlignment.FILL);
        area4.setBottomInset(10);

        dialog.setMinimumSize(almLayout.minimumLayoutSize(dialog));
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    public void testComplexButtons() {
        JDialog dialog = new JDialog();
        addDialog(dialog);
        dialog.setTitle("Complex Buttons");

        ALMLayout almLayout = new ALMLayout(getSolver());
        dialog.setLayout(almLayout);

        XTab left = almLayout.getLeft();
        YTab top = almLayout.getTop();
        XTab right = almLayout.getRight();
        YTab bottom = almLayout.getBottom();

        XTab x1 = new XTab();
        XTab x2 = new XTab();

        YTab y1 = new YTab();

        JButton button1 = new JButton("Button 1");
        JButton button2 = new JButton("Button 2");
        JButton button3 = new JButton("Button 3");

        JButton button4 = new JButton("Button 4");
        JButton button5 = new JButton("Button 5");

        dialog.add(button1, new ALMLayout.LayoutParams(left, top, x1, y1));
        dialog.add(button2, new ALMLayout.LayoutParams(x1, top, x2, y1));
        dialog.add(button3, new ALMLayout.LayoutParams(x2, top, right, y1));

        dialog.add(button4, new ALMLayout.LayoutParams(left, y1, x2, bottom));
        dialog.add(button5, new ALMLayout.LayoutParams(x2, y1, right, bottom));

        dialog.setMinimumSize(almLayout.minimumLayoutSize(dialog));
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);

        almLayout.areaOf(button1).setAlignment(HorizontalAlignment.FILL, VerticalAlignment.FILL);
        almLayout.areaOf(button2).setAlignment(HorizontalAlignment.FILL, VerticalAlignment.FILL);
        almLayout.areaOf(button3).setAlignment(HorizontalAlignment.FILL, VerticalAlignment.FILL);
        almLayout.areaOf(button4).setAlignment(HorizontalAlignment.FILL, VerticalAlignment.FILL);
        almLayout.areaOf(button5).setAlignment(HorizontalAlignment.FILL, VerticalAlignment.FILL);
    }
/*
    private void addFlowLayout(JDialog dialog, XTab left, YTab top, XTab right, YTab bottom) {
        ALMLayout layout = (ALMLayout)dialog.getContentPane().getLayout();

        final JPanel flowPanel = new JPanel();
        flowPanel.setLayout(new FlowLayout());
        flowPanel.add(new JButton("Flow 1"));
        flowPanel.add(new JButton("Flow 2"));
        flowPanel.add(new JButton("Flow 3"));
        flowPanel.add(new JButton("Flow 4"));

        dialog.add(flowPanel, new ALMLayout.LayoutParams(left, top, right, bottom));

        Area flowArea = layout.areaOf(flowPanel);
        Area.IDynamicSize minDynamicSize = new Area.IDynamicSize() {
            @Override
            public Area.Size getSize(double left, double top, double right, double bottom) {
                int width = (int) (right - left);
                int height = (int) (bottom - top);
                //flowPanel.setBounds((int) left, (int) top, width, height);
                //Dimension dimension = flowPanel.getLayout().preferredLayoutSize(flowPanel);
                //return new Area.Size(dimension.getWidth(), dimension.getHeight());

                Area.Size prefSize;

                if (width >= 341 && height >=35) // 11935
                    prefSize = new Area.Size(341, 35);
                else if (width >= 100 && height >= 140)//14000
                    prefSize = new Area.Size(100, 140);
                else if (width >= 260 && height >= 70) // 18200
                    prefSize = new Area.Size(260, 70);
                else //if (width >= 180 && height >= 105)//18900
                    prefSize = new Area.Size(180, 105);

               return prefSize;

                //return new Area.Size((int) (right - left), (int) (bottom - top));
            }
        };

        Area.IDynamicSize dynamicSize = new Area.IDynamicSize() {
            @Override
            public Area.Size getSize(double left, double top, double right, double bottom) {
                int width = (int) (right - left);
                int height = (int) (bottom - top);
                //flowPanel.setBounds((int) left, (int) top, width, height);
                //Dimension dimension = flowPanel.getLayout().preferredLayoutSize(flowPanel);
                //return new Area.Size(dimension.getWidth(), dimension.getHeight());

                Area.Size prefSize;
                if (width >= 341)
                    prefSize = new Area.Size(341, 35);
                else if (width > 260)
                    prefSize = new Area.Size(260, 70);
                else if (width > 180)
                    prefSize = new Area.Size(180, 105);
                else
                    prefSize = new Area.Size(100, 140);
                flowPanel.setMaximumSize(new Dimension((int) prefSize.getWidth(), (int) prefSize.getHeight()));
                //flowPanel.setMinimumSize(new Dimension((int) prefSize.getWidth(), (int) prefSize.getHeight()));
                //flowPanel.setMinimumSize(new Dimension(100, 35));
                return prefSize;

                //return new Area.Size((int) (right - left), (int) (bottom - top));
            }
        };
        flowArea.setPreferredSize(dynamicSize);
        flowArea.setMinSize(minDynamicSize);
        //flowPanel.setMinimumSize(new Dimension(100, 140));
        //flowPanel.setMaximumSize(new Dimension(100, 35));
    }

    public void testFlowPinWheel() {
        JDialog dialog = new JDialog();
        addDialog(dialog);
        dialog.setTitle("Flow Pin Wheel");

        ALMLayout almLayout = new ALMLayout(getSolver());
        dialog.setLayout(almLayout);

        XTab left = almLayout.getLeft();
        YTab top = almLayout.getTop();
        XTab right = almLayout.getRight();
        YTab bottom = almLayout.getBottom();

        XTab x1 = new XTab();
        XTab x2 = new XTab();

        YTab y1 = new YTab();
        YTab y2 = new YTab();

        addFlowLayout(dialog, left, top, x2, y1);
        addFlowLayout(dialog, left, top, x2, y1);
        addFlowLayout(dialog, x2, top, right, y2);
        addFlowLayout(dialog, x1, y2, right, bottom);
        addFlowLayout(dialog, left, y1, x1, bottom);
        addFlowLayout(dialog, x1, y1, x2, y2);

        //dialog.setMinimumSize(almLayout.minimumLayoutSize(dialog));
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    public void testFlowTwoButtons() {
        JDialog dialog = new JDialog();
        addDialog(dialog);
        dialog.setTitle("Flow Two Buttons");

        ALMLayout almLayout = new ALMLayout(getSolver());
        dialog.setLayout(almLayout);

        XTab left = almLayout.getLeft();
        YTab top = almLayout.getTop();
        XTab right = almLayout.getRight();
        YTab bottom = almLayout.getBottom();

        XTab x1 = new XTab();

        addFlowLayout(dialog, left, top, x1, bottom);
        addFlowLayout(dialog, x1, top, right, bottom);

        //dialog.setMinimumSize(almLayout.minimumLayoutSize(dialog));
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    public void testFlowLayout() {
        JDialog dialog = new JDialog();
        addDialog(dialog);
        dialog.setTitle("Flow Layout");

        ALMLayout almLayout = new ALMLayout(getSolver());
        dialog.setLayout(almLayout);

        XTab left = almLayout.getLeft();
        YTab top = almLayout.getTop();
        XTab right = almLayout.getRight();
        YTab bottom = almLayout.getBottom();

        YTab y1 = new YTab();

        JButton button1 = new JButton("Button 1");

        addFlowLayout(dialog, left, top, right, y1);
        dialog.add(button1, new ALMLayout.LayoutParams(left, y1, right, bottom));

        button1.setMinimumSize(new Dimension(50, 25));
        button1.setPreferredSize(new Dimension(100, 100));
        button1.setMaximumSize(new Dimension(50000, 50000));

        //dialog.setMinimumSize(almLayout.minimumLayoutSize(dialog));
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }
    */

    public void testNormalFlowLayout() {
        JDialog dialog = new JDialog();
        addDialog(dialog);
        dialog.setTitle("Normal Flow Layout");

        final JPanel flowPanel = new JPanel();
        flowPanel.setLayout(new FlowLayout());
        flowPanel.add(new JButton("Flow 1"));
        flowPanel.add(new JButton("Flow 2"));
        flowPanel.add(new JButton("Flow 3"));
        flowPanel.add(new JButton("Flow 4"));

        JButton button1 = new JButton("Button 1");

        GridLayout mainLayout = new GridLayout(2, 1);
        dialog.setLayout(mainLayout);

        flowPanel.setMinimumSize(new Dimension(10, 10));
        //flowPanel.setMaximumSize(new Dimension(100, 35));

        button1.setMinimumSize(new Dimension(50, 25));
        button1.setPreferredSize(new Dimension(50000, 50000));
        button1.setMaximumSize(new Dimension(50000, 50000));

        dialog.add(flowPanel);
        dialog.add(button1);

        //dialog.setMinimumSize(almLayout.minimumLayoutSize(dialog));
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    public void testMaxSizes() {
        JDialog dialog = new JDialog();
        addDialog(dialog);
        dialog.setTitle("Max Sizes");

        ALMLayout almLayout = new ALMLayout(getSolver());
        dialog.setLayout(almLayout);

        XTab left = almLayout.getLeft();
        YTab top = almLayout.getTop();
        XTab right = almLayout.getRight();
        YTab bottom = almLayout.getBottom();

        XTab x1 = new XTab();

        JButton button1 = new JButton("Button 1");
        JButton button2 = new JButton("Button 2");

        dialog.add(button1, new ALMLayout.LayoutParams(left, top, x1, bottom));
        dialog.add(button2, new ALMLayout.LayoutParams(x1, top, right, bottom));

        button2.setMaximumSize(new Dimension(50000, 50000));

        almLayout.areaOf(button1).setAlignment(HorizontalAlignment.FILL, VerticalAlignment.FILL);
        almLayout.areaOf(button2).setAlignment(HorizontalAlignment.FILL, VerticalAlignment.FILL);

        dialog.setMinimumSize(almLayout.minimumLayoutSize(dialog));
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    public void testSingleButton() {
        JDialog dialog = new JDialog();
        addDialog(dialog);
        dialog.setTitle("Single Button");

        ALMLayout almLayout = new ALMLayout(getSolver());
        dialog.setLayout(almLayout);

        XTab left = almLayout.getLeft();
        YTab top = almLayout.getTop();
        XTab right = almLayout.getRight();
        YTab bottom = almLayout.getBottom();

        JButton button1 = new JButton("Button 1");

        dialog.add(button1, new ALMLayout.LayoutParams(left, top, right, bottom));

        almLayout.areaOf(button1).setAlignment(HorizontalAlignment.FILL, VerticalAlignment.FILL);

        dialog.setMinimumSize(almLayout.minimumLayoutSize(dialog));
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }


    private JButton subOptimalButton() {
        JButton button = new JButton("Button");
        button.setMinimumSize(new Dimension(200, 70));
        button.setPreferredSize(new Dimension(200, 70));
        button.setMaximumSize(new Dimension(50000, 50000));
        return button;
    }

    public void testSubOptimal() {
        JDialog dialog = new JDialog();
        addDialog(dialog);
        dialog.setTitle("Sub optimal test case");

        ALMLayout almLayout = new ALMLayout(getSolver());
        dialog.setLayout(almLayout);

        XTab left = almLayout.getLeft();
        YTab top = almLayout.getTop();
        XTab right = almLayout.getRight();
        YTab bottom = almLayout.getBottom();

        YTab y1 = new YTab();
        dialog.add(subOptimalButton(), new ALMLayout.LayoutParams(left, top, right, y1));
        YTab y2 = new YTab();
        dialog.add(subOptimalButton(), new ALMLayout.LayoutParams(left, y1, right, y2));
        YTab y3 = new YTab();
        dialog.add(subOptimalButton(), new ALMLayout.LayoutParams(left, y2, right, y3));
        YTab y4 = new YTab();
        dialog.add(subOptimalButton(), new ALMLayout.LayoutParams(left, y3, right, y4));
        YTab y5 = new YTab();
        dialog.add(subOptimalButton(), new ALMLayout.LayoutParams(left, y4, right, y5));
        YTab y6 = new YTab();
        dialog.add(subOptimalButton(), new ALMLayout.LayoutParams(left, y5, right, y6));
        YTab y7 = new YTab();
        dialog.add(subOptimalButton(), new ALMLayout.LayoutParams(left, y6, right, y7));
        YTab y8 = new YTab();
        dialog.add(subOptimalButton(), new ALMLayout.LayoutParams(left, y7, right, y8));
        YTab y9 = new YTab();
        dialog.add(subOptimalButton(), new ALMLayout.LayoutParams(left, y8, right, y9));
        dialog.add(subOptimalButton(), new ALMLayout.LayoutParams(left, y9, right, bottom));

        //dialog.setMinimumSize(almLayout.minimumLayoutSize(dialog));
        dialog.setSize(1200, 800);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    private void randomButtonSplit(JDialog dialog, XTab left, YTab top, XTab right, YTab bottom, int nButtons,
                                   boolean horizontal) {
        ALMLayout layout = (ALMLayout)dialog.getContentPane().getLayout();
        double width =  right.getValue() - left.getValue();
        double height = bottom.getValue() - top.getValue();
        if (nButtons == 1) {
            JButton button1 = new JButton("B " + (dialog.getContentPane().getComponentCount() + 1));

            dialog.add(button1, new ALMLayout.LayoutParams(left, top, right, bottom));

            layout.areaOf(button1).setAlignment(HorizontalAlignment.FILL, VerticalAlignment.FILL);
            button1.setMinimumSize(new Dimension(10, 10));
            button1.setPreferredSize(new Dimension((int) (width), (int) (height)));
            button1.setMaximumSize(new Dimension(10000, 10000));
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
            randomButtonSplit(dialog, left, top, tab, bottom, half, false);
            randomButtonSplit(dialog, tab, top, right, bottom, nButtons - half, false);
        } else {
            // vertical
            YTab tab = new YTab();
            tab.setValue(top.getValue() + height * splitPoint);
            randomButtonSplit(dialog, left, top, right, tab, half, true);
            randomButtonSplit(dialog, left, tab, right, bottom, nButtons - half, true);
        }
    }

    public void testRandomButtons(int nButtons) {
        JDialog dialog = new JDialog();
        addDialog(dialog);
        dialog.setTitle("Random Buttons");

        ALMLayout almLayout = new ALMLayout(getSolver());
        dialog.setLayout(almLayout);

        XTab left = almLayout.getLeft();
        left.setValue(0);
        YTab top = almLayout.getTop();
        top.setValue(0);
        XTab right = almLayout.getRight();
        right.setValue(50);
        YTab bottom = almLayout.getBottom();
        bottom.setValue(50);

        randomButtonSplit(dialog, left, top, right, bottom, nButtons, true);

        //dialog.setMinimumSize(almLayout.minimumLayoutSize(dialog));
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    static public void testMain() throws InterruptedException {
        SwingTests swingTests = new SwingTests();
        //swingTests.testSingleButton();
        swingTests.testThreeButtons();
        /*swingTests.testPinWheel();
        swingTests.testInsetsAndSpacing();
        swingTests.testComplexButtons();*/
        //swingTests.testFlowLayout();
        //swingTests.testFlowPinWheel();
        //swingTests.testFlowTwoButtons();
        //swingTests.testNormalFlowLayout();
        //swingTests.testMaxSizes();
        //swingTests.testSubOptimal();
        //swingTests.testRandomButtons(50);
        Thread.sleep(10000000l);
    }
}