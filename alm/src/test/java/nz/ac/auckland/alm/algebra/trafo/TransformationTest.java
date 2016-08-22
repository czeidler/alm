package nz.ac.auckland.alm.algebra.trafo;


import junit.framework.TestCase;
import nz.ac.auckland.alm.IArea;
import nz.ac.auckland.alm.TabArea;
import nz.ac.auckland.alm.algebra.Fragment;
import nz.ac.auckland.alm.algebra.string.Parser;
import nz.ac.auckland.alm.algebra.string.StringReader;

import java.util.List;

public class TransformationTest extends TestCase {

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

    public void testFlowToColumnTrafos() {
        Fragment fragment = create("(A|B|C)/(D|E|F)");
        RowFlowTrafo rowFlowTrafo = new RowFlowTrafo();
        List<ITransformation.Result> results = rowFlowTrafo.transform(fragment);
        assertEquals(1, results.size());
        assertEquals("A/B/C/D/E/F", results.get(0).fragment.toString());

        fragment = create("(A/B/C)|(D/E/F)");
        InverseColumnTrafo inverseColumnTrafo = new InverseColumnTrafo();
        results = inverseColumnTrafo.transform(fragment);
        assertEquals(1, results.size());
        assertEquals("A/B/C/D/E/F", results.get(0).fragment.toString());

        results = rowFlowTrafo.transform(create("(A|B)/(C|D)/(E|F)"));
        assertEquals(1, results.size());
        assertEquals("A/B/C/D/E/F", results.get(0).fragment.toString());

        results = rowFlowTrafo.transform(create("(A)/(D|E|F)"));
        assertEquals(0, results.size());
        results = rowFlowTrafo.transform(create("A/(D|E|F)"));
        assertEquals(0, results.size());
        results = rowFlowTrafo.transform(create("(A|B)/(D|E|F)/(G)"));
        assertEquals(0, results.size());
        results = rowFlowTrafo.transform(create("(A|B)/(D|E)/(F|G|H)"));
        assertEquals(0, results.size());
    }
}
