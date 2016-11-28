/*
 * Copyright 2016.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.androidxmlparser;

import junit.framework.TestCase;
import nz.ac.nz.auckland.androidxmlparser.AndroidXMLParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;


public class ParserTest extends TestCase {
    public void testSimple() throws IOException, XmlPullParserException {
        File file = new File(ClassLoader.getSystemClassLoader().getResource("simple.xml").getFile());

        AndroidXMLParser parser = new AndroidXMLParser();
        parser.parse(file);
    }
}
