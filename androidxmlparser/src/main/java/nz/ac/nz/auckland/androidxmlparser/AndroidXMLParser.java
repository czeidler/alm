/*
 * Copyright 2016.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.nz.auckland.androidxmlparser;

import nz.ac.auckland.alm.IArea;
import nz.ac.auckland.alm.TabArea;
import nz.ac.auckland.alm.algebra.Fragment;
import nz.ac.auckland.alm.algebra.string.*;
import nz.ac.auckland.alm.algebra.string.StringWriter;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;


class AreaInfo {
    String viewType;
}

abstract class BaseParser {
    final protected Object elementSpecsParent;
    final protected LayoutElementParser parentParser;

    public BaseParser(LayoutElementParser parentParser, Object elementSpecsParent) {
        this.parentParser = parentParser;
        this.elementSpecsParent = elementSpecsParent;
    }

    abstract IArea getElement();

    public LayoutElementParser finish() {
        parentParser.addChild(elementSpecsParent, getElement());
        return parentParser;
    }
}

class ElementParser extends BaseParser {
    private IArea area;
    private AreaInfo areaInfo = new AreaInfo();

    public ElementParser(LayoutElementParser parentParser, Object elementSpecsParent, XmlPullParser parser,
                         Map<IArea, AreaInfo> areaMap) {
        super(parentParser, elementSpecsParent);

        String viewType = parser.getName();
        if (!isKnownView(viewType))
            viewType = "Custom";
        areaInfo.viewType = viewType;
        area = new TabArea();
        areaMap.put(area, areaInfo);
    }

    final private String[] viewTypes = {
            "AnalogClock",
            "AutoCompleteTextView",
            "Button",
            "CheckBox",
            "CheckedTextView",
            "Chronometer",
            "DatePicker",
            "DigitalClock",
            "EditText",
            "ExpandableListView",
            "fragment",
            "GridView",
            "HorizontalScrollView",
            "IconTextView",
            "ImageView",
            "ImageButton",
            "ListView",
            "NumberPicker",
            "RadioButton",
            "RadioGroup",
            "RatingBar",
            "ProgressBar",
            "ScrollView",
            "SeekBar",
            "Space",
            "Spinner",
            "StackView",
            "SurfaceView",
            "Switch",
            "TabHost",
            "TableRow",
            "TabWidget",
            "TextSwitcher",
            "TextView",
            "ToggleButton",
            "TimePicker",
            "TwoLineListItem",
            "VideoView",
            "ViewAnimator",
            "ViewFlipper",
            "ViewPager",
            "ViewStub",
            "ViewSwitcher",
            "WebView",
            "android.support.v4.widget.DrawerLayout",
            "android.support.v4.view.ViewPager",
            "android.support.v7.widget.CardView",
            "android.support.v7.widget.RecyclerView",
            "android.support.v7.widget.Toolbar",
            "view_spacer"
    };

    private boolean isKnownView(String name) {
        for (String type : viewTypes) {
            if (type.equals(name))
                return true;
        }
        return false;
    }

    IArea getElement() {
        return area;
    }
}

abstract class LayoutElementParser extends BaseParser {

    public LayoutElementParser(LayoutElementParser parentParser, Object elementSpecsParent) {
        super(parentParser, elementSpecsParent);
    }

    abstract Object parseChildSpecs(String tag, XmlPullParser parser);
    abstract void addChild(Object childSpecs, IArea child);

    public LayoutElementParser finish() {
        if (parentParser == null)
            return parentParser;
        parentParser.addChild(elementSpecsParent, getElement());
        return parentParser;
    }
}

class LinearLayoutParser extends LayoutElementParser {
    final Fragment fragment = new Fragment();

    public LinearLayoutParser(LayoutElementParser parentParser, Object elementSpecsParent, XmlPullParser parser) {
        super(parentParser, elementSpecsParent);

        fragment.setHorizontalDirection();
        String orientation = parser.getAttributeValue(null, "android:orientation");
        if (orientation != null && !orientation.equals("horizontal"))
            fragment.setVerticalDirection();
    }

    Object parseChildSpecs(String tag, XmlPullParser parser) {
        return null;
    }

    void addChild(Object childSpecs, IArea child) {
        fragment.add(child, false);
    }

    IArea getElement() {
        return fragment;
    }
}

public class AndroidXMLParser {
    private LayoutElementParser currentParser;

    public void parse(File file) throws XmlPullParserException, IOException {
        parse(new FileInputStream(file));
    }

    final private String[] layoutTypes = {
            "AbsoluteLayout",
            "FrameLayout",
            "LinearLayout",
            "TableLayout",
            "GridLayout",
            "RelativeLayout"
    };

    private LayoutElementParser getLayoutParser(String layout, LayoutElementParser parentParser,
                                                Object elementSpecsParent, XmlPullParser parser) {
        if (layout.equalsIgnoreCase("LinearLayout"))
            return new LinearLayoutParser(parentParser, elementSpecsParent, parser);
        return null;
    }

    public void parse(InputStream inputStream) throws XmlPullParserException, IOException {
        XmlPullParserFactory pullParserFactory;
        XmlPullParser parser;

        pullParserFactory = XmlPullParserFactory.newInstance();
        parser = pullParserFactory.newPullParser();
        parser.setInput(inputStream, null);

        Map<IArea, AreaInfo> areaMap = new HashMap<IArea, AreaInfo>();

        LayoutElementParser rootLayoutParser = null;
        LayoutElementParser currentLayoutParser = null;
        BaseParser currentElementParser = null;

        int tag = parser.getEventType();
        while (tag != XmlPullParser.END_DOCUMENT) {
            String key = parser.getName();
            switch (tag) {
                case XmlPullParser.START_DOCUMENT:

                    break;

                case XmlPullParser.START_TAG:
                    if (currentLayoutParser == null) {
                        rootLayoutParser = getLayoutParser(key, null, null, parser);
                        currentLayoutParser = rootLayoutParser;
                        break;
                    }
                    Object elementSpec = currentLayoutParser.parseChildSpecs(key, parser);
                    LayoutElementParser childLayoutParser = getLayoutParser(key, currentLayoutParser, elementSpec,
                            parser);
                    if (childLayoutParser == null)
                        currentElementParser = new ElementParser(currentLayoutParser, elementSpec, parser, areaMap);
                    else
                        currentLayoutParser = childLayoutParser;
                    break;

                case XmlPullParser.END_TAG:
                    if (currentElementParser != null) {
                        currentLayoutParser = currentElementParser.finish();
                        currentElementParser = null;
                    } else {
                        currentLayoutParser = currentLayoutParser.finish();
                    }
                    break;
            }
            tag = parser.next();
        }

        System.out.println(StringWriter.write((Fragment)rootLayoutParser.getElement()));
    }
}
