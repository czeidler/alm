/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm.algebra.string;

import nz.ac.auckland.alm.*;
import nz.ac.auckland.alm.algebra.AlgebraData;
import nz.ac.auckland.alm.algebra.BaseAlgebraTestCase;
import nz.ac.auckland.linsolve.Variable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AlgebraStringTest extends BaseAlgebraTestCase {
    private void assertStringNotParsed(String string) {
        System.out.println("Parse bad string: " + string);
        List<IArea> items = StringReader.readRawFragments(string, null);
        assertTrue(items == null);
    }

    private void assertStringParsed(String string) {
        System.out.println("Parse: " + string);
        List<IArea> items = StringReader.readRawFragments(string, null);
        assertTrue(items != null);
        System.out.println("Result: " + StringWriter.write(items));
    }

    public void testParsing() {
        assertStringParsed("AD2 |{2} B * (A | {  2 } B) ");
        assertStringParsed("(A|B|C)/ (D|E|((r/b)) ) / A  *m");
        assertStringParsed("((m))");
        assertStringParsed("A");

        assertStringNotParsed("(");
        assertStringNotParsed("}");
        assertStringNotParsed("(A");
        assertStringNotParsed("(A |");
        assertStringNotParsed("(A | }");
        assertStringNotParsed("(A: | }");

        List<IArea> items = StringReader.readRawFragments("", null);
        assertTrue(items != null);
        assertEquals(0, items.size());
    }

    private void assignAreaIds(List<IArea> areas) {
        int areaCount = 0;
        for (IArea area : areas) {
            final String areaNames = "ABCDEFGHIJKMNOPQRSTUVWXYZ";
            int letter = areaCount % areaNames.length();
            int index = areaCount / areaNames.length();
            String name = "" + areaNames.charAt(letter);
            if (index > 0)
                name += index;
            area.setId(name);
            areaCount++;
        }
    }

    /**
     * Checks if a AlgebraData describe the same layout.
     *
     * The areas are matched by their ids. The AlgebraData's tabstops can be different though. If no matching area is
     * found false is returned.
     *
     * @param algebraData
     * @param sameBorders check if the border tabstops are the same
     * @return true if the AlgebraData is equivalent
     */
    public boolean isEquivalent(AlgebraData algebraData1,  AlgebraData algebraData, boolean sameBorders) {
        if (sameBorders) {
            if (algebraData1.getLeft() != algebraData.getLeft() || algebraData1.getTop() != algebraData.getTop()
                    || algebraData1.getRight() != algebraData.getRight()
                    || algebraData1.getBottom() != algebraData.getBottom())
                return false;
        }
        Map<Variable, Variable> oursToTheirsMap = new HashMap<Variable, Variable>();
        List<IArea> ourAreas = algebraData1.getAllAreasList();
        List<IArea> theirAreas = algebraData.getAllAreasList();
        if (ourAreas.size() != theirAreas.size())
            return false;
        for (IArea ourArea : ourAreas) {
            if (ourArea.getId() == null)
                return false;
            IArea theirArea = null;
            for (IArea area : theirAreas) {
                if (area.getId() != null && area.getId().equals(ourArea.getId())) {
                    theirArea = area;
                    break;
                }
            }
            if (theirArea == null)
                return false;
            if (!isValidTabMatch(oursToTheirsMap, ourArea.getLeft(), theirArea.getLeft()))
                return false;
            if (!isValidTabMatch(oursToTheirsMap, ourArea.getTop(), theirArea.getTop()))
                return false;
            if (!isValidTabMatch(oursToTheirsMap, ourArea.getRight(), theirArea.getRight()))
                return false;
            if (!isValidTabMatch(oursToTheirsMap, ourArea.getBottom(), theirArea.getBottom()))
                return false;
        }
        return true;
    }

    static private boolean isValidTabMatch(Map<Variable, Variable> oursToTheirsMap, Variable our, Variable theirs) {
        Variable existingTheirs = oursToTheirsMap.get(our);
        if (existingTheirs == null) {
            oursToTheirsMap.put(our, theirs);
            return true;
        }
        return existingTheirs == theirs;
    }

    public void testThreeButtons() {
        LayoutSpec layoutSpec = getLayoutSpec(500, 500);

        XTab left = layoutSpec.getLeft();
        YTab top = layoutSpec.getTop();
        XTab right = layoutSpec.getRight();
        YTab bottom = layoutSpec.getBottom();

        XTab x1 = new XTab();
        XTab x2 = new XTab();

        layoutSpec.addArea(new Area(left, top, x1, bottom));
        layoutSpec.addArea(new Area(x1, top, x2, bottom));
        layoutSpec.addArea(new Area(x2, top, right, bottom));

        assignAreaIds(layoutSpec.getAreas());

        AlgebraData data = new AlgebraData(layoutSpec, null);
        AlgebraSpec algebraSpec = new AlgebraSpec(data);
        algebraSpec.compress();

        System.out.println("Three Buttons:");
        String algebraString = StringWriter.write(algebraSpec);
        System.out.println(algebraString);

        assertEquals(1, algebraSpec.getFragments().size());

        AlgebraData readAlgebraData = StringReader.read(algebraString, left, top, right, bottom,
                Parser.getDefaultAreaFactory());
        assertTrue(readAlgebraData != null);
        assertEquals(true, isEquivalent(data, readAlgebraData, true));
    }

    public void testPinWheel() {
        LayoutSpec layoutSpec = getLayoutSpec(500, 500);

        XTab left = layoutSpec.getLeft();
        YTab top = layoutSpec.getTop();
        XTab right = layoutSpec.getRight();
        YTab bottom = layoutSpec.getBottom();

        XTab x1 = makeXTabAt(100);
        XTab x2 = makeXTabAt(200);

        YTab y1 = makeYTabAt(100);
        YTab y2 = makeYTabAt(200);

        // outer areas
        layoutSpec.addArea(new Area(left, top, x2, y1));
        layoutSpec.addArea(new Area(x2, top, right, y2));
        layoutSpec.addArea(new Area(x1, y2, right, bottom));
        layoutSpec.addArea(new Area(left, y1, x1, bottom));
        // middle
        layoutSpec.addArea(new EmptySpace(x1, y1, x2, y2));
        assignAreaIds(layoutSpec.getAreas());

        AlgebraData data = new AlgebraData(layoutSpec, null);
        AlgebraSpec algebraSpec = new AlgebraSpec(data);
        algebraSpec.compress();

        System.out.println("Pin Wheel:");
        String algebraString = StringWriter.write(algebraSpec);
        System.out.println(algebraString);

        assertEquals(2, algebraSpec.getFragments().size());

        AlgebraData readAlgebraData = StringReader.read(algebraString, left, top, right, bottom,
                Parser.getDefaultAreaFactory());
        assertTrue(readAlgebraData != null);
        assertEquals(true, isEquivalent(data, readAlgebraData, true));
    }

    public void testPinWheelExtension() {
        LayoutSpec layoutSpec = getLayoutSpec(500, 500);

        XTab left = layoutSpec.getLeft();
        YTab top = layoutSpec.getTop();
        XTab right = layoutSpec.getRight();
        YTab bottom = layoutSpec.getBottom();

        XTab x1 = makeXTabAt(100);
        XTab x2 = makeXTabAt(200);
        XTab x3 = makeXTabAt(300);
        XTab x4 = makeXTabAt(400);

        YTab y1 = makeYTabAt(100);
        YTab y2 = makeYTabAt(200);

        // outer areas
        layoutSpec.addArea(new Area(left, top, x2, y1));
        layoutSpec.addArea(new Area(x2, top, x3, y2));
        layoutSpec.addArea(new Area(x1, y2, x3, bottom));
        layoutSpec.addArea(new Area(left, y1, x1, bottom));
        // middle
        layoutSpec.addArea(new EmptySpace(x1, y1, x2, y2));

        layoutSpec.addArea(new Area(x3, top, x4, bottom));
        layoutSpec.addArea(new Area(x4, top, right, bottom));

        AlgebraData data = new AlgebraData(layoutSpec, null);

        AlgebraSpec algebraSpec = new AlgebraSpec(data);
        algebraSpec.compress();

        System.out.println("Pin Wheel extension:");
        System.out.println(StringWriter.write(algebraSpec));
    }

    public void testGrid() {
        LayoutSpec layoutSpec = getLayoutSpec(500, 500);

        XTab left = layoutSpec.getLeft();
        YTab top = layoutSpec.getTop();
        XTab right = layoutSpec.getRight();
        YTab bottom = layoutSpec.getBottom();

        XTab x0 = makeXTabAt(100);
        XTab x1 = makeXTabAt(200);

        YTab y0 = makeYTabAt(100);
        YTab y1 = makeYTabAt(200);

        // row1
        layoutSpec.addArea(new Area(left, top, x0, y0));
        layoutSpec.addArea(new Area(x0, top, x1, y0));
        layoutSpec.addArea(new Area(x1, top, right, y0));
        // row2
        layoutSpec.addArea(new Area(left, y0, x0, y1));
        layoutSpec.addArea(new Area(x0, y0, x1, y1));
        layoutSpec.addArea(new Area(x1, y0, right, y1));
        // row3
        layoutSpec.addArea(new Area(left, y1, x0, bottom));
        layoutSpec.addArea(new Area(x0, y1, x1, bottom));
        layoutSpec.addArea(new Area(x1, y1, right, bottom));


        AlgebraData data = new AlgebraData(layoutSpec, null);

        AlgebraSpec algebraSpec = new AlgebraSpec(data);
        algebraSpec.compress();

        System.out.println("Grid:");
        System.out.println(StringWriter.write(algebraSpec));

        assertEquals(1, algebraSpec.getFragments().size());
    }

    public void testGroupLayout() {
        LayoutSpec layoutSpec = getLayoutSpec(500, 500);

        XTab left = layoutSpec.getLeft();
        YTab top = layoutSpec.getTop();
        XTab right = layoutSpec.getRight();
        YTab bottom = layoutSpec.getBottom();

        XTab x0 = makeXTabAt(100);
        XTab x1 = makeXTabAt(200);
        XTab x2 = makeXTabAt(300);

        YTab y0 = makeYTabAt(100);
        YTab y1 = makeYTabAt(200);

        // row1
        layoutSpec.addArea(new Area(left, top, x0, y0));
        layoutSpec.addArea(new Area(x0, top, x1, y0));
        // row2
        layoutSpec.addArea(new Area(left, y0, x1, y1));

        // right
        layoutSpec.addArea(new Area(x1, top, x2, y1));
        layoutSpec.addArea(new Area(x2, top, right, y1));

        // bottom
        layoutSpec.addArea(new Area(left, y1, right, bottom));

        AlgebraData data = new AlgebraData(layoutSpec, null);

        AlgebraSpec algebraSpec = new AlgebraSpec(data);
        algebraSpec.compress();

        System.out.println("Group Layout:");
        System.out.println(StringWriter.write(algebraSpec));

        assertEquals(1, algebraSpec.getFragments().size());
    }
}
