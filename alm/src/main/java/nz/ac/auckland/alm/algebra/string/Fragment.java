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

    static public Fragment horizontalTerm(IArea area1, IArea area2) {
        return new Fragment(area1, area2, horizontalDirection);
    }

    static public Fragment verticalTerm(IArea area1, IArea area2) {
        return new Fragment(area1, area2, verticalDirection);
    }

    public Fragment() {

    }

    private Fragment(IArea area1, IArea area2, IDirection<Tab, OrthTab> direction1) {
        this.direction = direction1;

        add(area1);
        if  (area2 != null)
            add(area2);
    }

    public void setHorizontalDirection() {
        this.direction = horizontalDirection;
        if (items.size() > 0)
            setFirstItem(items.get(0));
    }

    public void setVerticalDirection() {
        this.direction = verticalDirection;
        if (items.size() > 0)
            setFirstItem(items.get(0));
    }

    public boolean hasSubTerm(Fragment subFragment) {
        for (IArea area : items) {
            if (!(area instanceof Fragment))
                continue;
            Fragment fragment = (Fragment) area;
            if (fragment == subFragment)
                return true;
            if (fragment.hasSubTerm(subFragment))
                return true;
        }
        return false;
    }

    public boolean hasAtom(IArea atom) {
        for (IArea area : items) {
            if (!(area instanceof Fragment)) {
                if (area == atom)
                    return true;
            } else {
                Fragment fragment = (Fragment) area;
                if (fragment.hasAtom(atom))
                    return true;
            }
        }
        return false;
    }

    private void setFirstItem(IArea item) {
        if (direction == null)
            return;
        direction.setOppositeTab(this, direction.getOppositeTab(item));
        direction.setOrthogonalTab1(this, direction.getOrthogonalTab1(item));
        direction.setOrthogonalTab2(this, direction.getOrthogonalTab2(item));
    }

    public List<IArea> getItems() {
        return items;
    }

    public void add(IArea item) {
        if (items.size() == 0)
            setFirstItem(item);

        if (item instanceof Fragment) {
            Fragment fragment = (Fragment) item;
            if (fragment.direction == null || fragment.direction == direction) {
                for (Object subItem : fragment.getItems())
                    add((IArea)subItem);
                return;
            }
        }
        items.add(item);
        if (items.size() > 1) {
            direction.setTab(this, direction.getTab(item));
            if (direction.getOrthogonalTab1(this) != direction.getOrthogonalTab1(item))
                direction.setOrthogonalTab1(this, null);
            if (direction.getOrthogonalTab2(this) != direction.getOrthogonalTab2(item))
                direction.setOrthogonalTab2(this, null);
        }
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

    @Override
    public void setLeft(XTab value) {
        super.setLeft(value);
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
        super.setRight(value);
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
        super.setTop(value);
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
        super.setBottom(value);
        if (items.size() == 0 || value == null)
            return;
        if (direction == verticalDirection)
            items.get(items.size() - 1).setBottom(value);
        else {
            for (IArea area : items)
                area.setBottom(value);
        }
    }

    @Override
    public void setLeftRight(XTab left, XTab right) {
        setLeft(left);
        setRight(right);
    }

    @Override
    public void setTopBottom(YTab top, YTab bottom) {
        setTop(top);
        setBottom(bottom);
    }
}
