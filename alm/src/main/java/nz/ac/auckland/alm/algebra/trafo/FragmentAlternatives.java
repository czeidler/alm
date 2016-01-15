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


class Result {
    final public float quality;
    final public Fragment fragment;

    public Result(float quality, Fragment fragment) {
        this.quality = quality;
        this.fragment = fragment;
    }
}

interface ITransformation {
    /**
     * Tries to transform a fragment and returns a list of possible transformations.
     * @param fragment the fragment to transform
     * @return list of possible transformations
     */
    List<Result> transform(Fragment fragment);
}

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

            public boolean leftRootLevel() {
                return currentPosition == LEFT_ROOT_LEVEL;
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
            assert rootFragment.getItems().size() >= 1;
        }

        private FragmentIterator(Fragment clone, AreaRef currentFragmentRef, AreaRef nextFragmentRef) {
            this.rootFragment = clone;
            assert rootFragment.getItems().size() >= 1;

            this.currentFragmentRef.setTo(currentFragmentRef);
            this.nextFragmentRef.setTo(nextFragmentRef);
        }

        /**
         * Clone the root fragment and return iterator that points the the same position as the current iterator.
         *
         * @return
         */
        protected FragmentIterator cloneFragment() {
            Fragment clone = FragmentAlternatives.cloneFragment(rootFragment);
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
            currentLevel.getItems().remove(currentIndex);
            currentLevel.getItems().add(currentIndex, newFragment);
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
            if (peek() instanceof Fragment)
                nextFragmentRef.enterNextLevel();

            while (nextFragmentRef.getCurrentPosition() != AreaRef.LEFT_ROOT_LEVEL) {
                Fragment levelFragment = getLevelFragment(rootFragment, nextFragmentRef);
                if (nextFragmentRef.getCurrentPosition() >= levelFragment.getItems().size()) {
                    nextFragmentRef.leaveLevel();
                    continue;
                }
                if (levelFragment.getItems().get(nextFragmentRef.getCurrentPosition()) instanceof Fragment)
                    break;

                nextFragmentRef.advance();
            }
        }

        public IArea peek() {
            Fragment currentLevel = getLevelFragment(rootFragment, currentFragmentRef);
            return (IArea)currentLevel.getItems().get(currentFragmentRef.getCurrentPosition());
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
                root = (Fragment)root.getItems().get(index);
            return root;
        }
    }

    static private Fragment cloneFragment(Fragment fragment) {
        Fragment clone = Fragment.createEmptyFragment(fragment.getDirection());
        for (IArea child : (List<IArea>)fragment.getItems()) {
            if (child instanceof Fragment)
                clone.add(cloneFragment((Fragment)child), false);
            else
                clone.add(child, false);
        }
        return clone;
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

    public List<Result> calculateAlternatives(Fragment fragment) {
        // put the fragment into a container fragment so that the iterator also includes the fragment
        final Fragment containerFragment = Fragment.horizontalFragment();
        containerFragment.add(fragment, false);

        final List<Result> results = new ArrayList<Result>();
        final List<IntermediateResult> ongoingTransformations = new ArrayList<IntermediateResult>();
        // add first fragment
        ongoingTransformations.add(new IntermediateResult(1, new FragmentIterator(containerFragment)));

        while (ongoingTransformations.size() > 0) {
            IntermediateResult current = ongoingTransformations.remove(0);
            FragmentIterator currentIterator = current.iterator;
            while (currentIterator.hasNext()) {
                Fragment subFragment = currentIterator.next();
                for (ITransformation transformation : transformations) {
                    List<Result> trafoResults = transformation.transform(subFragment);
                    for (Result result : trafoResults) {
                        FragmentIterator subIterator = currentIterator.cloneAndReplaceCurrent(result.fragment);
                        ongoingTransformations.add(new IntermediateResult(current.quality * result.quality,
                                subIterator));
                    }
                }
            }
            // get the fragment out of the container
            Fragment returnedFragment = (Fragment)currentIterator.getRootFragment().getItems().get(0);
            if (returnedFragment != fragment)
                results.add(new Result(current.quality, returnedFragment));
        }

        return results;
    }
}
