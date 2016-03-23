/*
 * Copyright 2016.
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
import java.util.List;


public class SymmetryAnalyzer {
    static public int countFragments(Fragment fragment) {
        int nFragments = 1;
        for (IArea area : (Iterable<IArea>)fragment.getItems()) {
            if (!(area instanceof Fragment))
                continue;
            nFragments += countFragments((Fragment)area);
        }
        return nFragments;
    }

    static public int countSameOrientations(Fragment fragment) {
        int sameOrientationCount = 0;
        if (childrenHaveSameOrientation(Collections.singletonList(fragment)))
            sameOrientationCount++;

        for (IArea area : (Iterable<IArea>)fragment.getItems()) {
            if (!(area instanceof Fragment))
                continue;
            sameOrientationCount += countSameOrientations((Fragment)area);
        }
        return sameOrientationCount;
    }


    static private boolean childrenHaveSameOrientation(List<Fragment> level) {
        IDirection direction = null;
        for (Fragment fragment : level) {
            for (IArea area : (Iterable<IArea>) fragment.getItems()) {
                if (!(area instanceof Fragment))
                    continue;
                IDirection currentDirection = ((Fragment) area).getDirection();
                if (direction == null) {
                    direction = currentDirection;
                    continue;
                }
                // ignore single element fragments
                if (((Fragment) area).size() == 1)
                    continue;
                if (direction != currentDirection)
                    return false;
            }
        }
        return true;
    }

    static private List<Fragment> nextLevel(List<Fragment> currentLevel) {
        List<Fragment> level = new ArrayList<Fragment>();
        for (Fragment fragment : currentLevel) {
            for (IArea area : (Iterable<IArea>) fragment.getItems()) {
                if (!(area instanceof Fragment))
                    continue;
                level.add((Fragment)area);
            }
        }
        return level;
    }

    static private boolean sameOrientationsOnAllLevels(Fragment fragment) {
        List<Fragment> level = Collections.singletonList(fragment);
        while (level.size() > 0) {
            if (!childrenHaveSameOrientation(level))
                return false;
            level = nextLevel(level);
        }
        return true;
    }

    static public int fragmentWeight(Fragment fragment) {
        return countFragments(fragment);
    }

    static public int summedFragmentWeights(Fragment fragment) {
        int weight = fragmentWeight(fragment);
        for (IArea area : (Iterable<IArea>)fragment.getItems()) {
            if (!(area instanceof Fragment))
                continue;
            weight += summedFragmentWeights((Fragment)area);
        }
        return weight;
    }

    static public int summedFragmentWeightSameOrientation(Fragment fragment) {
        int count = 0;
        if (sameOrientationsOnAllLevels(fragment))
            count += fragmentWeight(fragment);

        for (IArea area : (Iterable<IArea>)fragment.getItems()) {
            if (!(area instanceof Fragment))
                continue;
            count += summedFragmentWeightSameOrientation((Fragment)area);
        }
        return count;
    }




    static public int symmetryCountSameChildrenSize(Fragment fragment) {
        IDirection direction = null;
        int childrenCount = -1;
        for (IArea area : (Iterable<IArea>)fragment.getItems()) {
            if (!(area instanceof Fragment))
                return 0;
            Fragment current = (Fragment)area;
            if (childrenCount < 0) {
                childrenCount = current.size();
                direction = current.getDirection();
                continue;
            }

            if (current.size() != childrenCount || current.getDirection() != direction)
                return 0;
        }
        return fragment.size();
    }

    static public int totalSymmetryCountSameChildrenSize(Fragment fragment) {
        int count = symmetryCountSameChildrenSize(fragment);
        for (IArea area : (Iterable<IArea>)fragment.getItems()) {
            if (!(area instanceof Fragment))
                continue;
            count += totalSymmetryCountSameChildrenSize((Fragment)area);
        }
        return count;
    }

    static public int numberOfElementsInLevels(Fragment fragment) {
        int count = 0;
        List<Fragment> level = Collections.singletonList(fragment);
        while (level.size() > 0) {
            for (Fragment current : level) {
                count += current.size();
            }
            level = nextLevel(level);
        }
        return count;
    }

    static int levelSymmetry(List<Fragment> level) {
        int count = 0;
        for (Fragment fragment : level) {
            int current = 0;
            int childFragmentCount = 0;
            for (IArea area : (Iterable<IArea>) fragment.getItems()) {
                if (!(area instanceof Fragment))
                    continue;
                childFragmentCount++;
                Fragment child = (Fragment) area;
                if (child.getDirection() == Fragment.horizontalDirection)
                    current += child.size();
                else
                    current -= child.size();
            }
            if (childFragmentCount >= 2)
                count += Math.abs(current);
        }
        return count;
    }

    static public int levelSymmetry(Fragment fragment) {
        int count = 0;
        List<Fragment> level = Collections.singletonList(fragment);
        while (level.size() > 0) {
            count += levelSymmetry(level);
            level = nextLevel(level);
        }
        return count;
    }
}

