/*
 * Copyright 2015.
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
import nz.ac.auckland.alm.algebra.string.StringWriter;
import nz.ac.auckland.linsolve.Variable;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class Fragment<Tab extends Variable, OrthTab extends Variable> extends TabArea {
    static public class Item<Tab extends Variable> {
        // for horizontal direction; the tab on the right side of the item
        private Tab tab2;
        private IArea item;

        public Item(IArea item, Tab tab2) {
            this.item = item;
            this.tab2 = tab2;
        }

        public Tab getTab2() {
            return tab2;
        }

        public void setTab2(Tab tab2) {
            this.tab2 = tab2;
        }

        public IArea getItem() {
            return item;
        }

        public void setItem(IArea item) {
            this.item = item;
        }
    }

    final private List<Item<Tab>> items = new ArrayList<Item<Tab>>();
    private IDirection<Tab, OrthTab> direction;

    static final public IDirection horizontalDirection = new RightDirection();
    static final public IDirection verticalDirection = new BottomDirection();

    static public Fragment<XTab, YTab> horizontalFragment(IArea area1, XTab tab, IArea area2) {
        return new Fragment<XTab, YTab>(area1, tab, area2, horizontalDirection);
    }

    static public Fragment<YTab, XTab> verticalFragment(IArea area1, YTab tab, IArea area2) {
        return new Fragment<YTab, XTab>(area1, tab, area2, verticalDirection);
    }

    static public Fragment<XTab, YTab> horizontalFragment() {
        Fragment fragment = new Fragment();
        fragment.direction = horizontalDirection;
        return fragment;
    }

    static public Fragment<YTab, XTab> verticalFragment() {
        Fragment fragment = new Fragment();
        fragment.direction = verticalDirection;
        return fragment;
    }

    static public Fragment createEmptyFragment(IDirection direction) {
        assert direction == horizontalDirection || direction == verticalDirection;

        Fragment fragment = new Fragment();
        fragment.direction = direction;
        return fragment;
    }

    public Fragment() {

    }

    public Fragment(IArea area1, Tab tab, IArea area2, IDirection<Tab, OrthTab> direction) {
        assert direction == horizontalDirection || direction == verticalDirection;
        this.direction = direction;

        add(area1, tab, true);
        if  (area2 != null)
            add(area2, true);
    }

    /**
     * Apply the fragment specification to the child, i.e. set the tabs
     */
    public void applySpecsToChild() {
        IDirection direction = getDirection();
        if (size() == 1 && getItemAt(0) instanceof Fragment) {
            ((Fragment)getItemAt(0)).applySpecsToChild();
            return;
        }
        for (int i = 0; i < size() - 1; i++) {
            IArea area1 = getItemAt(i);
            IArea area2 = getItemAt(i + 1);
            if (area1 instanceof Fragment)
                ((Fragment)area1).applySpecsToChild();
            if (area2 instanceof Fragment)
                ((Fragment)area2).applySpecsToChild();
            Variable tab = direction.createTab();
            direction.setTab(area1, tab);
            direction.setOppositeTab(area2, tab);
        }
        //TODO set tabs that are explicitly specified in the fragment
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

    public boolean isHorizontalDirection() {
        return this.direction == horizontalDirection;
    }

    public void setVerticalDirection() {
        this.direction = verticalDirection;
    }

    public boolean isVerticalDirection() {
        return this.direction == verticalDirection;
    }

    public IDirection<Tab, OrthTab> getDirection() {
        return direction;
    }

    public List<Item<Tab>> getRawItems() {
        return items;
    }

    public Item<Tab> getRawItemAt(int index) {
        return items.get(index);
    }

    public Iterable<IArea> getItems() {
        final Iterator<Item<Tab>> itemIterator = items.iterator();
        return new Iterable<IArea>() {
            @Override
            public Iterator<IArea> iterator() {
                return new Iterator<IArea>() {
                    @Override
                    public boolean hasNext() {
                        return itemIterator.hasNext();
                    }

                    @Override
                    public IArea next() {
                        return itemIterator.next().item;
                    }

                    @Override
                    public void remove() {
                        throw new RuntimeException("not supported");
                    }
                };
            }
        };
    }

    public int size() {
        return items.size();
    }

    public IArea getItemAt(int i) {
        return items.get(i).item;
    }

    public Fragment clone() {
        return cloneResolve(false);
    }

    /**
     * Clone and resolve sub fragments
     *
     * @param resolve merges sub fragments if true
     * @return a copy of the fragment
     */
    public Fragment cloneResolve(boolean resolve) {
        Fragment clone = Fragment.createEmptyFragment(getDirection());
        for (IArea child : getItems()) {
            if (child instanceof Fragment) {
                Fragment childClone = ((Fragment) child).cloneResolve(resolve);
                if (childClone.size() == 1 && resolve)
                    clone.add(childClone.getItemAt(0), resolve);
                else
                    clone.add(childClone, resolve);
            } else
                clone.add(child, resolve);
        }
        return clone;
    }

    /**
     * Ensure that each child fragment contains more than one item.
     *
     * For example, A|(B)|C -> A|B|C
     */
    public void removeSingletons() {
        for (int i = 0; i < size(); i++) {
            IArea area = getItemAt(i);
            if (!(area instanceof Fragment))
                continue;
            Fragment fragment = (Fragment)area;
            if (fragment.size() == 1) {
                // replace fragment
                remove(i, null);
                add(i, fragment.getRawItemAt(0), false);

                // scan the merged fragment by reducing i
                i--;
                continue;
            }
            fragment.removeSingletons();
        }
    }

    public boolean isEquivalent(Fragment fragment) {
        Fragment resolvedCopy = cloneResolve(true);
        return resolvedCopy.isEquivalentResolved(fragment.cloneResolve(true));
    }

    /**
     * Assumes both fragment are resolved
     * @param fragment
     * @return
     */
    private boolean isEquivalentResolved(Fragment fragment) {
        if (fragment.getDirection() != getDirection())
            return false;
        if (fragment.size() != size())
            return false;
        for (int i = 0; i < size(); i++) {
            IArea ours = getItemAt(i);
            IArea theirs = fragment.getItemAt(i);
            if (ours instanceof Fragment) {
                if (!(theirs instanceof Fragment))
                    return false;
                if (!((Fragment)ours).isEquivalentResolved((Fragment)theirs))
                    return false;
            } else if (ours != theirs)
                return false;
        }
        return true;
    }

    /**
     * Add an item to the Fragment.
     *
     * @param item the item to add
     * @param index index where to add the item
     * @param mergeFragments if true and item is a fragment with same direction the fragments are merged. For example,
     *                       (A|B) + (C|D) becomes (A|B|C|D) instead of  (A|B|(C|D)).
     */
    public void add(int index, Item<Tab> item, boolean mergeFragments) {
        if (mergeFragments && item.getItem() instanceof Fragment) {
            Fragment fragment = (Fragment) item.getItem();
            if (fragment.direction == direction || fragment.size() == 1) {
                int reverseIndex = size() - index;
                for (Item<Tab> subItem : (List<Item<Tab>>)fragment.getRawItems())
                    add(size() - reverseIndex, subItem, true);
                return;
            }
        }
        items.add(index, item);
    }

    public void add(IArea item, Tab tab, boolean mergeFragments) {
        add(size(), new Item<Tab>(item, tab), mergeFragments);
    }

    public void add(IArea item, boolean mergeFragments) {
        add(item, null, mergeFragments);
    }

    public Item remove(int index, Tab mergeTab) {
        Item item = items.remove(index);
        if (index > 0)
            items.get(index - 1).setTab2(mergeTab);
        return item;
    }

    public int countAtoms() {
        int count = 0;
        for (IArea item : getItems()) {
            if (item instanceof Fragment)
                count += ((Fragment) item).countAtoms();
            else
                count++;
        }
        return count;
    }

    private <Tab extends Variable, OrthTab extends Variable> Tab getTab1(IDirection<Tab, OrthTab> direction) {
        Tab tab = direction.getTab(getItemAt(0));
        if (tab == null)
            return null;
        for (int i = 1; i < size(); i++) {
            Tab areaTab = direction.getTab(getItemAt(i));
            if (areaTab == null || tab != areaTab)
                return null;
        }
        return tab;
    }

    private <Tab extends Variable, OrthTab extends Variable> Tab getTab2(IDirection<Tab, OrthTab> direction) {
        Tab tab = direction.getTab(getItemAt(size() - 1));
        if (tab == null)
            return null;
        for (int i = 0; i < size() - 1; i++) {
            Tab areaTab = direction.getTab(getItemAt(i));
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
            return getItemAt(0).getLeft();
        return getTab1(new LeftDirection());
    }

    @Override
    public YTab getTop() {
        if (items.size() == 0)
            return null;

        if (direction == verticalDirection)
            return getItemAt(0).getTop();
        return getTab1(new TopDirection());
    }

    @Override
    public XTab getRight() {
        if (items.size() == 0)
            return null;

        if (direction == horizontalDirection)
            return getItemAt(items.size() - 1).getRight();
        return getTab2(new RightDirection());
    }

    @Override
    public YTab getBottom() {
        if (size() == 0)
            return null;

        if (direction == verticalDirection)
            return getItemAt(items.size() - 1).getBottom();
        return getTab2(new BottomDirection());
    }

    @Override
    public void setLeft(XTab value) {
        if (size() == 0 || value == null)
            return;
        if (direction == horizontalDirection)
            getItemAt(0).setLeft(value);
        else {
            for (IArea area : getItems())
                area.setLeft(value);
        }
    }

    @Override
    public void setRight(XTab value) {
        if (size() == 0 || value == null)
            return;
        if (direction == horizontalDirection)
            getItemAt(size() - 1).setRight(value);
        else {
            for (IArea area : getItems())
                area.setRight(value);
        }
    }

    @Override
    public void setTop(YTab value) {
        if (size() == 0 || value == null)
            return;
        if (direction == verticalDirection)
            getItemAt(0).setTop(value);
        else {
            for (IArea area : getItems())
                area.setTop(value);
        }
    }

    @Override
    public void setBottom(YTab value) {
        if (size() == 0 || value == null)
            return;
        if (direction == verticalDirection)
            getItemAt(size() - 1).setBottom(value);
        else {
            for (IArea area : getItems())
                area.setBottom(value);
        }
    }
}
