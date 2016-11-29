/*
 * Copyright 2016.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm.algebra;

import nz.ac.auckland.alm.IArea;
import nz.ac.auckland.alm.TabArea;
import nz.ac.auckland.alm.XTab;
import nz.ac.auckland.alm.YTab;

import java.util.ArrayList;
import java.util.List;


public class AlgebraLayoutSpec extends TabArea {
    final private List<TabArea> elements = new ArrayList<TabArea>();

    public List<TabArea> getElements() {
        return elements;
    }

    public void add(TabArea element) {
        elements.add(element);
    }

    @Override
    public void setLeft(XTab value) {
        XTab current = getLeft();
        for (IArea area : elements) {
            if (area.getLeft() == current)
                area.setLeft(value);
        }
        super.setLeft(value);
    }

    @Override
    public void setTop(YTab value) {
        YTab current = getTop();
        for (IArea area : elements) {
            if (area.getTop() == current)
                area.setTop(value);
        }
        super.setTop(value);
    }

    @Override
    public void setRight(XTab value) {
        XTab current = getRight();
        for (IArea area : elements) {
            if (area.getRight() == current)
                area.setRight(value);
        }
        super.setRight(value);
    }

    @Override
    public void setBottom(YTab value) {
        YTab current = getBottom();
        for (IArea area : elements) {
            if (area.getBottom() == current)
                area.setBottom(value);
        }
        super.setBottom(value);
    }
}
