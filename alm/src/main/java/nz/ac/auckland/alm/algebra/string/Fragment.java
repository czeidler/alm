/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm.algebra.string;

import nz.ac.auckland.alm.IArea;
import nz.ac.auckland.alm.TabArea;
import nz.ac.auckland.alm.XTab;
import nz.ac.auckland.alm.YTab;
import nz.ac.auckland.alm.algebra.*;
import nz.ac.auckland.linsolve.Variable;


import java.util.ArrayList;
import java.util.List;


public class Fragment<Tab extends Variable, OrthTab extends Variable> extends TabArea {
    final List<IArea> items = new ArrayList<IArea>();
    IDirection<Tab, OrthTab> direction;

    static final public IDirection horizontalDirection = new RightDirection();
    static final public IDirection verticalDirection = new BottomDirection();

    static public Fragment horizontalFragment(IArea area1, IArea area2) {
        return new Fragment(area1, area2, horizontalDirection);
    }

    static public Fragment verticalFragment(IArea area1, IArea area2) {
        return new Fragment(area1, area2, verticalDirection);
    }

    public Fragment() {

    }

    public Fragment(IArea area1, IArea area2, IDirection<Tab, OrthTab> direction) {
        assert direction == horizontalDirection || direction == verticalDirection;
        this.direction = direction;

        add(area1);
        if  (area2 != null)
            add(area2);
    }

    @Override
    public String toString() {
        return StringWriter.write(this);
    }

    public String hash() {
        String hash = "";
        if (direction != null)
            hash += direction.getClass().getSimpleName();
        for (IArea item : getItems()) {
            if (item instanceof Fragment)
                hash += "(" + ((Fragment) item).hash() + ")";
            else
                hash += item.hashCode();
        }
        return hash;
    }

    public void setHorizontalDirection() {
        this.direction = horizontalDirection;
    }

    public void setVerticalDirection() {
        this.direction = verticalDirection;
    }

    public List<IArea> getItems() {
        return items;
    }

    public void add(IArea item) {
        if (item instanceof Fragment) {
            Fragment fragment = (Fragment) item;
            if (fragment.direction == null || fragment.direction == direction) {
                for (Object subItem : fragment.getItems())
                    add((IArea)subItem);
                return;
            }
        }
        items.add(item);
    }

    public int countAtoms() {
        int count = 0;
        for (IArea item : items) {
            if (item instanceof Fragment)
                count += ((Fragment) item).countAtoms();
            else
                count++;
        }
        return count;
    }

    private <Tab extends Variable, OrthTab extends Variable> Tab getTab1(IDirection<Tab, OrthTab> direction) {
        Tab tab = direction.getTab(items.get(0));
        if (tab == null)
            return null;
        for (int i = 1; i < items.size(); i++) {
            Tab areaTab = direction.getTab(items.get(i));
            if (areaTab == null || tab != areaTab)
                return null;
        }
        return tab;
    }

    private <Tab extends Variable, OrthTab extends Variable> Tab getTab2(IDirection<Tab, OrthTab> direction) {
        Tab tab = direction.getTab(items.get(items.size() - 1));
        if (tab == null)
            return null;
        for (int i = 0; i < items.size() - 1; i++) {
            Tab areaTab = direction.getTab(items.get(i));
            if (areaTab == null || tab != areaTab)
                return null;
        }
        return tab;
    }

    @Override
    public XTab getLeft() {
        if (items.size() == 0)
            return null;

        if (direction == horizontalDirection)
            return items.get(0).getLeft();
        return getTab1(new LeftDirection());
    }

    @Override
    public YTab getTop() {
        if (items.size() == 0)
            return null;

        if (direction == verticalDirection)
            return items.get(0).getTop();
        return getTab1(new TopDirection());
    }

    @Override
    public XTab getRight() {
        if (items.size() == 0)
            return null;

        if (direction == horizontalDirection)
            return items.get(items.size() - 1).getRight();
        return getTab2(new RightDirection());
    }

    @Override
    public YTab getBottom() {
        if (items.size() == 0)
            return null;

        if (direction == verticalDirection)
            return items.get(items.size() - 1).getBottom();
        return getTab2(new BottomDirection());
    }

    @Override
    public void setLeft(XTab value) {
        if (items.size() == 0 || value == null)
            return;
        if (direction == horizontalDirection)
            items.get(0).setLeft(value);
        else {
            for (IArea area : items)
                area.setLeft(value);
        }
    }

    @Override
    public void setRight(XTab value) {
        if (items.size() == 0 || value == null)
            return;
        if (direction == horizontalDirection)
            items.get(items.size() - 1).setRight(value);
        else {
            for (IArea area : items)
                area.setRight(value);
        }
    }

    @Override
    public void setTop(YTab value) {
        if (items.size() == 0 || value == null)
            return;
        if (direction == verticalDirection)
            items.get(0).setTop(value);
        else {
            for (IArea area : items)
                area.setTop(value);
        }
    }

    @Override
    public void setBottom(YTab value) {
        if (items.size() == 0 || value == null)
            return;
        if (direction == verticalDirection)
            items.get(items.size() - 1).setBottom(value);
        else {
            for (IArea area : items)
                area.setBottom(value);
        }
    }
}
