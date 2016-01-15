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

import java.util.ArrayList;
import java.util.List;


public class FragmentAlternativesTest extends TestCase {
    class EmptyTrafo implements ITransformation {
        @Override
        public List<Result> transform(Fragment fragment) {
            List<Result> results = new ArrayList<Result>();
            return results;
        }
    }

    class SwapTrafo implements ITransformation {
        @Override
        public List<Result> transform(Fragment fragment) {
            List<Result> results = new ArrayList<Result>();
            Fragment trafo;
            if (fragment.isHorizontalDirection())
                trafo = Fragment.verticalFragment();
            else
                trafo = Fragment.horizontalFragment();

            for (IArea area : (List<IArea>)fragment.getItems())
                trafo.add(area, false);

            results.add(new Result(1.1f, trafo));
            return results;
        }
    }

    class ColumnOneToTwoTrafo implements ITransformation {
        @Override
        public List<Result> transform(Fragment fragment) {
            List<Result> results = new ArrayList<Result>();
            if (fragment.isHorizontalDirection())
                return results;
            // at least 3 items otherwise it's a swap
            if (fragment.getItems().size() < 3)
                return results;
            int splitPoint = fragment.getItems().size() / 2 + 1;
            Fragment column1 = Fragment.verticalFragment();
            Fragment column2 = Fragment.verticalFragment();
            Fragment hFragment = Fragment.horizontalFragment();
            hFragment.add(column1, false);
            hFragment.add(column2, false);
            List<IArea> items = fragment.getItems();
            for (int i = 0; i < splitPoint; i++)
                column1.add(items.get(i), false);
            for (int i = splitPoint; i < items.size(); i++)
                column2.add(items.get(i), false);

            results.add(new Result(1.1f, hFragment));
            return results;
        }
    }

    private IArea createArea(String id) {
        IArea area = new TabArea();
        area.setId(id);
        return area;
    }


    public void testSimple() {
        FragmentAlternatives fragmentAlternatives = new FragmentAlternatives();
        // no trafo
        fragmentAlternatives.addTransformation(new EmptyTrafo());

        Fragment fragment = Fragment.horizontalFragment();
        fragment.add(createArea("A"), false);
        fragment.add(createArea("B"), false);

        List<Result> results = fragmentAlternatives.calculateAlternatives(fragment);
        assertEquals(0, results.size());

        // add swap trafo
        fragmentAlternatives.addTransformation(new SwapTrafo());
        results = fragmentAlternatives.calculateAlternatives(fragment);
        assertEquals(1, results.size());

        Fragment subFragment = Fragment.verticalFragment();
        subFragment.add(createArea("C"), false);
        subFragment.add(createArea("D"), false);
        fragment.add(subFragment, false);

        results = fragmentAlternatives.calculateAlternatives(fragment);
        assertEquals(3, results.size());

        // add simple 1 column -> 2 column Trafo
        fragmentAlternatives.addTransformation(new ColumnOneToTwoTrafo());
        fragment = Fragment.verticalFragment();
        fragment.add(createArea("A"), false);
        fragment.add(createArea("B"), false);
        fragment.add(createArea("C"), false);
        results = fragmentAlternatives.calculateAlternatives(fragment);
        assertEquals(2, results.size());
    }
}
