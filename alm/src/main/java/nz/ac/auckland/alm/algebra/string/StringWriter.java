/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm.algebra.string;

import nz.ac.auckland.alm.*;
import nz.ac.auckland.alm.algebra.RightDirection;
import nz.ac.auckland.linsolve.Variable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StringWriter {
    int areaCount = 0;
    int emptyCount = 0;
    final Iterable<IArea> fragments;
    final Map<IArea, String> areaNames = new HashMap<IArea, String>();
    final List<XTab> multipleXTabs = new ArrayList<XTab>();
    final List<YTab> multipleYTabs = new ArrayList<YTab>();

    public StringWriter(Iterable<IArea> fragments) {
        this.fragments = fragments;
    }

    static public String write(AlgebraSpec spec) {
        StringWriter writer = new StringWriter(spec.getFragments());
        return writer.write();
    }

    static public String write(Iterable<IArea> fragments) {
        StringWriter writer = new StringWriter(fragments);
        return writer.write();
    }

    public String write() {
        areaCount = 0;
        emptyCount = 0;
        areaNames.clear();

        processTabNames();

        String string = "";
        for (IArea area : fragments) {
            if (!string.equals(""))
                string += " * ";
            if (area instanceof Fragment)
                string += writeFragment((Fragment) area);
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
            if (area instanceof Fragment) {
                countTabs(area, tabCount);
                continue;
            }
            if (i == fragment.getItems().size() - 1)
                continue;
            Variable tab = fragment.direction.getTab(area);
            Integer count = tabCount.get(tab);
            if (count == null)
                count = 1;
            else
                count += 1;
            tabCount.put(tab, count);
        }
    }

    private String getName(IArea area) {
        String name = areaNames.get(area);
        if (name != null)
            return name;
        if (area instanceof Area) {
            final String areaNames = "ABCDEFGHIJKMNOPQRSTUVWXYZ";
            int letter = areaCount % areaNames.length();
            int index = areaCount / areaNames.length();
            name = "" + areaNames.charAt(letter);
            if (index > 0)
                name += areaCount;
            areaCount++;
        }
        if (area instanceof EmptySpace) {
            name = "L" + emptyCount;
            emptyCount++;
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

    private <Tab extends Variable, OrthTab extends Variable> String writeFragment(Fragment<Tab, OrthTab> fragment) {
        String operator = "/";
        if (fragment.direction instanceof RightDirection)
            operator = "|";

        String string = "";
        IArea prevArea = null;
        for (IArea area : fragment.getItems()) {
            if (prevArea != null) {
                string += operator;
                String tabName = getName(fragment.direction.getTab(prevArea));
                if (tabName != null)
                    string += "{" + tabName + "}";
            }
            prevArea = area;
            if (area instanceof Fragment) {
                Fragment subFragment = (Fragment) area;
                if (subFragment.direction != fragment.direction)
                    string += "(";
                string += writeFragment(subFragment);
                if (subFragment.direction != fragment.direction)
                    string += ")";
            } else
                string += getName(area);
        }
        return string;
    }
}
