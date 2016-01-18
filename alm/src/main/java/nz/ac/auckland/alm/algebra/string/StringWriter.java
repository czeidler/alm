/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm.algebra.string;

import nz.ac.auckland.alm.*;
import nz.ac.auckland.alm.algebra.Fragment;
import nz.ac.auckland.alm.algebra.RightDirection;
import nz.ac.auckland.linsolve.Variable;

import java.util.*;

public class StringWriter {
    int areaCount = 0;
    int emptyCount = 0;
    final Collection<Fragment> fragments;
    final Map<IArea, String> areaNames = new HashMap<IArea, String>();
    final List<XTab> multipleXTabs = new ArrayList<XTab>();
    final List<YTab> multipleYTabs = new ArrayList<YTab>();

    public StringWriter(Collection<Fragment> fragments) {
        this.fragments = fragments;
    }

    static public String write(AlgebraSpec spec) {
        StringWriter writer = new StringWriter(spec.getFragments());
        return writer.write(true);
    }

    static public String write(AlgebraSpec spec, boolean showNestedFragments) {
        StringWriter writer = new StringWriter(spec.getFragments());
        return writer.write(showNestedFragments);
    }

    static public String write(Collection<Fragment> fragments) {
        StringWriter writer = new StringWriter(fragments);
        return writer.write(true);
    }

    static public String write(Collection<Fragment> fragments, boolean showNestedFragments) {
        StringWriter writer = new StringWriter(fragments);
        return writer.write(showNestedFragments);
    }

    public static String write(Fragment fragment) {
        return write(Collections.singletonList(fragment));
    }

    public static String write(Fragment fragment, boolean showNestedFragments) {
        return write(Collections.singletonList(fragment), showNestedFragments);
    }

    public String write(boolean showNestedFragments) {
        areaCount = 0;
        emptyCount = 0;
        areaNames.clear();

        processTabNames();

        String string = "";
        for (IArea area : fragments) {
            if (!string.equals(""))
                string += " * ";
            if (area instanceof Fragment)
                string += writeFragment((Fragment) area, showNestedFragments);
            else
                string += getName(area);
        }
        return string;
    }

    private void processTabNames() {
        multipleXTabs.clear();
        multipleYTabs.clear();

        Map<Variable, Integer> tabCount = new HashMap<Variable, Integer>();
        for (IArea fragment : fragments)
            countTabs(fragment, tabCount);

        for (Map.Entry<Variable, Integer> entry : tabCount.entrySet()) {
            if (entry.getValue() <= 1)
                continue;
            Variable tab = entry.getKey();
            if (tab instanceof XTab)
                multipleXTabs.add((XTab) tab);
            if (tab instanceof YTab)
                multipleYTabs.add((YTab) tab);
        }
    }

    private void countTabs(IArea fragmentArea, Map<Variable, Integer> tabCount) {
        if (!(fragmentArea instanceof Fragment))
            return;
        Fragment fragment = (Fragment) fragmentArea;
        for (int i = 0; i < fragment.getItems().size(); i++) {
            IArea area = (IArea) fragment.getItems().get(i);
            if (area instanceof Fragment)
                countTabs(area, tabCount);
            if (i == fragment.getItems().size() - 1)
                continue;
            Variable tab = fragment.getDirection().getTab(area);
            Integer count = tabCount.get(tab);
            if (count == null)
                count = 1;
            else
                count += 1;
            tabCount.put(tab, count);
        }
    }

    private String getName(IArea area) {
        String name = area.getId();
        if (name != null)
            return name;
        name = areaNames.get(area);
        if (name != null)
            return name;
        if (area instanceof EmptySpace) {
            name = "_" + emptyCount;
            emptyCount++;
        } else {
            final String areaNames = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
            int letter = areaCount % areaNames.length();
            int index = areaCount / areaNames.length();
            name = "" + areaNames.charAt(letter);
            if (index > 0)
                name += index;
            areaCount++;
        }
        assert name != null;
        areaNames.put(area, name);
        return name;
    }

    private String getName(Variable tab) {
        if (multipleXTabs.contains(tab))
            return "" + multipleXTabs.indexOf(tab);
        if (multipleYTabs.contains(tab))
            return "" + multipleYTabs.indexOf(tab);
        return null;
    }

    private <Tab extends Variable, OrthTab extends Variable> String writeFragment(Fragment<Tab, OrthTab> fragment,
                                                                                  boolean showNestedFragments) {
        String operator = "/";
        if (fragment.getDirection() instanceof RightDirection)
            operator = "|";

        String string = "";
        IArea prevArea = null;
        for (IArea area : fragment.getItems()) {
            if (prevArea != null) {
                string += operator;
                String tabName = getName(fragment.getDirection().getTab(prevArea));
                if (tabName != null)
                    string += "{" + tabName + "}";
            }
            prevArea = area;
            if (area instanceof Fragment) {
                Fragment subFragment = (Fragment) area;
                if (showNestedFragments || subFragment.getDirection() != fragment.getDirection())
                    string += "(";
                string += writeFragment(subFragment, showNestedFragments);
                if (showNestedFragments || subFragment.getDirection() != fragment.getDirection())
                    string += ")";
            } else
                string += getName(area);
        }
        return string;
    }
}
