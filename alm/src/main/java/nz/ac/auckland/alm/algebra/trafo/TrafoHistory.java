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
        final public List<FragmentRef> fragmentRefs = new ArrayList<FragmentRef>();
        final public List<ITransformation> trafos = new ArrayList<ITransformation>();
        final public List<ITransformation.Result> trafoResults = new ArrayList<ITransformation.Result>();

        public Entry() {
        }

        public void add(FragmentRef fragmentRef, ITransformation trafo, ITransformation.Result trafoResult) {
            fragmentRefs.add(fragmentRef);
            trafos.add(trafo);
            trafoResults.add(trafoResult);
        }

        public int size() {
            return fragmentRefs.size();
        }
    }
    final private List<Entry> entries = new ArrayList<Entry>();

    public TrafoHistory() {
    }

    public void add(Entry entry) {
        entries.add(entry);
    }

    public TrafoHistory clone() {
        TrafoHistory clone = new TrafoHistory();
        clone.entries.addAll(entries);
        return clone;
    }

    public int getNTrafos() {
        return entries.size();
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
    /*
    private boolean isSubTree(Entry entry, Entry subTree) {
        List<Integer> subTreeLevels = subTree.fragmentRef.getLevelPositions();
        List<Integer> entryLevels = entry.fragmentRef.getLevelPositions();
        if (subTreeLevels.size() >= entryLevels.size())
            return false;
        for (int i = 0; i < subTreeLevels.size(); i++) {
            if (!subTreeLevels.get(i).equals(entryLevels.get(i)))
                return false;
        }
        return true;
    }*/
}
