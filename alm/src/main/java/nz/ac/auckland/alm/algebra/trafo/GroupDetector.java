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
import nz.ac.auckland.alm.algebra.IDirection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class GroupDetector implements IGroupDetector {
    final private Comparator<IArea> comparator;

    public GroupDetector(Comparator<IArea> comparator) {
        this.comparator = comparator;
    }

    @Override
    public List<Fragment> detect(Fragment fragment) {
        return detect(fragment, comparator);
    }

    /**
     * Detects item groups.
     *
     * @param fragment the fragment to analyse
     * @param comparator used to find similar items
     * @return all detected groups exclusive the fragment.
     */
    static public List<Fragment> detect(Fragment fragment, Comparator<IArea> comparator) {
        /*List<Fragment> finalized = detectSinglePass(fragment, comparator);
        List<Fragment> ongoing = new ArrayList<Fragment>();
        ongoing.addAll(detectAcrossChild(fragment, comparator));
        while (ongoing.size() > 0) {
            Fragment alternative = ongoing.remove(0);
            List<Fragment> subAlternatives = detectSinglePass(alternative, comparator);
            if (subAlternatives.size() == 0) {
                if (!inList(finalized, alternative, comparator))
                    finalized.add(alternative);
            }
        }
        return finalized;*/

        List<Fragment> ongoing = detectSinglePass(fragment, comparator);
        //ongoing.addAll(detectAcrossChild(fragment, comparator));
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

    static class DetectAcrossChild {
        interface IState {
            boolean addChild(IArea area);
            void finish();
        }

        class StartState implements IState {
            @Override
            public boolean addChild(IArea area) {
                if (!(area instanceof Fragment))
                    result.add(area, false);
                else
                    setState(new RunCandidateState((Fragment)area));
                return false;
            }

            @Override
            public void finish() {

            }
        }

        class RunCandidateState implements IState {
            final private Fragment first;

            public RunCandidateState(Fragment firstItem) {
                this.first = firstItem;
            }

            @Override
            public boolean addChild(IArea area) {
                if (!(area instanceof Fragment)) {
                    result.add(first, false);
                    result.add(area, false);
                    setState(new StartState());
                    return false;
                }
                Fragment run = Fragment.createEmptyFragment(fragment.getDirection());
                addChildren(run, first);
                addChildren(run, (Fragment)area);
                List<Fragment> groups = GroupDetector.detectSinglePass(run, comparator);
                if (groups.size() == 0) {
                    result.add(first, false);
                    setState(new RunCandidateState((Fragment)area));
                    return false;
                }

                setState(new RunState(run, (Fragment)area));
                return true;
            }

            @Override
            public void finish() {
                result.add(first, false);
            }
        }

        class RunState implements IState {
            final private Fragment run;
            private Fragment lastAdded;

            public RunState(Fragment run, Fragment lastAdded) {
                this.run = run;
                this.lastAdded = lastAdded;
            }

            @Override
            public boolean addChild(IArea area) {
                if (!(area instanceof Fragment)) {
                    addChildren(result, run);
                    result.add(area, false);
                    setState(new StartState());
                    return false;
                }
                Fragment current = (Fragment)area;
                Fragment subRun = Fragment.createEmptyFragment(fragment.getDirection());
                addChildren(subRun, lastAdded);
                addChildren(subRun, current);
                List<Fragment> groups = GroupDetector.detectSinglePass(subRun, comparator);
                lastAdded = current;
                if (groups.size() > 0) {
                    run.add(current, false);
                    return true;
                }

                addChildren(result, run);
                setState(new RunCandidateState(lastAdded));
                return false;
            }

            @Override
            public void finish() {
                addChildren(result, run);
            }
        }

        final Fragment fragment;
        final Comparator<IArea> comparator;
        final Fragment result;
        private IState currentState = new StartState();
        private boolean hasGroup = false;

        public DetectAcrossChild(Fragment fragment, Comparator<IArea> comparator) {
            this.fragment = fragment;
            this.comparator = comparator;
            this.result = Fragment.createEmptyFragment(fragment.getDirection());
        }

        private void setState(IState state) {
            if (state == null)
                currentState.finish();
            currentState = state;
        }

        static private void addChildren(Fragment target, Fragment source) {
            for (IArea child : (Iterable<IArea>)source.getItems())
                target.add(child, false);
        }

        public List<Fragment> detect() {
            for (IArea child : (Iterable<IArea>)fragment.getItems()) {
                if (currentState.addChild(child))
                    hasGroup = true;
            }
            // finish the current state
            setState(null);
            if (!hasGroup)
                return Collections.emptyList();
            return Collections.singletonList(result);
        }
    }

    /**
     * Returns a fragment with merged children if children might belong to a group
     *
     * For example, (A|B)/(A|B)/(C|D) would return A/B/A/B/(C|D). This fragment can then be further analysed.
     *
     * @param fragment
     * @param comparator
     * @return
     */
    static public List<Fragment> detectAcrossChild(Fragment fragment, Comparator<IArea> comparator) {
        DetectAcrossChild detector = new DetectAcrossChild(fragment, comparator);
        return detector.detect();
    }

    static private boolean inList(List<Fragment> list, Fragment fragment, Comparator<IArea> comparator) {
        for (Fragment item : list) {
            if (comparator.compare(item, fragment) == 0)
                return true;
        }
        return false;
    }

    static public List<Fragment> detectSinglePass(Fragment fragment, Comparator<IArea> comparator) {
        List<Fragment> alternatives = new ArrayList<Fragment>();
        for (int groupSize = 1; groupSize <= fragment.size() / 2; groupSize++) {
            for (int offset = 0; offset < groupSize; offset++) {
                Fragment alternative = detect(fragment, offset, groupSize, comparator);
                if (alternative != null)
                    alternatives.add(alternative);
            }
            if (alternatives.size() > 1)
                break;
        }
        return alternatives;
    }

    static private void addIfNotAtTail(Fragment fragment, IArea item) {
        int tail = fragment.size() - 1;
        IArea tailArea = null;
        if (tail >= 0)
            tailArea = fragment.getItemAt(tail);
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
        for (int i = 0; i < offset; i++)
            alternative.add(fragment.getItemAt(i), false);
        int currentPosition = offset;
        Fragment currentMatches = null;
        for (int i = offset; i <= fragment.size() - 2 * groupSize; i += groupSize) {
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
                    alternative.add(fragment.getItemAt(a), false);
                currentPosition = i + groupSize;
            }
            currentSubGroup = nextSubGroup;
        }
        // close the current matches
        if (currentMatches != null)
            alternative.add(currentMatches, false);

        // add the remaining areas
        for (int i = currentPosition; i < fragment.size(); i++)
            alternative.add(fragment.getItemAt(i), false);

        if (alternative.size() == 0)
            return null;
        // remove superfluous containing fragment, e.g. ((A|B) | (A|B)) -> (A|B) | (A|B)
        if (alternative.size() == 1) {
            IArea area = alternative.getItemAt(0);
            if (area instanceof Fragment)
                alternative = (Fragment)area;
        }
        // don't return an alternative if all item in a fragment are the same, i.e. the alternative is equivalent
        if (alternative.size() == fragment.size())
            return null;
        return alternative;
    }

    static private IArea copySubGroup(Fragment fragment, int offset, int size) {
        if (size == 1)
            return fragment.getItemAt(offset);
        Fragment subGroup = Fragment.createEmptyFragment(fragment.getDirection());
        for (int i = offset; i < offset + size; i++)
            subGroup.add(fragment.getItemAt(i), false);
        return subGroup;
    }
}
