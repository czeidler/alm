/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm.algebra.trafo;

import java.util.ArrayList;
import java.util.List;


public class TrafoHistory {
    static public class Entry {
        final public FragmentRef fragmentRef;
        final public ITransformation trafo;
        final public ITransformation.Result trafoResult;

        public Entry(FragmentRef fragmentRef, ITransformation trafo, ITransformation.Result trafoResult) {
            this.fragmentRef = fragmentRef;
            this.trafo = trafo;
            this.trafoResult = trafoResult;
        }
    }
    final private List<Entry> entries = new ArrayList<Entry>();

    public TrafoHistory() {
    }

    public Entry addTrafoHistory(FragmentRef fragmentRef, ITransformation trafo,
                                 ITransformation.Result trafoResult) {
        Entry entry = new Entry(fragmentRef, trafo, trafoResult);
        entries.add(entry);
        return entry;
    }

    public TrafoHistory clone() {
        TrafoHistory clone = new TrafoHistory();
        clone.entries.addAll(entries);
        return clone;
    }

    public float getTotalQuality() {
        float quality = 1f;
        for (Entry entry : entries)
            quality *= entry.trafoResult.quality;
        return quality;
    }

    public int getNTrafos() {
        return entries.size();
    }

    public int getHighestNestedLevel() {
        int highestLevel = 0;
        for (Entry entry : entries) {
            int nestedLevel = getNestedLevel(entry);
            if (nestedLevel > highestLevel)
                highestLevel = nestedLevel;
        }
        return highestLevel;
    }

    private int getNestedLevel(Entry entry) {
        int level = 0;
        for (Entry current : entries) {
            if (current == entry)
                continue;
            if (!isSubTree(entry, current))
                continue;

            level++;
        }
        return level;
    }

    /**
     * Check if subTree is a sub tree of entry.
     *
     * For example, the entry = /0/1/3 is sub tree of /0/1/3/4
     *
     * @param entry
     * @param subTree
     * @return
     */
    private boolean isSubTree(Entry entry, Entry subTree) {
        List<Integer> subTreeLevels = subTree.fragmentRef.getLevelPositions();
        List<Integer> entryLevels = entry.fragmentRef.getLevelPositions();
        if (subTreeLevels.size() >= entryLevels.size())
            return false;
        for (int i = 0; i < subTreeLevels.size(); i++) {
            if (!subTreeLevels.get(i).equals(entryLevels.get(i)))
                return false;
        }
        if (subTree.fragmentRef.getCurrentPosition() != entryLevels.get(subTreeLevels.size()))
            return false;
        return true;
    }
}
