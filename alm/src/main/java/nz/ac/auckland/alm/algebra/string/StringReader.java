/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm.algebra.string;

import nz.ac.auckland.alm.IArea;

import java.util.List;


public class StringReader {
    static public List<IArea> read(String input) {
        Parser parser = new Parser();
        Lexer lexer = new Lexer(input, parser);
        lexer.run();

        if (parser.getErrorToken() != null) {
            Lexer.Token token = parser.getErrorToken();
            System.out.println(input);
            String errorIndicator = "";
            for (int i = 0; i < token.position; i++)
                errorIndicator += " ";
            errorIndicator += "^";
            System.out.println(errorIndicator);
            System.out.println("Parser error at " + token.position + ": " + parser.getError());
            return null;
        }
        return parser.getTerms();
    }
}
