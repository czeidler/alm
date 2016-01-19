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
import java.util.Iterator;
import java.util.List;


public class FragmentAlternatives {
    final private List<ITransformation> transformations = new ArrayList<ITransformation>();

    public FragmentAlternatives() {

    }

    static public class FragmentIterator implements Iterator<Fragment> {
        static class AreaRef {
            // list of indices at what position the next level has been entered
            final private List<Integer> fragLevelPosition = new ArrayList<Integer>();
            final static private int LEFT_ROOT_LEVEL = -1;
            private int currentPosition = 0;

            public AreaRef clone() {
                AreaRef areaRef = new AreaRef();
                areaRef.currentPosition = currentPosition;
                areaRef.fragLevelPosition.addAll(fragLevelPosition);
                return areaRef;
            }

            public void setTo(AreaRef areaRef) {
                this.currentPosition = areaRef.currentPosition;
                this.fragLevelPosition.clear();
                this.fragLevelPosition.addAll(areaRef.fragLevelPosition);
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

            public List<Integer> getFragLevelPosition() {
                return fragLevelPosition;
            }

            public int advance() {
                currentPosition++;
                return currentPosition;
            }
        }

        final private Fragment rootFragment;

        final private AreaRef currentFragmentRef = new AreaRef();
        final private AreaRef nextFragmentRef = new AreaRef();

        public FragmentIterator(Fragment rootFragment) {
            this.rootFragment = rootFragment;
            assert rootFragment.size() >= 1;
        }

        private FragmentIterator(Fragment clone, AreaRef currentFragmentRef, AreaRef nextFragmentRef) {
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
            currentLevel.remove(currentIndex, null);
            currentLevel.add(currentIndex, newFragment, false);
            // recalculate the next fragment position
            iterator.nextFragmentRef.setTo(iterator.currentFragmentRef);
            iterator.calculateNextFragmentPosition();
            return iterator;
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
            if (nextFragmentRef.getCurrentPosition() == AreaRef.LEFT_ROOT_LEVEL)
                return;

            // if currently on a fragment enter it
            if (peek() instanceof Fragment)
                nextFragmentRef.enterNextLevel();

            // iterate through the fragment and its parent fragments till we find the next fragment
            while (nextFragmentRef.getCurrentPosition() != AreaRef.LEFT_ROOT_LEVEL) {
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

        private Fragment getLevelFragment(Fragment root, AreaRef areaRef) {
            for (Integer index : areaRef.getFragLevelPosition())
                root = (Fragment)root.getItemAt(index);
            return root;
        }
    }

    public void addTransformation(ITransformation transformation) {
        transformations.add(transformation);
    }

    private class IntermediateResult {
        final public float quality;
        final public FragmentIterator iterator;

        public IntermediateResult(float quality, FragmentIterator iterator) {
            this.quality = quality;
            this.iterator = iterator;
        }
    }

    public List<ITransformation.Result> calculateAlternatives(Fragment fragment) {
        // put the fragment into a container fragment so that the iterator also includes the fragment
        final Fragment containerFragment = Fragment.horizontalFragment();
        containerFragment.add(fragment, false);

        final List<ITransformation.Result> results = new ArrayList<ITransformation.Result>();
        final List<IntermediateResult> ongoingTransformations = new ArrayList<IntermediateResult>();
        // add first fragment
        ongoingTransformations.add(new IntermediateResult(1, new FragmentIterator(containerFragment)));

        while (ongoingTransformations.size() > 0) {
            IntermediateResult current = ongoingTransformations.remove(0);
            FragmentIterator currentIterator = current.iterator;
            while (currentIterator.hasNext()) {
                Fragment subFragment = currentIterator.next();
                for (ITransformation transformation : transformations) {
                    List<ITransformation.Result> trafoResults = transformation.transform(subFragment);
                    for (ITransformation.Result result : trafoResults) {
                        FragmentIterator subIterator = currentIterator.cloneAndReplaceCurrent(result.fragment);
                        ongoingTransformations.add(new IntermediateResult(current.quality * result.quality,
                                subIterator));
                    }
                }
            }
            // get the fragment out of the container
            Fragment returnedFragment = (Fragment)currentIterator.getRootFragment().getItemAt(0);
            if (returnedFragment != fragment && !returnedFragment.isEquivalent(fragment)) {
                int equivalentIndex = getEquivalent(results, returnedFragment);
                if (equivalentIndex >= 0) {
                    // choose the one with lower quality
                    if (results.get(equivalentIndex).quality > current.quality) {
                        results.remove(equivalentIndex);
                        results.add(new ITransformation.Result(current.quality, returnedFragment));
                    }
                } else
                    results.add(new ITransformation.Result(current.quality, returnedFragment));
            }
        }

        return results;
    }

    private int getEquivalent(List<ITransformation.Result> results, Fragment fragment) {
        for (int i = 0; i < results.size(); i++) {
            ITransformation.Result result = results.get(i);
            if (result.fragment.isEquivalent(fragment))
                return i;
        }
        return -1;
    }
}
