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
import java.util.Iterator;
import java.util.List;


public class FragmentAlternatives {
    final private List<ITransformation> transformations = new ArrayList<ITransformation>();

    public FragmentAlternatives() {

    }

    static public class FragmentIterator implements Iterator<Fragment> {
        static class IteratorFragmentRef extends FragmentRef {
            final static private int LEFT_ROOT_LEVEL = -1;

            public IteratorFragmentRef clone() {
                IteratorFragmentRef iteratorFragmentRef = new IteratorFragmentRef();
                iteratorFragmentRef.currentPosition = currentPosition;
                iteratorFragmentRef.fragLevelPosition.addAll(fragLevelPosition);
                return iteratorFragmentRef;
            }

            public void setTo(IteratorFragmentRef iteratorFragmentRef) {
                this.currentPosition = iteratorFragmentRef.currentPosition;
                this.fragLevelPosition.clear();
                this.fragLevelPosition.addAll(iteratorFragmentRef.fragLevelPosition);
            }

            public void enterNextLevel() {
                fragLevelPosition.add(currentPosition);
                currentPosition = 0;
            }

            public void leaveLevel() {
                if (fragLevelPosition.size() == 0) {
                    currentPosition = LEFT_ROOT_LEVEL;
                    return;
                }

                currentPosition = fragLevelPosition.remove(fragLevelPosition.size() - 1);
                currentPosition++;
            }

            public int getCurrentPosition() {
                return currentPosition;
            }

            public List<Integer> getLevelPositions() {
                return fragLevelPosition;
            }

            public int advance() {
                currentPosition++;
                return currentPosition;
            }
        }

        final private Fragment rootFragment;

        final private IteratorFragmentRef currentFragmentRef = new IteratorFragmentRef();
        final private IteratorFragmentRef nextFragmentRef = new IteratorFragmentRef();

        public FragmentIterator(Fragment rootFragment) {
            this.rootFragment = rootFragment;
            assert rootFragment.size() >= 1;
        }

        private FragmentIterator(Fragment clone, IteratorFragmentRef currentFragmentRef, IteratorFragmentRef nextFragmentRef) {
            this.rootFragment = clone;
            assert rootFragment.size() >= 1;

            this.currentFragmentRef.setTo(currentFragmentRef);
            this.nextFragmentRef.setTo(nextFragmentRef);
        }

        /**
         * Clone the root fragment and return iterator that points the the same position as the current iterator.
         *
         * @return
         */
        protected FragmentIterator cloneFragment() {
            Fragment clone = rootFragment.clone();
            FragmentIterator iterator = new FragmentIterator(clone, currentFragmentRef, nextFragmentRef);
            return iterator;
        }

        /**
         * Clone the current fragment and replace the current fragment with the new fragment.
         *
         * @param newFragment
         * @return
         */
        public FragmentIterator cloneAndReplaceCurrent(Fragment newFragment) {
            FragmentIterator iterator = cloneFragment();
            Fragment currentLevel = iterator.getCurrentLevelFragment();
            int currentIndex = iterator.getCurrentPosition();
            Fragment.Item item = currentLevel.getRawItemAt(currentIndex);
            item.setItem(newFragment);
            // recalculate the next fragment position
            iterator.nextFragmentRef.setTo(iterator.currentFragmentRef);
            iterator.calculateNextFragmentPosition();
            return iterator;
        }

        public FragmentRef getFragmentRef() {
            return currentFragmentRef;
        }

        @Override
        public String toString() {
            return currentFragmentRef.toString() + " " + rootFragment.toString();
        }

        @Override
        public boolean hasNext() {
            return nextFragmentRef.getCurrentPosition() >= 0;
        }

        @Override
        public Fragment next() {
            currentFragmentRef.setTo(nextFragmentRef);
            calculateNextFragmentPosition();
            Fragment fragment = (Fragment)peek();
            return fragment;
        }

        @Override
        public void remove() {

        }

        private void calculateNextFragmentPosition() {
            if (nextFragmentRef.getCurrentPosition() == IteratorFragmentRef.LEFT_ROOT_LEVEL)
                return;

            // if currently on a fragment enter it
            if (peek() instanceof Fragment)
                nextFragmentRef.enterNextLevel();

            // iterate through the fragment and its parent fragments till we find the next fragment
            while (nextFragmentRef.getCurrentPosition() != IteratorFragmentRef.LEFT_ROOT_LEVEL) {
                Fragment levelFragment = getLevelFragment(rootFragment, nextFragmentRef);
                if (nextFragmentRef.getCurrentPosition() >= levelFragment.size()) {
                    nextFragmentRef.leaveLevel();
                    continue;
                }
                if (levelFragment.getItemAt(nextFragmentRef.getCurrentPosition()) instanceof Fragment)
                    break;

                nextFragmentRef.advance();
            }
        }

