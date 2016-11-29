/*
 * Copyright 2016.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.nz.auckland.androidxmlparser;

import nz.ac.auckland.alm.EmptySpace;
import nz.ac.auckland.alm.IArea;
import nz.ac.auckland.alm.TabArea;
import nz.ac.auckland.alm.XTab;
import nz.ac.auckland.alm.algebra.AlgebraLayoutSpec;
import nz.ac.auckland.alm.algebra.Fragment;
import nz.ac.auckland.alm.algebra.string.StringWriter;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


class ItemInfo {
    String type = "";
    int layoutColumn = -1;

    public void parse(XmlPullParser parser) {
        this.type = parser.getName();
        String layoutColumnString = parser.getAttributeValue(null, "android:layout_column");
        if (layoutColumnString != null)
            layoutColumn = Integer.parseInt(layoutColumnString);
    }
}

abstract class ItemParser {
    // info about this item (the one we are parsing
    protected ItemInfo itemInfo;
    final private ItemParser parentParser;

    public ItemParser(ItemParser parentParser) {

        this.parentParser = parentParser;
    }

    public void parse(XmlPullParser parser) {
        this.itemInfo = createItemInfo();
        itemInfo.parse(parser);
    }

    public ItemInfo getItemInfo() {
        return itemInfo;
    }

    public ItemParser getParentParser() {
        return parentParser;
    }

    static public TabArea simplify(AlgebraLayoutSpec child) {
        if (child.getElements().size() > 1)
            return child;
        return child.getElements().get(0);
    }

    protected ItemInfo createItemInfo() {
        return new ItemInfo();
    }

    abstract public void add(AlgebraLayoutSpec child, ItemInfo childInfo);
    abstract public AlgebraLayoutSpec getLayoutSpec();
}


class ViewParser extends ItemParser {
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

    public ViewParser(ItemParser parentParser) {
        super(parentParser);
    }

    private boolean isKnownView(String name) {
        for (String type : viewTypes) {
            if (type.equals(name))
                return true;
        }
        return false;
    }

    @Override
    protected ItemInfo createItemInfo() {
        return new ItemInfo() {
            @Override
            public void parse(XmlPullParser parser) {
                super.parse(parser);

                if (!isKnownView(parser.getName()))
                    this.type = "unknown";
            }
        };
    }

    @Override
    public void add(AlgebraLayoutSpec child, ItemInfo childInfo) {
        throw new RuntimeException("Unexpected");
    }

    @Override
    public AlgebraLayoutSpec getLayoutSpec() {
        AlgebraLayoutSpec layoutSpec = new AlgebraLayoutSpec();
        layoutSpec.add(new TabArea());
        return layoutSpec;
    }
}


class LinearLayoutParser extends ItemParser {
    final protected Fragment fragment = new Fragment();

    public LinearLayoutParser(ItemParser parentParser) {
        super(parentParser);
    }

    @Override
    public void parse(XmlPullParser parser) {
        super.parse(parser);

        fragment.setHorizontalDirection();
        String orientation = parser.getAttributeValue(null, "android:orientation");
        if (orientation != null && !orientation.equals("horizontal"))
            fragment.setVerticalDirection();
    }

    @Override
    public void add(AlgebraLayoutSpec child, ItemInfo childInfo) {
        fragment.add(simplify(child), false);
    }

    @Override
    public AlgebraLayoutSpec getLayoutSpec() {
        AlgebraLayoutSpec layoutSpec = new AlgebraLayoutSpec();
        layoutSpec.add(fragment);
        return layoutSpec;
    }
}

class TableRowLayout extends LinearLayoutParser {
    public TableRowLayout(ItemParser parentParser) {
        super(parentParser);
    }

    @Override
    public void parse(XmlPullParser parser) {
        super.parse(parser);
        fragment.setHorizontalDirection();
    }
}

class TableLayoutParser extends ItemParser {
    class Row {
        AlgebraLayoutSpec child;
        ItemInfo childInfo;

        public Row(AlgebraLayoutSpec child, ItemInfo childInfo) {
            this.child = child;
            this.childInfo = childInfo;
        }
    }

    final Map<IArea, ItemInfo> areaMap;
    private int nColumns = 0;
    final List<Row> rowList = new ArrayList<Row>();

    public TableLayoutParser(ItemParser parentParser, Map<IArea, ItemInfo> areaMap) {
        super(parentParser);

        this.areaMap = areaMap;
    }

    private boolean isTableRow(Row row) {
        return row.childInfo.type.equals("TableRow");
    }

    @Override
    public void add(AlgebraLayoutSpec child, ItemInfo childInfo) {
        Row row = new Row(child, childInfo);
        rowList.add(row);

        if (isTableRow(row)) {
            int nChildItems = ((Fragment)child.getElements().get(0)).size();
            nColumns = Math.max(nColumns, nChildItems);
        }
    }

    @Override
    public AlgebraLayoutSpec getLayoutSpec() {
        Fragment fragment = new Fragment();
        fragment.setVerticalDirection();

        XTab[] tabs = new XTab[nColumns];
        for (int i = 0; i < tabs.length - 1; i++)
            tabs[i] = new XTab();

        for (Row row : rowList) {
            if (!isTableRow(row)) {
                fragment.add(simplify(row.child), false);
                continue;
            }

            IArea[] areaRow = new IArea[nColumns];
            Fragment rowFragment = (Fragment) row.child.getElements().get(0);
            assert rowFragment.isHorizontalDirection();
            int pointer = 0;
            for (IArea rowItem : (Iterable<IArea>)rowFragment.getItems()) {
                ItemInfo rowItemInfo = areaMap.get(rowItem);
                if (rowItemInfo.layoutColumn >= 0)
                    pointer = rowItemInfo.layoutColumn;
                assert pointer >= 0;
                assert pointer < nColumns;
                areaRow[pointer] = rowItem;
                pointer++;
            }
            for (int i = 0; i < areaRow.length; i++) {
                if (areaRow[i] == null)
                    areaRow[i] = new EmptySpace();
            }
            rowFragment = Fragment.horizontalFragment();
            for (int i = 0; i < areaRow.length; i++) {
                IArea rowItem = areaRow[i];
                rowFragment.add(rowItem, tabs[i], false);
            }
            fragment.add(rowFragment, false);
        }

        fragment.applySpecsToChild();
        AlgebraLayoutSpec layoutSpec = new AlgebraLayoutSpec();
        layoutSpec.add(fragment);
        return layoutSpec;
    }
}


public class AndroidXMLParser {
    public void parse(File file) throws XmlPullParserException, IOException {
        parse(new FileInputStream(file));
    }

    final private String[] layoutTypes = {
            "AbsoluteLayout",
            "FrameLayout",
            "LinearLayout",
            "TableLayout",
            "TableRow",
            "GridLayout",
            "RelativeLayout",
    };

    private boolean isKnownLayout(String name) {
        for (String type : layoutTypes) {
            if (type.equals(name))
                return true;
        }
        return false;
    }

    private ItemParser getLayoutParser(String layout, ItemParser parentParser, Map<IArea, ItemInfo> areaMap) {
        if (layout.equalsIgnoreCase("LinearLayout"))
            return new LinearLayoutParser(parentParser);
        if (layout.equalsIgnoreCase("TableLayout"))
            return new TableLayoutParser(parentParser, areaMap);
        if (layout.equalsIgnoreCase("TableRow"))
            return new TableRowLayout(parentParser);
        return null;
    }

    private ItemParser getParser(String tag, ItemParser parentParser, Map<IArea, ItemInfo> areaMap) {
        ItemParser layoutParser = getLayoutParser(tag, parentParser, areaMap);
        if (layoutParser != null)
            return layoutParser;
        return new ViewParser(parentParser);
    }

    public void parse(InputStream inputStream) throws XmlPullParserException, IOException {
        XmlPullParserFactory pullParserFactory;
        XmlPullParser parser;

        pullParserFactory = XmlPullParserFactory.newInstance();
        parser = pullParserFactory.newPullParser();
        parser.setInput(inputStream, null);

        Map<IArea, ItemInfo> areaMap = new HashMap<IArea, ItemInfo>();

        ItemParser currentParser = null;

        int tag = parser.getEventType();
        while (tag != XmlPullParser.END_DOCUMENT) {
            String key = parser.getName();
            switch (tag) {
                case XmlPullParser.START_DOCUMENT:

                    break;

                case XmlPullParser.START_TAG:
                    currentParser = getParser(key, currentParser, areaMap);
                    currentParser.parse(parser);
                    break;

                case XmlPullParser.END_TAG:
                    ItemInfo itemInfo = currentParser.getItemInfo();
                    AlgebraLayoutSpec layoutSpec = currentParser.getLayoutSpec();

                    areaMap.put(ItemParser.simplify(layoutSpec), itemInfo);

                    ItemParser parentParser = currentParser.getParentParser();
                    if (parentParser != null) {
                        parentParser.add(layoutSpec, itemInfo);
                        currentParser = parentParser;
                    }

                    break;
            }
            tag = parser.next();
        }

        System.out.println(StringWriter.write((Fragment)currentParser.getLayoutSpec().getElements().get(0)));
    }
}
