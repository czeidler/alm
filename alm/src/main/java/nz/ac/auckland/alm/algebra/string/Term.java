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


public class Term<Tab extends Variable, OrthTab extends Variable> extends TabArea {
    final List<IArea> items = new ArrayList<IArea>();
    IDirection<Tab, OrthTab> direction;

    static final public IDirection horizontalDirection = new RightDirection();
    static final public IDirection verticalDirection = new BottomDirection();

    static public Term horizontalTerm(IArea area1, IArea area2) {
        return new Term(area1, area2, horizontalDirection);
    }

    static public Term verticalTerm(IArea area1, IArea area2) {
        return new Term(area1, area2, verticalDirection);
    }

    public Term() {

    }

    private Term(IArea area1, IArea area2, IDirection<Tab, OrthTab> direction1) {
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

    public boolean hasSubTerm(Term subTerm) {
        for (IArea area : items) {
            if (!(area instanceof Term))
                continue;
            Term term = (Term) area;
            if (term == subTerm)
                return true;
            if (term.hasSubTerm(subTerm))
                return true;
        }
        return false;
    }

    public boolean hasAtom(IArea atom) {
        for (IArea area : items) {
            if (!(area instanceof Term)) {
                if (area == atom)
                    return true;
            } else {
                Term term = (Term) area;
                if (term.hasAtom(atom))
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

        if (item instanceof Term) {
            Term term = (Term) item;
            if (term.direction == null || term.direction == direction) {
                for (Object subItem : term.getItems())
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
            if (item instanceof Term)
                count += ((Term) item).countAtoms();
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
