/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm.algebra.string;


import nz.ac.auckland.alm.IArea;
import nz.ac.auckland.alm.XTab;
import nz.ac.auckland.alm.YTab;
import nz.ac.auckland.alm.algebra.*;
import nz.ac.auckland.linsolve.Variable;

import java.util.*;


public class AlgebraSpec {
    class FragmentBucket {
        final Map<String, Fragment> bucket = new HashMap<String, Fragment>();

        /**
         * Adds a fragment to the bucket.
         *
         * @param fragment
         * @return the fragment
         */
        public Fragment addFragment(Fragment fragment) {
            String hash = fragment.hash();
            Fragment existingFragment = bucket.get(hash);
            if (existingFragment != null)
                return existingFragment;
            bucket.put(hash, fragment);
            return fragment;
        }

        public boolean removeFragment(Fragment fragment) {
            return bucket.remove(fragment.hash()) != null;
        }
    }

    class FragmentSpec {
        final Map<XTab, Edge> xEdgeMap = new HashMap<XTab, Edge>();
        final Map<YTab, Edge> yEdgeMap = new HashMap<YTab, Edge>();

        final List<IArea> atoms = new ArrayList<IArea>();

        public void addArea(IArea area) {
            Edge.addAreaChecked(area, xEdgeMap, yEdgeMap);
            atoms.add(area);
        }
    }

    class AlgebraSpecData {
        final Map<XTab, Edge> xEdgeMap;
        final Map<YTab, Edge> yEdgeMap;

        final FragmentBucket fragments = new FragmentBucket();

        public AlgebraSpecData(Map<XTab, Edge> xEdgeMap, Map<YTab, Edge> yEdgeMap) {
            this.xEdgeMap = new HashMap<XTab, Edge>(xEdgeMap);
            this.yEdgeMap = new HashMap<YTab, Edge>(yEdgeMap);
            extractFragments(xEdgeMap, new HorizontalFragmentFactory());
            extractFragments(yEdgeMap, new VerticalFragmentFactory());
        }

        public List<Fragment> getFragments() {
            return new ArrayList<Fragment>(fragments.bucket.values());
        }

        public boolean addFragment(Fragment fragment) {
            Edge.addAreaChecked(fragment, xEdgeMap, yEdgeMap);
            return fragments.addFragment(fragment) == fragment;
        }

        private <Tab extends Variable> void extractFragments(Map<Tab, Edge> edgeMap,
                                                                IFragmentFactory fragmentFactory) {
            for (Map.Entry<Tab, Edge> entry : edgeMap.entrySet()) {
                Edge edge = entry.getValue();
                for (IArea area1 : edge.areas1) {
                    if (edge.areas2.size() == 0) {
                        if (area1 instanceof Fragment) {
                            fragments.addFragment((Fragment) area1);
                        } else {
                            Fragment fragment = new Fragment();
                            fragment.add(area1, true);
                            fragments.addFragment(fragment);
                        }
                    }
                    for (IArea area2 : edge.areas2) {
                        Fragment newFragment = fragmentFactory.create(area1, area2);
                        fragments.addFragment(newFragment);
                    }
                }
            }
        }

        public void chainFragments() {
            while (chainFragments(xEdgeMap, new HorizontalFragmentFactory())
                    || chainFragments(yEdgeMap, new VerticalFragmentFactory())) continue;
        }

