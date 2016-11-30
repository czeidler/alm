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
    public void testFrameLayout() throws IOException, XmlPullParserException {
        File file = new File(ClassLoader.getSystemClassLoader().getResource("framelayout.xml").getFile());

        AndroidXMLParser parser = new AndroidXMLParser();
        parser.parse(file);
    }

    public void testLinearLayout() throws IOException, XmlPullParserException {
        File file = new File(ClassLoader.getSystemClassLoader().getResource("simple.xml").getFile());

        AndroidXMLParser parser = new AndroidXMLParser();
        parser.parse(file);
    }

    public void testTableLayout() throws IOException, XmlPullParserException {
        File file = new File(ClassLoader.getSystemClassLoader().getResource("tablelayout.xml").getFile());

        AndroidXMLParser parser = new AndroidXMLParser();
        parser.parse(file);
    }

    public void testGridLayout() throws IOException, XmlPullParserException {
        File file = new File(ClassLoader.getSystemClassLoader().getResource("gridlayout.xml").getFile());

        AndroidXMLParser parser = new AndroidXMLParser();
        parser.parse(file);
    }

    public void testGridLayout0() throws IOException, XmlPullParserException {
        File file = new File(ClassLoader.getSystemClassLoader().getResource("gridlayout0.xml").getFile());

        AndroidXMLParser parser = new AndroidXMLParser();
        parser.parse(file);
    }

    public void testGridLayout1() throws IOException, XmlPullParserException {
        File file = new File(ClassLoader.getSystemClassLoader().getResource("gridlayout1.xml").getFile());

        AndroidXMLParser parser = new AndroidXMLParser();
        parser.parse(file);
    }

    public void testRelativeLayout() throws IOException, XmlPullParserException {
        File file = new File(ClassLoader.getSystemClassLoader().getResource("relativelayout.xml").getFile());

        AndroidXMLParser parser = new AndroidXMLParser();
        parser.parse(file);
    }
}
