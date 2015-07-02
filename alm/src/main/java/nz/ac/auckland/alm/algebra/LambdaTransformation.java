/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm.algebra;


import nz.ac.auckland.alm.*;
import nz.ac.auckland.linsolve.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LambdaTransformation {
    AlgebraData algebraData;

    public LambdaTransformation(AlgebraData algebraData) {
        this.algebraData = algebraData;
    }

    public EmptySpace makeSpace(XTab left, YTab top, XTab right, YTab bottom) {
        Map<XTab, Edge> xTabEdgeMap = algebraData.getXTabEdges();
        Map<YTab, Edge> yTabEdgeMap = algebraData.getYTabEdges();
        IDirection rightDirection = new RightDirection();
        IDirection bottomDirection = new BottomDirection();

        EmptySpace emptySpace = findEmptySpaceAtCorner(left, top, new LeftDirection(), new TopDirection());
        if (emptySpace == null) {
            // try to create one
            List<EmptySpace> emptySpaces = new ArrayList<EmptySpace>(algebraData.getEmptySpaces());
            for (EmptySpace space : emptySpaces) {
                if (space.getLeft().getValue() <= left.getValue() && space.getRight().getValue() >= left.getValue()
                    && space.getTop().getValue() <= top.getValue() && space.getBottom().getValue() >= top.getValue()) {
                    EmptySpace orgSpace = space;

                    if (space.getLeft() != left) {
                        space = TilingAlgebra.split(algebraData, space, left, xTabEdgeMap, rightDirection);
                        if (space == null)
                            continue;
                    }
                    if (space.getTop() != top) {
                        emptySpace = TilingAlgebra.split(algebraData, space, top, yTabEdgeMap,
                                new BottomDirection());
                        if (emptySpace == null && orgSpace != space) {
                            TilingAlgebra.merge(algebraData, orgSpace, space, new RightDirection());
                        } else
                            break;
                    } else {
                        emptySpace = space;
                        break;
                    }
                }
            }
            if (emptySpace == null)
                return null;
        }

        if (!resize(emptySpace, right, rightDirection, xTabEdgeMap, bottomDirection, yTabEdgeMap))
            return null;
        if (!resize(emptySpace, bottom, bottomDirection, yTabEdgeMap, rightDirection, xTabEdgeMap))
            return null;

        return emptySpace;
    }

    private <Tab extends Variable, OrthTab extends Variable> boolean resize(EmptySpace space, Tab targetTab,
                                                                            IDirection direction,
                                                                            Map<Tab, Edge> tabMap,
                                                                            IDirection orthDirection,
                                                                            Map<OrthTab, Edge> orthTabMap) {
        if (direction.getTab(space) == targetTab)
            return true;
        int compareFactor = 1;
        if (direction instanceof LeftDirection || direction instanceof TopDirection)
            compareFactor = -1;
        Tab currentXTab = (Tab)direction.getTab(space);
        while (currentXTab != targetTab) {
            // are we already too large?
            if (compareFactor * (currentXTab.getValue() - targetTab.getValue()) >= 0) {
                if (TilingAlgebra.split(algebraData, space, targetTab, tabMap, direction) == null)
                    return false;
                else
                    return true;
            }
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

        List<EmptySpace> neighbours = collectIntersectingNeighbours(space, direction, tabMap, orthDirection,
                orthTabMap);
        if (neighbours == null)
            return false;
        if (!alignNeighboursEnds(space, neighbours, orthDirection, orthTabMap))
            return false;
        if (!alignNeighbours(neighbours, direction, tabMap))
            return false;
        if (!mergeLine(neighbours, orthDirection))
            return false;
        if (!TilingAlgebra.merge(algebraData, space, neighbours.get(0), direction))
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
    private <Tab extends Variable, OrthTab extends Variable>
    List<EmptySpace> collectIntersectingNeighbours(IArea start, IDirection direction, Map<Tab, Edge> tabMap,
                                                   IDirection orthDirection, Map<OrthTab, Edge> orthMap) {
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
            if (LayoutSpec.fuzzyEquals(currentTab, endTab)) {
                chainFound = true;
                if (Edge.isInChain(endTab, currentTab, orthMap, orthDirection))
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
            EmptySpace newEmptySpace = TilingAlgebra.split(algebraData, firstNeighbour,
                    (OrthTab) orthDirection.getOppositeTab(space), orthTabMap, orthDirection);
            if (newEmptySpace == null)
                return false;
            neighbours.remove(0);
            neighbours.add(0, newEmptySpace);
        }
        EmptySpace lastNeighbour = neighbours.get(neighbours.size() - 1);
        if (orthDirection.getTab(lastNeighbour) != orthDirection.getTab(space)) {
            EmptySpace newEmptySpace = TilingAlgebra.split(algebraData, lastNeighbour,
                    (OrthTab) orthDirection.getTab(space), orthTabMap, orthDirection);
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
            EmptySpace newEmptySpace = TilingAlgebra.split(algebraData, neighbour, minDistanceTab, tabMap,
                    direction);
            if (newEmptySpace == null)
                return false;
        }
        return true;
    }

    private boolean mergeLine(List<EmptySpace> line, IDirection direction) {
        while (line.size() > 1) {
            EmptySpace area1 = line.get(0);
            EmptySpace area2 = line.get(1);
            if (!TilingAlgebra.merge(algebraData, area1, area2, direction))
                return false;
            line.remove(area2);
        }
        return true;
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
        Map<XTab, Edge> xTabEdgeMap = algebraData.getXTabEdges();

        Edge xEdge = xTabEdgeMap.get(xTab);
        if (xEdge == null)
            return null;
        List<IArea> candidates = hDirection.getOppositeAreas(xEdge);
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