        public IArea peek() {
            Fragment currentLevel = getLevelFragment(rootFragment, currentFragmentRef);
            return currentLevel.getItemAt(currentFragmentRef.getCurrentPosition());
        }

        public Fragment getRootFragment() {
            return rootFragment;
        }

        private int getCurrentPosition() {
            return currentFragmentRef.getCurrentPosition();
        }

        private Fragment getCurrentLevelFragment() {
            return getLevelFragment(rootFragment, currentFragmentRef);
        }

        private Fragment getLevelFragment(Fragment root, IteratorFragmentRef iteratorFragmentRef) {
            for (Integer index : iteratorFragmentRef.getLevelPositions())
                root = (Fragment)root.getItemAt(index);
            return root;
        }
    }

    public void addTransformation(ITransformation transformation) {
        transformations.add(transformation);
    }

    static private class IntermediateResult {
        final public TrafoHistory trafoHistory;
        final public FragmentIterator iterator;

        public IntermediateResult(TrafoHistory trafoHistory, FragmentIterator iterator) {
            this.trafoHistory = trafoHistory;
            this.iterator = iterator;
        }
    }

    static public class Result {
        final public TrafoHistory trafoHistory;
        final public Fragment fragment;

        public Result(TrafoHistory trafoHistory, Fragment fragment) {
            this.trafoHistory = trafoHistory;
            this.fragment = fragment;
        }
    }

    public List<Result> calculateAlternatives(Fragment fragment, Comparator<IArea> comparator) {
        // put the fragment into a container fragment so that the iterator also includes the fragment
        final Fragment containerFragment = Fragment.horizontalFragment();
        containerFragment.add(fragment, false);

        final List<Result> results = new ArrayList<Result>();
        final List<IntermediateResult> ongoingTransformations = new ArrayList<IntermediateResult>();
        // add first fragment
        ongoingTransformations.add(new IntermediateResult(new TrafoHistory(), new FragmentIterator(containerFragment)));

        while (ongoingTransformations.size() > 0) {
            IntermediateResult current = ongoingTransformations.remove(0);
            FragmentIterator currentIterator = current.iterator;
            while (currentIterator.hasNext()) {
                Fragment subFragment = currentIterator.next();
                List<Fragment> subGroups = GroupDetector.detect(subFragment, comparator);
                subGroups.add(subFragment);
                for (Fragment groupItem : subGroups) {
                    for (ITransformation transformation : transformations) {
                        List<ITransformation.Result> trafoResults = transformation.transform(groupItem);
                        for (ITransformation.Result result : trafoResults) {
                            TrafoHistory trafoHistory = current.trafoHistory.clone();
                            FragmentRef fragmentRef = currentIterator.getFragmentRef().clone();
                            trafoHistory.addTrafoHistory(fragmentRef, transformation, result);
                            FragmentIterator subIterator = currentIterator.cloneAndReplaceCurrent(result.fragment);
                            ongoingTransformations.add(new IntermediateResult(trafoHistory, subIterator));
                        }
                    }
                }
            }
            // get the fragment out of the container
            Fragment returnedFragment = (Fragment)currentIterator.getRootFragment().getItemAt(0);
            if (returnedFragment != fragment && !returnedFragment.isEquivalent(fragment)) {
                //returnedFragment.removeSingletons();
                int equivalentIndex = getEquivalent(results, returnedFragment);
                if (equivalentIndex >= 0) {
                    // choose the one with lower quality
                    if (results.get(equivalentIndex).trafoHistory.getTotalQuality()
                            > current.trafoHistory.getTotalQuality()) {
                        results.remove(equivalentIndex);
                        results.add(new Result(current.trafoHistory, returnedFragment));
                    }
                } else
                    results.add(new Result(current.trafoHistory, returnedFragment));
            }
        }

        return results;
    }

    private int getEquivalent(List<Result> results, Fragment fragment) {
        for (int i = 0; i < results.size(); i++) {
            Result result = results.get(i);
            if (result.fragment.isEquivalent(fragment))
                return i;
        }
        return -1;
    }
}
