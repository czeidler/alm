/*
 * Copyright 2016.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm.algebra.trafo;

import junit.framework.TestCase;
import nz.ac.auckland.alm.IArea;
import nz.ac.auckland.alm.TabArea;
import nz.ac.auckland.alm.algebra.Fragment;
import nz.ac.auckland.alm.algebra.FragmentUtils;
import nz.ac.auckland.alm.algebra.string.Parser;
import nz.ac.auckland.alm.algebra.string.StringReader;


public class SymmetryAnalyzerTest extends TestCase  {

    private Parser.IAreaFactory areaFactory = new Parser.IAreaFactory() {
        @Override
        public IArea getArea(String areaId) {
            return createArea(areaId);
        }
    };

    private IArea createArea(String id) {
        IArea area = new TabArea();
        area.setId(id);
        return area;
    }

    private Fragment create(String algebraString) {
        return StringReader.readRawFragments(algebraString, areaFactory).get(0);
    }

    public void testTotalSymmetryCountSameChildrenSize() throws Exception {
        Fragment fragment = create("(A|B)/(C|D)");
        int count = SymmetryAnalyzer.totalSymmetryCountSameChildrenSize(fragment);
        assertEquals(2, count);

        fragment = create("(A|B|C)/(D|E|F)");
        count = SymmetryAnalyzer.totalSymmetryCountSameChildrenSize(fragment);
        assertEquals(2, count);

        fragment = create("(A|B)/(C|D)/(E|F)");
        count = SymmetryAnalyzer.totalSymmetryCountSameChildrenSize(fragment);
        assertEquals(3, count);

        fragment = create("(A|B)/(C/D)/(E|F)");
        count = SymmetryAnalyzer.totalSymmetryCountSameChildrenSize(fragment);
        assertEquals(0, count);

        fragment = create("(A|B|C)/(D|((A|B)/(C|D))|F)");
        count = SymmetryAnalyzer.totalSymmetryCountSameChildrenSize(fragment);
        assertEquals(4, count);
    }

    public void testLevelSymmetry() throws Exception {
        Fragment fragment = create("(A|B)/(C|D)");
        int count = SymmetryAnalyzer.levelSymmetry(fragment);
        assertEquals(4, count);

        fragment = create("((A/B/C)/(A/B/C))|G|(A/B/C)");
        count = SymmetryAnalyzer.levelSymmetry(fragment);
        assertEquals(14, SymmetryAnalyzer.numberOfElementsInLevels(fragment));
        assertEquals(11, count);

        fragment = create("(((A|B|C)/(A|B|C))|G)/(A|B|C)");
        count = SymmetryAnalyzer.levelSymmetry(fragment);
        assertEquals(15, SymmetryAnalyzer.numberOfElementsInLevels(fragment));
        assertEquals(11, count);

        fragment = create("(A|B)/(C|D)/(E|F)");
        count = SymmetryAnalyzer.levelSymmetry(fragment);
        assertEquals(6, count);

        fragment = create("((A/B)|(A/B))/((A/B)|(A/B))");
        count = SymmetryAnalyzer.levelSymmetry(fragment);
        assertEquals(12, count);

        fragment = create("((A/B)|(A/B))/((A/B)/(A/B))");
        count = SymmetryAnalyzer.levelSymmetry(fragment);
        assertEquals(8, count);

        fragment = create("((A/B)|(A/B))/((A/B)|(A/B))/((A/B)/(A/B))");
        count = SymmetryAnalyzer.levelSymmetry(fragment);
        assertEquals(14, count);
    }

    public void testSummedFragmentWeightSameOrientation() throws Exception {
        Fragment fragment = create("(A|B)/(C/D)");
        int count = SymmetryAnalyzer.summedFragmentWeights(fragment);
        int summedFragmentWeightSameOrientation = SymmetryAnalyzer.summedFragmentWeightSameOrientation(fragment);
        assertEquals(4, count);
        assertEquals(4, summedFragmentWeightSameOrientation);
    }

    private int symmetryMaxCount(Fragment fragment) {
        return (int)Math.pow(FragmentUtils.countAreas(fragment), 2) * FragmentUtils.countLevels(fragment);
    }

    public void testSymmetryClassifier() throws Exception {
        Fragment fragment = create("(A|B)/(C/D)");
        int count = SymmetryAnalyzer.symmetryValueRecursive(fragment);
        int maxCount = symmetryMaxCount(fragment);
        assertEquals(0, count);
        //assertEquals(16, maxCount);

        fragment = create("(A|B)/(C|D)");
        count = SymmetryAnalyzer.symmetryValueRecursive(fragment);
        maxCount = symmetryMaxCount(fragment);
        assertEquals(16, count);
        assertEquals(16, maxCount);

        fragment = create("((A/B)|(A/B))/((A/B)|(A/B))");
        count = SymmetryAnalyzer.symmetryValueRecursive(fragment);
        maxCount = symmetryMaxCount(fragment);
        assertEquals(128, count);
        assertEquals(128, maxCount);

        fragment = create("((A/B)|(A/B))/((A/B)|(A|B))");
        count = SymmetryAnalyzer.symmetryValueRecursive(fragment);
        maxCount = symmetryMaxCount(fragment);
        assertEquals(16, count);
        assertEquals(128, maxCount);

        fragment = create("(T/E)/(T/E)/(T/E)/(T/E)/(T/E)/(T/E)");
        count = SymmetryAnalyzer.symmetryValueRecursive(fragment);
        maxCount = symmetryMaxCount(fragment);
        assertEquals(144, count);
        assertEquals(144, maxCount);

        fragment = create("(T/E)/(T/E)");
        count = SymmetryAnalyzer.symmetryValueRecursive(fragment);
        maxCount = symmetryMaxCount(fragment);
        assertEquals(16, count);
        assertEquals(16, maxCount);

        fragment = create("((T/T)/E)/((T/T)/E)");
        count = SymmetryAnalyzer.symmetryValueRecursive(fragment);
        maxCount = symmetryMaxCount(fragment);
        assertEquals(72, count);
        assertEquals(72, maxCount);

        fragment = create("S/T/S/((T/E)/(T/E))/(((T/T)/E)/((T/T)/E))/((T/E)/(T/E)/(T/E)/(T/E)/(T/E)/(T/E))");
        count = SymmetryAnalyzer.symmetryValueRecursive(fragment);
        maxCount = symmetryMaxCount(fragment);
        assertEquals(232, count);
        assertEquals(1875, maxCount);

        fragment = create("(A/(B|(C/D/E)))/F");
        count = SymmetryAnalyzer.symmetryValueRecursive(fragment);
        maxCount = symmetryMaxCount(fragment);
        assertEquals(0, count);
        assertEquals(108, maxCount);

        fragment = create("(((D|E)/F)|((G|H)/I))/(((J|K)/L)|((M|N)/O))/(((P|Q)/R)|((S|T)/U))");
        count = SymmetryAnalyzer.symmetryValueRecursive(fragment);
        maxCount = symmetryMaxCount(fragment);
        assertEquals(972, count);
        assertEquals(972, maxCount);

        fragment = create("(((D|E)/F)/((G|H)/I)/((J|K)/L)/((M|N)/O))|(((P|Q)/R)/((S|T)/U))");
        count = SymmetryAnalyzer.symmetryValueRecursive(fragment);
        maxCount = symmetryMaxCount(fragment);
        assertEquals(360, count);
        assertEquals(972, maxCount);

        fragment = create("((A|B|C|D)/(E|F|G|H))|((I|J|K|L)/(M|N|O|P)/(Q|R|S|T)/(U|V|W|X)/(Y|Z|A1|B1))");
        count = SymmetryAnalyzer.symmetryValueRecursive(fragment);
        maxCount = symmetryMaxCount(fragment);

        fragment = create("(A|B|C|D)|((E|F|G|H)/(I|J|K|L)/(M|N|O|P)/(Q|R|S|T)/(U|V|W|X)/(Y|Z|A1|B1))");
        count = SymmetryAnalyzer.symmetryValueRecursive(fragment);
        maxCount = symmetryMaxCount(fragment);

        fragment = create("((A|B|C|D)|(E|F|G|H))/((I|J|K|L)|(M|N|O|P))/((Q|R|S|T)|(U|V|W|X))/(Y|Z|A1|B1)");
        count = SymmetryAnalyzer.symmetryValueRecursive(fragment);
        maxCount = symmetryMaxCount(fragment);

        fragment = create("((A|B|C|D)|(E|F|G|H))/((I|J|K|L)|(M|N|O|P))/((Q|R|S|T)|(U|V|W|X))/(Y|Z|A1|B1)");
        count = SymmetryAnalyzer.symmetryValueRecursive(fragment);
        maxCount = symmetryMaxCount(fragment);

    }
}
