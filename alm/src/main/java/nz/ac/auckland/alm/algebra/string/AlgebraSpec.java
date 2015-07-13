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
    final Map<XTab, Edge> xEdgeMap;
    final Map<YTab, Edge> yEdgeMap;

    List<Term> horizontalTerms = null;
    List<Term> verticalTerms = null;
    List<Term> singleTerms = null;

    interface ITermFactory {
        Term create(IArea area1, IArea area2);
    }

    class HorizontalTermFactory implements ITermFactory {
        @Override
        public Term create(IArea area1, IArea area2) {
            return Term.horizontalTerm(area1, area2);
        }
    }

    class VerticalTermFactory implements ITermFactory {
        @Override
        public Term create(IArea area1, IArea area2) {
            return Term.verticalTerm(area1, area2);
        }
    }

    public AlgebraSpec(AlgebraData data) {
        xEdgeMap = new HashMap<XTab, Edge>(data.getXTabEdges());
        yEdgeMap = new HashMap<YTab, Edge>(data.getYTabEdges());
    }

    public List<IArea> getTerms() {
        if (horizontalTerms == null) {
            horizontalTerms = new ArrayList<Term>();
            verticalTerms = new ArrayList<Term>();
            singleTerms = new ArrayList<Term>();
            addTerms(xEdgeMap, horizontalTerms, new HorizontalTermFactory());
            addTerms(yEdgeMap, verticalTerms, new VerticalTermFactory());

            // clean up single terms
            for (int i = 0; i < singleTerms.size(); i++) {
                Term singleTerm = singleTerms.get(i);
                for (Term term : new JoinedList<Term>(verticalTerms, horizontalTerms)) {
                    if (term == singleTerm || term.hasSubTerm(singleTerm) || (singleTerm.getItems().size() == 1
                            && term.hasAtom((IArea)singleTerm.getItems().get(0)))) {
                        singleTerms.remove(i);
                        i--;
                        break;
                    }
                }
            }
        }
        ArrayList<IArea> out = new ArrayList<IArea>();
        out.addAll(horizontalTerms);
        out.addAll(verticalTerms);
        out.addAll(singleTerms);
        return out;
    }

    private void invalidateTerms() {
        horizontalTerms = null;
        verticalTerms = null;
        singleTerms = null;
    }

    private <Tab extends Variable> void addTerms(Map<Tab, Edge> edgeMap, List<Term> terms,
                                                                           ITermFactory termFactory) {
        for (Map.Entry<Tab, Edge> entry : edgeMap.entrySet()) {
            Edge edge = entry.getValue();
            for (IArea area1 : edge.areas1) {
                if (edge.areas2.size() == 0) {
                    if (area1 instanceof Term) {
                        if (!singleTerms.contains(area1))
                            singleTerms.add((Term) area1);
                    } else
                        singleTerms.add(termFactory.create(area1, null));
                }
                for (IArea area2 : edge.areas2)
                    terms.add(termFactory.create(area1, area2));
            }
        }
    }

    public void compress() {
        singleMerge();
        multiMerge();
    }

    /**
     * Merge stuff like A|B => C
     */
    private void singleMerge() {
        ITermFactory vFactory = new VerticalTermFactory();
        ITermFactory hFactory = new HorizontalTermFactory();
        boolean merged = true;
        while (merged) {
            merged = false;
            for (Edge edge : xEdgeMap.values()) {
                if (singleMergeOnEdgeAll(edge, new LeftDirection(), vFactory))
                    merged = true;
                if (singleMergeOnEdgeAll(edge, new RightDirection(), vFactory))
                    merged = true;
            }
            for (Edge edge : yEdgeMap.values()) {
                if (singleMergeOnEdgeAll(edge, new TopDirection(), hFactory))
                    merged = true;
                if (singleMergeOnEdgeAll(edge, new BottomDirection(), hFactory))
                    merged = true;
            }
        }
    }

    private <Tab extends Variable, OrthTab extends Variable>
    boolean singleMergeOnEdgeAll(Edge edge, IDirection<Tab, OrthTab> direction, ITermFactory termFactory) {
        boolean merged = false;
        while (singleMergeOnEdge(edge, direction, termFactory)) {
            merged = true;
            continue;
        }
        return merged;
    }

    private <Tab extends Variable, OrthTab extends Variable>
    boolean singleMergeOnEdge(Edge edge, IDirection<Tab, OrthTab> direction, ITermFactory termFactory) {
        List<IArea> areas = direction.getAreas(edge);
        for (int i = 0; i < areas.size(); i++) {
            IArea area1 = areas.get(i);
            for (int a = 0; a < areas.size() && a != i; a++) {
                IArea area2 = areas.get(a);
                if (direction.getTab(area1) != direction.getTab(area2))
                    continue;
                Term term = null;
                if (direction.getOrthogonalTab1(area1) == direction.getOrthogonalTab2(area2))
                    term = termFactory.create(area2, area1);
                else if (direction.getOrthogonalTab2(area1) == direction.getOrthogonalTab1(area2))
                    term = termFactory.create(area1, area2);

                if (term == null)
                    continue;

                Edge.addArea(term, xEdgeMap, yEdgeMap);
                Edge.removeArea(area1, xEdgeMap, yEdgeMap);
                Edge.removeArea(area2, xEdgeMap, yEdgeMap);
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
        ITermFactory vFactory = new VerticalTermFactory();
        ITermFactory hFactory = new HorizontalTermFactory();
        boolean merged = true;
        while (merged) {
            merged = false;
            List<Edge> xEdges = new ArrayList<Edge>(xEdgeMap.values());
            for (int i = 0; i < xEdges.size(); i++) {
                Edge edge = xEdges.get(i);
                if (multiMergeOnEdge(edge, new LeftDirection(), xEdgeMap, yEdgeMap, hFactory, vFactory)) {
                    merged = true;
                    break;
                }
                if (multiMergeOnEdge(edge, new RightDirection(), xEdgeMap, yEdgeMap, hFactory, vFactory)) {
                    merged = true;
                    break;
                }
            }
            List<Edge> yEdges = new ArrayList<Edge>(yEdgeMap.values());
            for (int i = 0; i < yEdges.size(); i++) {
                Edge edge = yEdges.get(i);
                if (multiMergeOnEdge(edge, new TopDirection(), yEdgeMap, xEdgeMap, vFactory, hFactory)) {
                    merged = true;
                    break;
                }
                if (multiMergeOnEdge(edge, new BottomDirection(), yEdgeMap, xEdgeMap, vFactory, hFactory)) {
                    merged = true;
                    break;
                }
            }
        }
    }

    private <Tab extends Variable, OrthTab extends Variable>
    boolean multiMergeOnEdge(Edge edge, IDirection<Tab, OrthTab> direction, Map<Tab, Edge> map,
                             Map<OrthTab, Edge> orthMap, ITermFactory factory, ITermFactory orthFactory) {
        IDirection<Tab, OrthTab> oppositeDirection = direction.getOppositeDirection();
        IDirection<OrthTab, Tab> orthDirection1 = direction.getOrthogonalDirection1();
        IDirection<OrthTab, Tab> orthDirection2 = direction.getOrthogonalDirection2();
        List<IArea> areas = direction.getOppositeAreas(edge);
        boolean merged = false;
        for (int a = 0; a < areas.size(); a++) {
            IArea area1 = areas.get(a);
            List<IArea> neighbours = findAlignedNeighbours(area1, direction, map);
            if (neighbours == null)
                continue;

            Term neighbourTerm = orthFactory.create(neighbours.get(0), null);
            for (int i = 1; i < neighbours.size(); i++)
                neighbourTerm.add(neighbours.get(i));

            Term mergedTerm;
            if (direction instanceof LeftDirection || direction instanceof TopDirection)
                mergedTerm = factory.create(neighbourTerm, area1);
            else
                mergedTerm = factory.create(area1, neighbourTerm);

            Edge.removeAreaChecked(area1, xEdgeMap, yEdgeMap);
            a--;
            Edge.addAreaChecked(mergedTerm, xEdgeMap, yEdgeMap);
            // remove neighbours
            for (int i = 0; i < neighbours.size(); i++) {
                IArea neighbour = neighbours.get(i);
                //if (oppositeDirection.getTab(neighbour) != null)
                    Edge.removeArea(neighbour, oppositeDirection, map);

                if (i == 0 || (orthDirection1.getTab(neighbour) != null
                        && orthDirection1.getEdge(neighbour, orthMap).areas2.size() > 1))
                    Edge.removeArea(neighbour, orthDirection1, orthMap);

                if (i == neighbours.size() - 1 || (orthDirection2.getTab(neighbour) != null
                    && orthDirection2.getEdge(neighbour, orthMap).areas1.size() > 1))
                    Edge.removeArea(neighbour, orthDirection2, orthMap);
            }

            merged = true;
            break;
        }
        return merged;
    }

    private <Tab extends Variable, OrthTab extends Variable>
    Tab getTab(IArea area, IDirection<Tab, OrthTab> direction, IDirection<OrthTab, Tab> orthDirection) {
        Tab tab = direction.getTab(area);
        return tab;
        /*if (tab != null)
            return tab;

        // it must be a term
        Term term = (Term) area;
        IArea atom;
        if (orthDirection instanceof BottomDirection || orthDirection instanceof RightDirection)
            atom = term.getLastAtom();
        else
            atom = term.getFirstAtom();
        return direction.getTab(atom);*/
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
