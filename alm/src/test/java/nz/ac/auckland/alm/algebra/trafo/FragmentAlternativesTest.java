/*
 * Copyright 2015.
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
import nz.ac.auckland.alm.algebra.string.Parser;
import nz.ac.auckland.alm.algebra.string.StringReader;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class FragmentAlternativesTest extends TestCase {
    class EmptyTrafo implements ITransformation {
        @Override
        public List<Result> transform(Fragment fragment) {
            List<Result> results = new ArrayList<Result>();
            return results;
        }
    }

    private IArea createArea(String id) {
        IArea area = new TabArea();
        area.setId(id);
        return area;
    }

    private Parser.IAreaFactory areaFactory = new Parser.IAreaFactory() {
        @Override
        public IArea getArea(String areaId) {
            return createArea(areaId);
        }
    };
    Comparator<IArea> comparator = new AreaComparator();

    private Fragment create(String algebraString) {
        return StringReader.readRawFragments(algebraString, areaFactory).get(0);
    }

    public void testSimple() {
        FragmentAlternatives fragmentAlternatives = new FragmentAlternatives();
        // no trafo
        fragmentAlternatives.addTransformation(new EmptyTrafo());

        Fragment fragment = create("A|B");

        List<FragmentAlternatives.Result> results = fragmentAlternatives.calculateAlternatives(fragment, comparator);
        assertEquals(0, results.size());

        // add swap trafo
        fragmentAlternatives.addTransformation(new SwapTrafo());
        results = fragmentAlternatives.calculateAlternatives(fragment, comparator);
        assertEquals(1, results.size());

        fragment = create("A|B|(C/D)");
        results = fragmentAlternatives.calculateAlternatives(fragment, comparator);
        assertEquals(3, results.size());

        // add simple 1 column -> 2 column Trafo
        fragmentAlternatives.addTransformation(new ColumnOneToTwoTrafo());
        fragment = create("A/B/C");
        results = fragmentAlternatives.calculateAlternatives(fragment, comparator);
        assertEquals(2, results.size());

        fragment = create("A|B|C");
        results = fragmentAlternatives.calculateAlternatives(fragment, comparator);
        assertEquals(1, results.size());

        // test error case
        fragment = create("A/B/C/D/(E|F)");
        results = fragmentAlternatives.calculateAlternatives(fragment, comparator);
        assertEquals(13, results.size());
    }

    public void testError0() {
        FragmentAlternatives fragmentAlternatives = new FragmentAlternatives();
        fragmentAlternatives.addTransformation(new SwapTrafo());
        fragmentAlternatives.addTransformation(new ColumnOneToTwoTrafo());

        Fragment fragment = create("(A|A|A)/(A|A|A)/G/(A|A|A)");
        fragment = fragment.cloneResolve(true);
        List<FragmentAlternatives.Result> results = fragmentAlternatives.calculateAlternatives(fragment, comparator);
        for (FragmentAlternatives.Result result : results)
            System.out.println(result.fragment);
        System.out.println(results.size());
    }
}
