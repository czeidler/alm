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
import nz.ac.auckland.alm.algebra.*;
import nz.ac.auckland.linsolve.Variable;


import java.util.ArrayList;
import java.util.List;


public class Term<Tab extends Variable, OrthTab extends Variable> extends TabArea {
    final List<IArea> items = new ArrayList<IArea>();
    final IDirection<Tab, OrthTab> direction;

    static IDirection horizontalDirection = new RightDirection();
    static IDirection verticalDirection = new BottomDirection();

    static Term horizontalTerm(IArea area1, IArea area2) {
        return new Term(area1, area2, horizontalDirection);
    }

    static Term verticalTerm(IArea area1, IArea area2) {
        return new Term(area1, area2, verticalDirection);
    }

    private Term(IArea area1, IArea area2, IDirection<Tab, OrthTab> direction1) {
        this.direction = direction1;

        setFirstItem(area1);
        add(area1);
        if  (area2 != null)
            add(area2);
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
        assert items.size() == 0;

        direction.setOppositeTab(this, direction.getOppositeTab(item));
        direction.setOrthogonalTab1(this, direction.getOrthogonalTab1(item));
        direction.setOrthogonalTab2(this, direction.getOrthogonalTab2(item));
    }

    public List<IArea> getItems() {
        return items;
    }

    public IArea getFirstAtom() {
        IArea area = items.get(0);
        if (area instanceof Term)
            return ((Term) area).getFirstAtom();
        else
            return area;
    }

    public IArea getLastAtom() {
        IArea area = items.get(items.size() - 1);
        if (area instanceof Term)
            return ((Term) area).getLastAtom();
        else
            return area;
    }

    public void add(IArea item) {
        if (item instanceof Term) {
            Term term = (Term) item;
            if (term.direction == direction) {
                for (Object subItem : term.getItems())
                    add((IArea)subItem);
                return;
            }
        }
        items.add(item);
        direction.setTab(this, direction.getTab(item));
        if (direction.getOrthogonalTab1(this) != direction.getOrthogonalTab1(item))
            direction.setOrthogonalTab1(this, null);
        if (direction.getOrthogonalTab2(this) != direction.getOrthogonalTab2(item))
            direction.setOrthogonalTab2(this, null);
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
}
