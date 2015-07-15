/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm.algebra;

import junit.framework.TestCase;
import nz.ac.auckland.alm.LayoutSpec;
import nz.ac.auckland.alm.XTab;
import nz.ac.auckland.alm.YTab;


abstract public class BaseAlgebraTestCase extends TestCase {
    protected LayoutSpec getLayoutSpec(int width, int height) {
        LayoutSpec layoutSpec = new LayoutSpec();
        layoutSpec.getLeft().setValue(0);
        layoutSpec.getTop().setValue(0);
        layoutSpec.getRight().setValue(width);
        layoutSpec.getBottom().setValue(height);
        return layoutSpec;
    }

    protected XTab makeXTabAt(double value) {
        XTab tab = new XTab();
        tab.setValue(value);
        return tab;
    }

    protected YTab makeYTabAt(double value) {
        YTab tab = new YTab();
        tab.setValue(value);
        return tab;
    }
}
