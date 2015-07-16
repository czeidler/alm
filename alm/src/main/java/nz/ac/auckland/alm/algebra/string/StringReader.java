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
import nz.ac.auckland.alm.algebra.AlgebraData;

import java.util.ArrayList;
import java.util.List;


public class StringReader {
    static public List<IArea> readRawFragments(final String input, Parser.IAreaFactory areaFactory) {
        Parser parser = new Parser(areaFactory, new Parser.IListener() {
            @Override
            public void onError(String error, Lexer.Token token) {
                System.out.println(input);
                String errorIndicator = "";
                for (int i = 0; i < token.position; i++)
                    errorIndicator += " ";
                errorIndicator += "^";
                System.out.println(errorIndicator);
                System.out.println("Parser error at " + token.position + ": " + error);
            }
        });
        Lexer lexer = new Lexer(input, parser);
        lexer.run();

        if (parser.hasError())
            return null;
        return parser.getTerms();
    }

    static public AlgebraData read(final String input, XTab left, YTab top, XTab right, YTab bottom,
                                   Parser.IAreaFactory areaFactory) {
        List<IArea> fragments = readRawFragments(input, areaFactory);
        if (fragments == null)
            return null;
        List<IArea> atoms = toAtoms(fragments);
        // set border tabs
        for (IArea atom : atoms) {
            if (atom.getLeft() == null)
                atom.setLeft(left);
            if (atom.getRight() == null)
                atom.setRight(right);
            if (atom.getTop() == null)
                atom.setTop(top);
            if (atom.getBottom() == null)
                atom.setBottom(bottom);
        }
        // create AlgebraData
        AlgebraData algebraData = new AlgebraData(left, top, right, bottom);
        for (IArea atom : atoms)
            algebraData.addArea(atom);

        return algebraData;
    }

    static private List<IArea> toAtoms(List<IArea> fragments) {
        List<IArea> atoms = new ArrayList<IArea>();
        for (IArea fragment : fragments)
            toAtoms(fragment, atoms);
        return atoms;
    }

    static private void toAtoms(IArea fragment, List<IArea> atoms) {
        if (!(fragment instanceof Fragment)) {
            if (!atoms.contains(fragment))
                atoms.add(fragment);
            return;
        }
        for (IArea child : (List<IArea>)((Fragment) fragment).getItems())
            toAtoms(child, atoms);
    }
}
