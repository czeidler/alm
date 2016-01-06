/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm.algebra.trafo;

import nz.ac.auckland.alm.IArea;
import nz.ac.auckland.alm.algebra.Fragment;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class GroupDetector {
    static public List<Fragment> detect(Fragment fragment, Comparator<IArea> comparator) {
        List<Fragment> ongoing = detectSinglePass(fragment, comparator);
        List<Fragment> finalized = new ArrayList<Fragment>();
        while (ongoing.size() > 0) {
            Fragment alternative = ongoing.remove(0);
            List<Fragment> subAlternatives = detectSinglePass(alternative, comparator);
            if (subAlternatives.size() == 0) {
                if (!inList(finalized, alternative, comparator))
                    finalized.add(alternative);
            } else
                ongoing.addAll(subAlternatives);
        }
        return finalized;
    }

    static private boolean inList(List<Fragment> list, Fragment fragment, Comparator<IArea> comparator) {
        for (Fragment item : list) {
            if (comparator.compare(item, fragment) == 0)
                return true;
        }
        return false;
    }

    static private List<Fragment> detectSinglePass(Fragment fragment, Comparator<IArea> comparator) {
        List<Fragment> alternatives = new ArrayList<Fragment>();
        List<IArea> items = fragment.getItems();
        for (int groupSize = 1; groupSize <= items.size() / 2; groupSize++) {
            for (int offset = 0; offset < groupSize; offset++) {
                Fragment alternative = detect(fragment, offset, groupSize, comparator);
                if (alternative != null)
                    alternatives.add(alternative);
            }
        }
        return alternatives;
    }

    static private void addIfNotAtTail(Fragment fragment, IArea item) {
        List<IArea> items = fragment.getItems();
        int tail = items.size() - 1;
        IArea tailArea = null;
        if (tail >= 0)
            tailArea = items.get(tail);
        if (tailArea != item)
            fragment.add(item, false);
    }

    /**
     * Detects groups.
     *
     * For example, detecting the sequence 1, 2, 3, 2, 3 with offset 1 and group size 2 would result in a
     * sequence 1, 4, 4.
     *
     * @param fragment the fragment to search
     * @param offset search start index
     * @param groupSize size of the group to detect
     * @return null if not groups have been detect
     */
    static private Fragment detect(Fragment fragment, int offset, int groupSize, Comparator<IArea> comparator) {
        Fragment alternative = Fragment.createEmptyFragment(fragment.getDirection());
        IArea currentSubGroup = null;
        IArea nextSubGroup;
        List<IArea> items = fragment.getItems();
        for (int i = 0; i < offset; i++)
            alternative.add(items.get(i), false);
        int currentPosition = offset;
        Fragment currentMatches = null;
        for (int i = offset; i <= items.size() - 2 * groupSize; i += groupSize) {
            if (currentSubGroup == null)
                currentSubGroup = copySubGroup(fragment, i, groupSize);
            nextSubGroup = copySubGroup(fragment, i + groupSize, groupSize);
            if (comparator.compare(currentSubGroup, nextSubGroup) == 0) {
                // start current matches?
                if (currentMatches == null)
                    currentMatches = Fragment.createEmptyFragment(fragment.getDirection());
                addIfNotAtTail(currentMatches, currentSubGroup);
                currentMatches.add(nextSubGroup, false);
                currentPosition = i + 2 * groupSize;
            } else {
                // close current matches?
                if (currentMatches != null) {
                    alternative.add(currentMatches, false);
                    currentMatches = null;
                }
                for (int a = currentPosition; a < i + groupSize; a++)
                    alternative.add(items.get(a), false);
                currentPosition = i + groupSize;
            }
            currentSubGroup = nextSubGroup;
        }
        // close the current matches
        if (currentMatches != null)
            alternative.add(currentMatches, false);

        // add the remaining areas
        for (int i = currentPosition; i < items.size(); i++)
            alternative.add(items.get(i), false);

        if (alternative.getItems().size() == 0)
            return null;
        // remove superfluous containing fragment, e.g. ((A|B) | (A|B)) -> (A|B) | (A|B)
        if (alternative.getItems().size() == 1) {
            IArea area = (IArea) alternative.getItems().get(0);
            if (area instanceof Fragment)
                alternative = (Fragment)area;
        }
        // don't return an alternative if all item in a fragment are the same, i.e. the alternative is equivalent
        if (alternative.getItems().size() == fragment.getItems().size())
            return null;
        return alternative;
    }

    static private IArea copySubGroup(Fragment fragment, int offset, int size) {
        List<IArea> items = fragment.getItems();
        if (size == 1)
            return items.get(offset);
        Fragment subGroup = Fragment.createEmptyFragment(fragment.getDirection());
        for (int i = offset; i < offset + size; i++)
            subGroup.add(items.get(i), false);
        return subGroup;
    }
}
