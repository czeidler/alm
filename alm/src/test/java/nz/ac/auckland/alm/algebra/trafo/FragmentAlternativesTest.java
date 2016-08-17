/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm.algebra.trafo;

import junit.framework.TestCase;
import nz.ac.auckland.alm.Area;
import nz.ac.auckland.alm.IArea;
import nz.ac.auckland.alm.LayoutSpec;
import nz.ac.auckland.alm.algebra.Fragment;
import nz.ac.auckland.alm.algebra.FragmentUtils;
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

    final double SCREEN_WIDTH_LAND = 1800;
    final double SCREEN_HEIGHT_LAND = 1000;
    private IArea createArea(String id) {
        Area area = new Area();
        area.setId(id);
        if (id.equals("L"))
            area.setPreferredSize(SCREEN_WIDTH_LAND, SCREEN_HEIGHT_LAND);
        else
            area.setPreferredSize(200, 70);
        area.setMinSize(200, 70);
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

    /*
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

        // add simple column flow trafo
        fragmentAlternatives.addTransformation(new ColumnFlowTrafo());

        fragment = create("A/B/C");
        results = fragmentAlternatives.calculateAlternatives(fragment, comparator);
        assertEquals(2, results.size());

        fragment = create("A|B|C");
        results = fragmentAlternatives.calculateAlternatives(fragment, comparator);
        assertEquals(1, results.size());

        // test error case
        fragment = create("A/B/C/D/(E|F)");
        results = fragmentAlternatives.calculateAlternatives(fragment, comparator);
        assertEquals(17, results.size());

        // add simple row flow trafo
        fragmentAlternatives.addTransformation(new RowFlowTrafo());

        fragment = create("A|B|C");
        results = fragmentAlternatives.calculateAlternatives(fragment, comparator);
        assertEquals(2, results.size());

        fragment = create("A|B|C|D|E|F|G");
        results = fragmentAlternatives.calculateAlternatives(fragment, comparator);
        assertEquals(22, results.size());
    }*/

    IAlternativeClassifier<Object> classifierSimple = new IAlternativeClassifier<Object>() {
        @Override
        public Object coarseClassify(Fragment fragment, TrafoHistory trafoHistory) {
            return null;
        }

        @Override
        public void fineClassify(Fragment fragment, Object classification) {

        }

        @Override
        public double objectiveValue(Object classification) {
            return 0;
        }
    };

    class Classification {
        public TrafoHistory trafoHistory;
        public double prefWidth;
        public double prefHeight;
        public double diffWidth;
        public double diffHeight;

        public double getDiffSize() {
            return Math.pow(diffWidth, 2) + Math.pow(diffHeight, 2);
        }
    }

    IAlternativeClassifier<Classification> classifier = new IAlternativeClassifier<Classification>() {
        @Override
        public Classification coarseClassify(Fragment fragment, TrafoHistory history) {
            Classification classification = new Classification();
            classification.trafoHistory = history;

            LayoutSpec layoutSpec = FragmentUtils.toLayoutSpec(fragment);
            layoutSpec.setRight(SCREEN_WIDTH_LAND);
            layoutSpec.setBottom(SCREEN_HEIGHT_LAND);
            layoutSpec.solve();
            layoutSpec.release();

            List<Area> areas = FragmentUtils.getAreas(fragment);
            for (Area area : areas) {
                double width = area.getRight().getValue() - area.getLeft().getValue();
                double height = area.getBottom().getValue() - area.getTop().getValue();
                Area.Size areaPrefSize = area.getPreferredSize();
                classification.prefWidth = width;
                classification.prefHeight = height;
                classification.diffWidth += Math.pow(width - areaPrefSize.getWidth(), 2);
                classification.diffHeight += Math.pow(height - areaPrefSize.getHeight(), 2);
            }

            return classification;
        }

        @Override
        public void fineClassify(Fragment fragment, Classification classification) {

        }

        @Override
        public double objectiveValue(Classification classification) {
            double prefSizeDiffTerm = classification.getDiffSize() / (SCREEN_HEIGHT_LAND * SCREEN_WIDTH_LAND);
            double ratioTerm = (classification.prefWidth / classification.prefHeight)
                    / (SCREEN_WIDTH_LAND / SCREEN_HEIGHT_LAND);
            return (float)(prefSizeDiffTerm + ratioTerm);
        }
    };

    public void testFragmentAlternatives3() {
        FragmentAlternatives fragmentAlternatives = new FragmentAlternatives(new Classifier(5000, 5000),
                new GroupDetector(comparator));
        SwapTrafo swapTrafo = new SwapTrafo();
        ColumnFlowTrafo columnFlowTrafo = new ColumnFlowTrafo();
        InverseRowFlowTrafo inverseRowFlowTrafo = new InverseRowFlowTrafo();
        fragmentAlternatives.addTrafo(swapTrafo);
        fragmentAlternatives.addTrafo(columnFlowTrafo);
        fragmentAlternatives.addTrafo(inverseRowFlowTrafo);
        List<ITransformation> trafos = fragmentAlternatives.getTrafos();

        //Fragment fragment = create("(B|C)/A/A");
        //Fragment fragment = create("A|B");
        //Fragment fragment = create("(A|B)/(C|D)");
        //Fragment fragment = create("(A|B)/C");
        //Fragment fragment = create("(A/B/C)|D");
        //Fragment fragment = create("((A|B)/(C|D))/((E|F)/(G|H))");
        //Fragment fragment = create("A/B/A/B/A");
        //Fragment fragment = create("(A|A|A)/(A|A|A)/G/(A|A|A)");
        //Fragment fragment = create("S/T/S/T/E/T/E/T/T/E/T/T/E/T/E/T/E/T/E/T/E/T/E/T/E");
        Fragment fragment = create("(T|T|T)/((T|E)/(T|E)/(T|E)/(T|E)/(T|E)/(T|E))/S/(T|T|T|T)/S/E");
        //Fragment fragment = create("T/T/E/T/T/E/T/E/T/E");
        //Fragment fragment = create("(((T/T)/E)/((T/T)/E))/((T/E)/(T/E))");

        IPermutationSelector<Classification> selector
                = new ChainPermutationSelector<Classification>(
                new ApplyToAllPermutationSelector<Classification>(trafos, swapTrafo),
                new ApplyToAllPermutationSelector<Classification>(trafos, columnFlowTrafo),
                new ApplyToAllPermutationSelector<Classification>(trafos, inverseRowFlowTrafo)
                //new RandomPermutationSelector<Classification>(trafos)
        );

        List<FragmentAlternatives.Result> results = fragmentAlternatives.calculateAlternatives(fragment, selector, 60,
                1000 * 5, 20);
        for (FragmentAlternatives.Result result : results)
            System.out.println(result.fragment);
        System.out.println(results.size());
    }

    public void testFragmentAlternativesSimpleClassification() {
        FragmentAlternatives fragmentAlternatives = new FragmentAlternatives(classifierSimple,
                new GroupDetector(comparator));
        SwapTrafo swapTrafo = new SwapTrafo();
        ColumnFlowTrafo columnFlowTrafo = new ColumnFlowTrafo();
        InverseRowFlowTrafo inverseRowFlowTrafo = new InverseRowFlowTrafo();
        fragmentAlternatives.addTrafo(swapTrafo);
        fragmentAlternatives.addTrafo(columnFlowTrafo);
        fragmentAlternatives.addTrafo(inverseRowFlowTrafo);
        List<ITransformation> trafos = fragmentAlternatives.getTrafos();

        //Fragment fragment = create("(B|C)/A/A");
        //Fragment fragment = create("A|B");
        //Fragment fragment = create("(A|B)/(C|D)");
        //Fragment fragment = create("(A|B)/C");
        //Fragment fragment = create("(A/B/C)|D");
        //Fragment fragment = create("((A|B)/(C|D))/((E|F)/(G|H))");
        //Fragment fragment = create("A/B/A/B/A");
        //Fragment fragment = create("(A|A|A)/(A|A|A)/G/(A|A|A)");
        //Fragment fragment = create("S/T/S/T/E/T/E/T/T/E/T/T/E/T/E/T/E/T/E/T/E/T/E/T/E");
        Fragment fragment = create("(T|T|T)/((T|E)/(T|E)/(T|E)/(T|E)/(T|E)/(T|E))/S/(T|T|T|T)/S/E");
        //Fragment fragment = create("T/T/E/T/T/E/T/E/T/E");
        //Fragment fragment = create("(((T/T)/E)/((T/T)/E))/((T/E)/(T/E))");

        IPermutationSelector<Object> selector
                = new ChainPermutationSelector<Object>(
                new ApplyToAllPermutationSelector<Object>(trafos, swapTrafo),
                new ApplyToAllPermutationSelector<Object>(trafos, columnFlowTrafo),
                //new ApplyToAllPermutationSelector<Object>(trafos, inverseRowFlowTrafo));
                new ApplyToAllPermutationSelector<Object>(trafos, inverseRowFlowTrafo),
                new RandomPermutationSelector<Object>(trafos));

        List<FragmentAlternatives.Result> results = fragmentAlternatives.calculateAlternatives(fragment, selector, 100,
                1000 * 6);
        for (FragmentAlternatives.Result result : results)
            System.out.println(result.fragment);
        System.out.println(results.size());
    }

    public void testFragmentAlternativesPermutations() {
        FragmentAlternatives fragmentAlternatives = new FragmentAlternatives(classifierSimple,
                new GroupDetector(comparator));
        SwapTrafo swapTrafo = new SwapTrafo();
        ColumnFlowTrafo columnFlowTrafo = new ColumnFlowTrafo();
        InverseRowFlowTrafo inverseRowFlowTrafo = new InverseRowFlowTrafo();
        fragmentAlternatives.addTrafo(swapTrafo);
        fragmentAlternatives.addTrafo(columnFlowTrafo);
        fragmentAlternatives.addTrafo(inverseRowFlowTrafo);
        List<ITransformation> trafos = fragmentAlternatives.getTrafos();

        IPermutationSelector<Object> selector
                = new ChainPermutationSelector<Object>(
                new ApplyToAllPermutationSelector<Object>(trafos, swapTrafo),
                new ApplyToAllPermutationSelector<Object>(trafos, columnFlowTrafo),
                new ApplyToAllPermutationSelector<Object>(trafos, inverseRowFlowTrafo),
                new RandomPermutationSelector<Object>(trafos));

        Fragment fragment = create("A|B");
        List<FragmentAlternatives.Result> results = fragmentAlternatives.calculateAlternatives(fragment, selector,
                30000, 1000 * 60);
        assertEquals(1, results.size());

        fragment = create("A/B/C/D");
        results = fragmentAlternatives.calculateAlternatives(fragment, selector, 30000, 1000 * 60);
        assertEquals(8, results.size());
    }


    /*
    public void testError2() {
        FragmentAlternatives2 fragmentAlternatives = new FragmentAlternatives2();
        //fragmentAlternatives.addTransformation(new SwapTrafo());
        //fragmentAlternatives.addTransformation(new ColumnFlowTrafo());

        Fragment fragment = create("S/T/S/T/E/T/E/(T/T)/E/(T/T)/E/T/E/T/E/T/E/T/E/T/E/T/E");
        List<FragmentAlternatives2.Result> results = fragmentAlternatives.calculateAlternatives(fragment, comparator);
        for (FragmentAlternatives2.Result result : results)
            System.out.println(result.fragment);
        System.out.println(results.size());
    }

    private Area.Size getPrefDiff(Fragment fragment) {
        double diffWidth = 0;
        double diffHeight = 0;
        List<Area> areas = FragmentUtils.getAreas(fragment);
        for (Area area : areas) {
            double width = area.getRight().getValue() - area.getLeft().getValue();
            double height = area.getBottom().getValue() - area.getTop().getValue();
            Area.Size areaPrefSize = area.getPreferredSize();
            diffWidth += Math.pow(width - areaPrefSize.getWidth(), 2);
            diffHeight += Math.pow(height - areaPrefSize.getHeight(), 2);
        }
        diffWidth = Math.sqrt(diffWidth);
        diffHeight = Math.sqrt(diffHeight);
        return new Area.Size(diffWidth, diffHeight);
    }

    public void testError3() {
        FragmentAlternatives2 fragmentAlternatives = new FragmentAlternatives2();

        Fragment fragment = create("(A|A|A)/(A|A|A)/L/(A|A|A)");

        LayoutSpec layoutSpec = FragmentUtils.toLayoutSpec(fragment);
        layoutSpec.setRight(SCREEN_WIDTH_LAND);
        layoutSpec.setBottom(SCREEN_HEIGHT_LAND);
        layoutSpec.solve();
        Area.Size diff = getPrefDiff(fragment);

        for (Fragment group : GroupDetector.detect(fragment, comparator)) {
            layoutSpec = FragmentUtils.toLayoutSpec(group);
            layoutSpec.setRight(SCREEN_WIDTH_LAND);
            layoutSpec.setBottom(SCREEN_HEIGHT_LAND);
            layoutSpec.solve();
            //Area.Size size =
        }
        List<FragmentAlternatives2.Result> results = fragmentAlternatives.calculateAlternatives(fragment, comparator);
        for (FragmentAlternatives2.Result result : results)
            System.out.println(result.fragment);
        System.out.println(results.size());
    }
*/
}