        private <Tab extends Variable> boolean chainFragments(Map<Tab, Edge> edgeMap,
                                                              IFragmentFactory fragmentFactory) {
            for (Edge edge : new ArrayList<Edge>(edgeMap.values())) {
                for (IArea area1 : edge.areas1) {
                    for (IArea area2 : edge.areas2) {
                        Fragment newFragment = fragmentFactory.create(area1, area2);
                        if (fragments.addFragment(newFragment) == newFragment) {
                            Edge.addAreaChecked(newFragment, xEdgeMap, yEdgeMap);
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        public void remove(IArea area) {
            Edge.removeAreaChecked(area, xEdgeMap, yEdgeMap);
            if (area instanceof Fragment)
                fragments.removeFragment((Fragment) area);
        }
    }

    interface IFragmentFactory {
        Fragment create(IArea area1, IArea area2);
    }

    class HorizontalFragmentFactory implements IFragmentFactory {
        @Override
        public Fragment create(IArea area1, IArea area2) {
            return Fragment.horizontalFragment(area1, area2);
        }
    }

    class VerticalFragmentFactory implements IFragmentFactory {
        @Override
        public Fragment create(IArea area1, IArea area2) {
            return Fragment.verticalFragment(area1, area2);
        }
    }

    final AlgebraSpecData algebraSpecData;

    public AlgebraSpec(AlgebraData data) {
        algebraSpecData = new AlgebraSpecData(data.getXTabEdges(), data.getYTabEdges());
    }

    public List<Fragment> getFragments() {
        return algebraSpecData.getFragments();
    }

    public void compress() {
        singleMerge();
        multiMerge();
        algebraSpecData.chainFragments();
        removeRedundant();
    }

    private void removeRedundant() {
        List<Fragment> fragments = algebraSpecData.getFragments();
        if (fragments.size() <= 1)
            return;
        Collections.sort(fragments, new Comparator<Fragment>() {
            @Override
            public int compare(Fragment fragment, Fragment fragment2) {
                Integer count1 = fragment.countAtoms();
                Integer count2 = fragment2.countAtoms();
                return count2.compareTo(count1);
            }
        });

        Map<Fragment, FragmentSpec> fragmentSpecMap = new HashMap<Fragment, FragmentSpec>();
        for (Fragment fragment : fragments)
            fragmentSpecMap.put(fragment, getFragmentSpec(Collections.singletonList(fragment)));

        for (int i = 1; i < fragments.size(); i++) {
            Fragment fragment = fragments.get(i);
            FragmentSpec fragmentSpec = fragmentSpecMap.get(fragment);
            int total = countConstraints(fragmentSpec);
            int intersection = 0;
            for (int a = 0; a < i; a++) {
                Fragment previousFragment = fragments.get(a);
                FragmentSpec prevFragmentSpec = fragmentSpecMap.get(previousFragment);
                intersection += countSpecIntersection(prevFragmentSpec, fragmentSpec);
            }

            if (total - intersection <= intersection) {
                fragments.remove(i);
                i--;
                algebraSpecData.remove(fragment);
            }
        }
    }

    private int countConstraints(FragmentSpec fragmentSpec) {
        return countConstraints(fragmentSpec.xEdgeMap) + countConstraints(fragmentSpec.yEdgeMap);
    }

    private <Tab extends Variable> int countConstraints(Map<Tab, Edge> map) {
        int count = 0;
        for (Edge edge : map.values())
            count += edge.areas1.size() * edge.areas2.size();
        return count;
    }

    private int countSpecIntersection(FragmentSpec fragmentSpec1, FragmentSpec fragmentSpec2) {
        if (fragmentSpec2.atoms.size() == 1 && fragmentSpec1.atoms.contains(fragmentSpec2.atoms.get(0)))
            return 1;
        return countSpecIntersection(fragmentSpec1.xEdgeMap, fragmentSpec2.xEdgeMap)
                + countSpecIntersection(fragmentSpec1.yEdgeMap, fragmentSpec2.yEdgeMap);
    }

    private <Tab extends Variable> int countSpecIntersection(Map<Tab, Edge> map1, Map<Tab, Edge> map2) {
        int count = 0;
        for (Map.Entry<Tab, Edge> entry : map1.entrySet()) {
            Tab tab = entry.getKey();
            Edge edge1 = entry.getValue();
            Edge edge2 = map2.get(tab);
            if (edge2 == null)
                continue;
            for (IArea area1 : edge1.areas1) {
                for (IArea area2 : edge1.areas2) {
                    // does edge2 has the same pair?
                    if (edge2.areas1.contains(area1) && edge2.areas2.contains(area2))
                        count++;
                }
            }
        }
        return count;
    }

    private FragmentSpec getFragmentSpec(List<Fragment> fragments) {
        FragmentSpec specData = new FragmentSpec();
        List<Fragment> dirtyFragments = new ArrayList<Fragment>(fragments);
        while (dirtyFragments.size() > 0) {
            Fragment f = dirtyFragments.remove(0);
            for (IArea child : (Iterable<IArea>)f.getItems()) {
                if (child instanceof Fragment)
                    dirtyFragments.add((Fragment)child);
                else
                    specData.addArea(child);
            }
        }
        return specData;
    }

    /**
     * Merge stuff like A|B => C
     */
    private void singleMerge() {
        IFragmentFactory vFactory = new VerticalFragmentFactory();
        IFragmentFactory hFactory = new HorizontalFragmentFactory();
        boolean merged = true;
        while (merged) {
            merged = false;
            for (Edge edge : algebraSpecData.xEdgeMap.values()) {
                if (singleMergeOnEdgeAll(edge, new LeftDirection(), vFactory))
                    merged = true;
                if (singleMergeOnEdgeAll(edge, new RightDirection(), vFactory))
                    merged = true;
            }
            for (Edge edge : algebraSpecData.yEdgeMap.values()) {
                if (singleMergeOnEdgeAll(edge, new TopDirection(), hFactory))
                    merged = true;
                if (singleMergeOnEdgeAll(edge, new BottomDirection(), hFactory))
                    merged = true;
            }
        }
    }

    private <Tab extends Variable, OrthTab extends Variable>
    boolean singleMergeOnEdgeAll(Edge edge, IDirection<Tab, OrthTab> direction, IFragmentFactory fragmentFactory) {
        boolean merged = false;
        while (singleMergeOnEdge(edge, direction, fragmentFactory)) {
            merged = true;
            continue;
        }
        return merged;
    }

    private <Tab extends Variable, OrthTab extends Variable>
    boolean singleMergeOnEdge(Edge edge, IDirection<Tab, OrthTab> direction, IFragmentFactory fragmentFactory) {
        List<IArea> areas = direction.getAreas(edge);
        for (int i = 0; i < areas.size(); i++) {
            IArea area1 = areas.get(i);
            for (int a = 0; a < areas.size() && a != i; a++) {
                IArea area2 = areas.get(a);
                if (direction.getTab(area1) != direction.getTab(area2))
                    continue;
                Fragment fragment = null;
                if (direction.getOrthogonalTab1(area1) == direction.getOrthogonalTab2(area2))
                    fragment = fragmentFactory.create(area2, area1);
                else if (direction.getOrthogonalTab2(area1) == direction.getOrthogonalTab1(area2))
                    fragment = fragmentFactory.create(area1, area2);

                if (fragment == null)
                    continue;

                algebraSpecData.addFragment(fragment);
                algebraSpecData.remove(area1);
                algebraSpecData.remove(area2);
                return true;
            }
        }
        return false;
    }

    private <Tab extends Variable, OrthTab extends Variable>
    int chainSort(List<IArea> areas, IArea start, IDirection<Tab, OrthTab> direction,
                  IDirection<OrthTab, Tab> orthDirection) {
        IDirection<Tab, OrthTab> oppositeDirection = direction.getOppositeDirection();
        int chainLength = 1;
        IArea current = start;
        for (int i = 0; i < areas.size(); i++) {
            IArea area = areas.get(i);
            if (area == start || area == current)
                continue;
            if (getTab(current, direction, orthDirection) == getTab(area, oppositeDirection, orthDirection)) {
                areas.remove(i);
                areas.add(areas.indexOf(current) + 1, area);
                chainLength ++;
                current = area;
                i = -1;
            }
        }
        return chainLength;
    }

    /**
     * Merge stuff like: A | (B / C) => A | D with right of B != right of C
     */
    private void multiMerge() {
        Map<XTab, Edge> xEdgeMap = algebraSpecData.xEdgeMap;
        Map<YTab, Edge> yEdgeMap = algebraSpecData.yEdgeMap;
        IFragmentFactory vFactory = new VerticalFragmentFactory();
        IFragmentFactory hFactory = new HorizontalFragmentFactory();
        boolean merged = true;
        while (merged) {
            merged = false;
            List<Edge> xEdges = new ArrayList<Edge>(xEdgeMap.values());
            for (int i = 0; i < xEdges.size(); i++) {
                Edge edge = xEdges.get(i);
                if (multiMergeOnEdge(edge, new LeftDirection(), xEdgeMap, hFactory, vFactory)) {
                    merged = true;
                    break;
                }
                if (multiMergeOnEdge(edge, new RightDirection(), xEdgeMap, hFactory, vFactory)) {
                    merged = true;
                    break;
                }
            }
            List<Edge> yEdges = new ArrayList<Edge>(yEdgeMap.values());
            for (int i = 0; i < yEdges.size(); i++) {
                Edge edge = yEdges.get(i);
                if (multiMergeOnEdge(edge, new TopDirection(), yEdgeMap, vFactory, hFactory)) {
                    merged = true;
                    break;
                }
                if (multiMergeOnEdge(edge, new BottomDirection(), yEdgeMap, vFactory, hFactory)) {
                    merged = true;
                    break;
                }
            }
        }
    }

    private <Tab extends Variable, OrthTab extends Variable>
    boolean multiMergeOnEdge(Edge edge, IDirection<Tab, OrthTab> direction, Map<Tab, Edge> map,
                             IFragmentFactory factory, IFragmentFactory orthFactory) {
        List<IArea> areas = direction.getOppositeAreas(edge);
        boolean merged = false;
        for (int a = 0; a < areas.size(); a++) {
            IArea area1 = areas.get(a);
            List<IArea> neighbours = findAlignedNeighbours(area1, direction, map);
            if (neighbours == null)
                continue;

            Fragment neighbourFragment = orthFactory.create(neighbours.get(0), null);
            for (int i = 1; i < neighbours.size(); i++)
                neighbourFragment.add(neighbours.get(i), true);

            Fragment mergedFragment;
            if (direction instanceof LeftDirection || direction instanceof TopDirection)
                mergedFragment = factory.create(neighbourFragment, area1);
            else
                mergedFragment = factory.create(area1, neighbourFragment);

            merged = algebraSpecData.addFragment(mergedFragment);
            break;
        }
        return merged;
    }

    private <Tab extends Variable, OrthTab extends Variable>
    Tab getTab(IArea area, IDirection<Tab, OrthTab> direction, IDirection<OrthTab, Tab> orthDirection) {
        Tab tab = direction.getTab(area);
        return tab;
    }

    private <Tab extends Variable, OrthTab extends Variable>
    List<IArea> findAlignedNeighbours(IArea start, IDirection<Tab, OrthTab> direction, Map<Tab, Edge> tabMap) {
        IDirection<Tab, OrthTab> oppositeDirection = direction.getOppositeDirection();
        IDirection<OrthTab, Tab> orthDirection1 = direction.getOrthogonalDirection1();
        IDirection<OrthTab, Tab> orthDirection2 = direction.getOrthogonalDirection2();

        if (orthDirection1.getTab(start) == null || orthDirection2.getTab(start) == null)
            return null;

        Edge edge = tabMap.get(direction.getTab(start));
        List<IArea> neighbours = direction.getAreas(edge);

        IArea startNeighbour = null;
        for (IArea area : neighbours) {
            if (getTab(area, orthDirection1, oppositeDirection) == getTab(start, orthDirection1, direction)) {
                startNeighbour = area;
                break;
            }
        }
        if (startNeighbour == null)
            return null;

        int chainLength = chainSort(neighbours, startNeighbour, orthDirection2, oppositeDirection);
        List<IArea> outList = new ArrayList<IArea>();
        int startIndex = neighbours.indexOf(startNeighbour);
        OrthTab endTab = getTab(start, orthDirection2, direction);
        for (int i = startIndex; i < startIndex + chainLength; i++) {
            IArea area = neighbours.get(i);
            outList.add(area);

            OrthTab currentTab = getTab(area, orthDirection2, oppositeDirection);
            if (currentTab == endTab) {
                return outList;
            }
        }
        return null;
    }
}
