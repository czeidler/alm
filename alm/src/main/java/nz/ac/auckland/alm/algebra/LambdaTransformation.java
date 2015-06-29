/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm.algebra;


import nz.ac.auckland.alm.EmptySpace;
import nz.ac.auckland.alm.IArea;
import nz.ac.auckland.alm.XTab;
import nz.ac.auckland.alm.YTab;
import nz.ac.auckland.linsolve.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LambdaTransformation {
    LayoutStructure layoutStructure;

    public LambdaTransformation(LayoutStructure layoutStructure) {
        this.layoutStructure = layoutStructure;
    }

    public EmptySpace makeSpace(XTab left, YTab top, XTab right, YTab bottom) {
        Map<XTab, Edge> xTabEdgeMap = layoutStructure.getXTabEdges();
        Map<YTab, Edge> yTabEdgeMap = layoutStructure.getYTabEdges();
        IDirection rightDirection = new RightDirection();
        IDirection bottomDirection = new BottomDirection();

        EmptySpace emptySpace = findEmptySpaceAtCorner(left, top, new LeftDirection(), new TopDirection());
        if (emptySpace == null) {
            // try to create one
            for (int i = 0; i < layoutStructure.getEmptySpaces().size(); i++) {
                EmptySpace space = layoutStructure.getEmptySpaces().get(i);
                if (space.getLeft().getValue() <= left.getValue() && space.getRight().getValue() >= left.getValue()
                    && space.getTop().getValue() <= top.getValue() && space.getBottom().getValue() >= top.getValue()) {
                    EmptySpace orgSpace = space;
                    if (space.getLeft() != left) {
                        space = split(space, left, xTabEdgeMap, rightDirection);
                        if (space == null)
                            continue;
                    }
                    if (space.getTop() != top) {
                        emptySpace = split(space, top, yTabEdgeMap, new BottomDirection());
                        if (emptySpace == null && orgSpace != space) {
                            merge(orgSpace, space, new RightDirection());
                        } else
                            break;
                    }
                }
            }
            if (emptySpace == null)
                return null;
        }

        // empty space is larger?
        EmptySpace split = null;
        if (emptySpace.getRight() != right && right.getValue() <= emptySpace.getRight().getValue()) {
            split = split(emptySpace, right, xTabEdgeMap, rightDirection);
            if (split == null)
                return null;
        }
        if (emptySpace.getBottom() != bottom && bottom.getValue() <= emptySpace.getBottom().getValue()) {
            if (split(emptySpace, bottom, yTabEdgeMap, bottomDirection) == null) {
                if (split != null)
                    merge(emptySpace, split, rightDirection);
                return null;
            }
        }

        if (!extend(emptySpace, right, rightDirection, xTabEdgeMap, bottomDirection, yTabEdgeMap))
            return null;
        if (!extend(emptySpace, bottom, bottomDirection, yTabEdgeMap, rightDirection, xTabEdgeMap))
            return null;

        return emptySpace;
    }

    private <Tab extends Variable, OrthTab extends Variable> boolean extend(EmptySpace space, Tab targetTab,
                                                                            IDirection direction,
                                                                            Map<Tab, Edge> tabMap,
                                                                            IDirection orthDirection,
                                                                            Map<OrthTab, Edge> orthTabMap) {
        if (direction.getTab(space) == targetTab)
            return true;
        Tab currentXTab = (Tab)direction.getTab(space);
        while (currentXTab != targetTab) {
            if (!extend(space, direction, tabMap, orthDirection, orthTabMap))
                return false;
            currentXTab = (Tab)direction.getTab(space);
        }
        return true;
    }

    public <Tab extends Variable, OrthTab extends Variable> boolean extend(EmptySpace space, IDirection direction,
                                                                            Map<Tab, Edge> tabMap,
                                                                            IDirection orthDirection,
                                                                            Map<OrthTab, Edge> orthTabMap) {
        // Example: extend the empty space to the right (orthogonal direction is bottom):
        // 1) get intersecting, direct neighbours to the right
        // 2) align top and bottom neighbours with the initial empty space area
        // 3) align the right of all neighbours
        // 4) 2 and 3 results in an aligned, rectangular box of empty spaces, merge this box into a single empty space
        // 5) merge the initial empty space with the right neighbour

        List<EmptySpace> neighbours = collectIntersectingNeighbours(space, direction, orthDirection, tabMap);
        if (neighbours == null)
            return false;
        if (!alignNeighboursEnds(space, neighbours, orthDirection, orthTabMap))
            return false;
        if (!alignNeighbours(neighbours, direction, tabMap))
            return false;
        if (!mergeLine(neighbours, orthDirection))
            return false;
        if (!merge(space, neighbours.get(0), direction))
            return false;
        return true;
    }

    /**
     * Gets direct EmptySpace neighbours that "intersect" with the start area
     *
     * @param start
     * @param direction
     * @param orthDirection
     * @param tabMap
     * @param <Tab>
     * @return
     */
    private <Tab extends Variable> List<EmptySpace> collectIntersectingNeighbours(IArea start, IDirection direction,
                                                                                  IDirection orthDirection,
                                                                                  Map<Tab, Edge> tabMap) {
        assert (direction instanceof RightDirection) || (direction instanceof BottomDirection);
        assert (orthDirection instanceof RightDirection) || (orthDirection instanceof BottomDirection);

        Edge edge = tabMap.get(direction.getTab(start));
        List<IArea> areas = direction.getAreas(edge);
        EmptySpace startNeighbour = null;
        // exact match?
        for (IArea area : areas) {
            if (!isEmptySpace(area))
                continue;
            if (orthDirection.getOppositeTab(area) == orthDirection.getOppositeTab(start)) {
                startNeighbour = (EmptySpace)area;
                break;
            }
        }
        if (startNeighbour == null) {
            Variable startTab = orthDirection.getOppositeTab(start);
            for (IArea area : areas) {
                if (!isEmptySpace(area))
                    continue;
                if (orthDirection.getOppositeTab(area).getValue() <= startTab.getValue()
                        && orthDirection.getTab(area).getValue() > startTab.getValue()) {
                    startNeighbour = (EmptySpace)area;
                    break;
                }
            }
        }
        if (startNeighbour == null)
            return null;

        int chainLength = emptyElementChainSort(areas, startNeighbour, orthDirection);
        List<EmptySpace> outList = new ArrayList<EmptySpace>();
        int startIndex = areas.indexOf(startNeighbour);
        Variable endTab = orthDirection.getTab(start);
        boolean chainFound = false;
        for (int i = startIndex; i < startIndex + chainLength; i++) {
            EmptySpace area = (EmptySpace)areas.get(i);
            outList.add(area);

            Variable currentTab = orthDirection.getTab(area);
            if (currentTab == endTab) {
                chainFound = true;
                break;
            }
            if (currentTab.getValue() > endTab.getValue()) {
                chainFound = true;
                break;
            }
        }
        if (!chainFound)
            return null;
        assert (outList.size() > 0);

        return outList;
    }

    private <OrthTab extends Variable> boolean alignNeighboursEnds(EmptySpace space, List<EmptySpace> neighbours,
                                                                   IDirection orthDirection,
                                                                   Map<OrthTab, Edge> orthTabMap) {
        // cut both ends
        EmptySpace firstNeighbour = neighbours.get(0);
        if (orthDirection.getOppositeTab(firstNeighbour) != orthDirection.getOppositeTab(space)) {
            EmptySpace newEmptySpace = split(firstNeighbour, (OrthTab)orthDirection.getOppositeTab(space), orthTabMap,
                    orthDirection);
            if (newEmptySpace == null)
                return false;
            neighbours.remove(0);
            neighbours.add(0, newEmptySpace);
        }
        EmptySpace lastNeighbour = neighbours.get(neighbours.size() - 1);
        if (orthDirection.getTab(lastNeighbour) != orthDirection.getTab(space)) {
            EmptySpace newEmptySpace = split(lastNeighbour, (OrthTab)orthDirection.getTab(space), orthTabMap,
                    orthDirection);
            if (newEmptySpace == null)
                return false;
            // the neighbours list stays valid because the newEmptySpace is no neighbour anymore
        }
        return true;
    }

    private <Tab extends Variable> boolean alignNeighbours(List<EmptySpace> neighbours, IDirection direction,
                                                           Map<Tab, Edge> tabMap) {
        // find shortest neighbour
        double minDistance = Float.MAX_VALUE;
        Tab minDistanceTab = null;
        for (EmptySpace neighbour : neighbours) {
            double distance = Math.abs(direction.getTab(neighbour).getValue()
                    - direction.getOppositeTab(neighbour).getValue());
            if (distance < minDistance) {
                minDistance = distance;
                minDistanceTab = (Tab)direction.getTab(neighbour);
            }
        }
        if (minDistanceTab == null)
            return false;
        for (EmptySpace neighbour : neighbours) {
            if (direction.getTab(neighbour) == minDistanceTab)
                continue;
            EmptySpace newEmptySpace = split(neighbour, minDistanceTab, tabMap, direction);
            if (newEmptySpace == null)
                return false;
        }
        return true;
    }

    private boolean mergeLine(List<EmptySpace> line, IDirection direction) {
        while (line.size() > 1) {
            EmptySpace area1 = line.get(0);
            EmptySpace area2 = line.get(1);
            if (!merge(area1, area2, direction))
                return false;
            line.remove(area2);
        }
        return true;
    }

    /**
     * Merge area2 into area1.
     *
     * @param area1
     * @param area2
     * @param direction must point from area1 to area2
     * @return
     */
    private boolean merge(EmptySpace area1, EmptySpace area2, IDirection direction) {
        assert (direction.getTab(area1) == direction.getOppositeTab(area2));

        if (direction.getOrthogonalTab1(area1) != direction.getOrthogonalTab1(area2))
            return false;
        if (direction.getOrthogonalTab2(area1) != direction.getOrthogonalTab2(area2))
            return false;

        layoutStructure.removeArea(area2);
        layoutStructure.removeArea(area1);
        direction.setTab(area1, direction.getTab(area2));
        layoutStructure.addArea(area1);
        return true;
    }

    /**
     * Splits an existing EmptySpace. The new EmptySpace is added in direction of direction.
     *
     * @param space
     * @param splitTab
     * @param tabMap
     * @param direction
     * @param <Tab>
     * @return
     */
    private <Tab extends Variable> EmptySpace split(EmptySpace space, Tab splitTab, Map<Tab, Edge> tabMap,
                                                 IDirection direction) {
        Tab spaceTab = (Tab)direction.getTab(space);
        Tab oppositeSpaceTab = (Tab)direction.getOppositeTab(space);
        if (Edge.isInChain(spaceTab, splitTab, tabMap, direction))
            return null;
        if (Edge.isInChain(oppositeSpaceTab, splitTab, tabMap, direction.getOppositeDirection()))
            return null;

        layoutStructure.removeArea(space);

        direction.setTab(space, splitTab);

        EmptySpace newEmptySpace = new EmptySpace();
        direction.setOppositeTab(newEmptySpace, splitTab);
        direction.setTab(newEmptySpace, spaceTab);
        direction.setOrthogonalTab1(newEmptySpace, direction.getOrthogonalTab1(space));
        direction.setOrthogonalTab2(newEmptySpace, direction.getOrthogonalTab2(space));

        // update the layout structure
        layoutStructure.addArea(space);
        layoutStructure.addArea(newEmptySpace);

        return newEmptySpace;
    }

    private int emptyElementChainSort(List<IArea> areas, EmptySpace start, IDirection direction) {
        int chainLength = 1;
        for (int i = 0; i < areas.size(); i++) {
            IArea area = areas.get(i);
            if (area == start)
                continue;
            if (!isEmptySpace(area))
                continue;
            if (direction.getTab(start) == direction.getOppositeTab(area)) {
                areas.remove(i);
                areas.add(areas.indexOf(start) + 1, area);
                chainLength += emptyElementChainSort(areas, (EmptySpace)area, direction);
            }
        }
        return chainLength;
    }

    private EmptySpace findEmptySpaceAtCorner(XTab xTab, YTab yTab, IDirection hDirection, IDirection vDirection) {
        Map<XTab, Edge> xTabEdgeMap = layoutStructure.getXTabEdges();

        List<IArea> candidates = hDirection.getOppositeAreas(xTabEdgeMap.get(xTab));
        for (IArea candidate : candidates) {
            if (!isEmptySpace(candidate))
                continue;
            if (vDirection.getTab(candidate) == yTab)
                return (EmptySpace)candidate;
        }
        return null;
    }

    private boolean isEmptySpace(IArea area) {
        return area instanceof EmptySpace;
    }
}

