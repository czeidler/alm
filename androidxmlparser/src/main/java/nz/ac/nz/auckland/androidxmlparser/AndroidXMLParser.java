/*
 * Copyright 2016.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.nz.auckland.androidxmlparser;

import nz.ac.auckland.alm.*;
import nz.ac.auckland.alm.algebra.AlgebraData;
import nz.ac.auckland.alm.algebra.AlgebraLayoutSpec;
import nz.ac.auckland.alm.algebra.Fragment;
import nz.ac.auckland.alm.algebra.string.AlgebraSpec;
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
    String id = null;
    int layoutColumn = -1;
    int layoutRow = -1;
    int layoutColumnSpan = -1;
    int layoutRowSpan = -1;

    public void parse(XmlPullParser parser) {
        this.type = parser.getName();
        String id = parser.getAttributeValue(null, "android:id");
        if (id != null) {
            if (id.startsWith("@+id/"))
                id = id.substring("@+id/".length());
            else if (id.startsWith("@id/"))
                id = id.substring("@id/".length());
            this.id = id;
        }
        String layoutColumnString = parser.getAttributeValue(null, "android:layout_column");
        if (layoutColumnString != null)
            layoutColumn = Integer.parseInt(layoutColumnString);
        String layoutRowString = parser.getAttributeValue(null, "android:layout_row");
        if (layoutRowString != null)
            layoutRow = Integer.parseInt(layoutRowString);
        String layoutColumnSpanString = parser.getAttributeValue(null, "android:layout_columnSpan");
        if (layoutColumnSpanString != null)
            layoutColumnSpan = Integer.parseInt(layoutColumnSpanString);
        String layoutRowSpanString = parser.getAttributeValue(null, "android:layout_rowSpan");
        if (layoutRowSpanString != null)
            layoutRowSpan = Integer.parseInt(layoutRowSpanString);
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
        TabArea area = new TabArea();
        if (itemInfo.type.equals("Space"))
            area = new EmptySpace();
        area.setId(itemInfo.id);
        layoutSpec.add(area);
        return layoutSpec;
    }
}

class FrameLayoutParser extends ItemParser {
    final private AlgebraLayoutSpec layoutSpec = new AlgebraLayoutSpec();

    public FrameLayoutParser(ItemParser parentParser) {
        super(parentParser);
    }

    public void add(AlgebraLayoutSpec child, ItemInfo childInfo) {
        layoutSpec.add(simplify(child));
    }

    public AlgebraLayoutSpec getLayoutSpec() {
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

class TableRowLayoutParser extends LinearLayoutParser {
    public TableRowLayoutParser(ItemParser parentParser) {
        super(parentParser);
    }

    @Override
    public void parse(XmlPullParser parser) {
        super.parse(parser);
        fragment.setHorizontalDirection();
    }
}

class GridLayoutParser extends  ItemParser {
    class Item {
        AlgebraLayoutSpec child;
        ItemInfo childInfo;

        public Item(AlgebraLayoutSpec child, ItemInfo childInfo) {
            this.child = child;
            this.childInfo = childInfo;
        }
    }

    private int nColumns = 0;
    private int nRows = 0;
    private List<Item> itemList = new ArrayList<Item>();

    public GridLayoutParser(ItemParser parentParser) {
        super(parentParser);
    }

    @Override
    protected ItemInfo createItemInfo() {
        return new ItemInfo() {
            @Override
            public void parse(XmlPullParser parser) {
                super.parse(parser);

                String row = parser.getAttributeValue(null, "android:rowCount");
                if (row != null)
                    nRows = Integer.parseInt(row);
                String column = parser.getAttributeValue(null, "android:columnCount");
                if (column != null)
                    nColumns = Integer.parseInt(column);
            }
        };
    }

    public void add(AlgebraLayoutSpec child, ItemInfo childInfo) {
        itemList.add(new Item(child, childInfo));
    }

    private void set(IArea area, int column, int row, int columnSpan, int rowSpan, XTab[] xTabs, YTab[] yTabs) {
        area.setLeft(xTabs[column]);
        area.setRight(xTabs[column + columnSpan]);
        area.setTop(yTabs[row]);
        area.setBottom(yTabs[row + rowSpan]);
    }

    public AlgebraLayoutSpec getLayoutSpec() {
        XTab[] xTabs = new XTab[nColumns + 1];
        YTab[] yTabs = new YTab[nRows + 1];
        for (int i = 0; i < xTabs.length; i++)
            xTabs[i] = new XTab();
        for (int i = 0; i < yTabs.length; i++)
            yTabs[i] = new YTab();

        AlgebraData  algebraData = new AlgebraData(xTabs[0], yTabs[0], xTabs[nColumns], yTabs[nRows]);
        int columnPointer = 0;
        int rowPointer = 0;
        for (Item item : itemList) {
            ItemInfo itemInfo = item.childInfo;
            IArea area = simplify(item.child);

            int column = columnPointer;
            int row = rowPointer;
            int columnSpan = 1;
            int rowSpan = 1;

            if (itemInfo.layoutColumn >= 0)
                column = itemInfo.layoutColumn;
            if (itemInfo.layoutRow >= 0)
                row = itemInfo.layoutRow;
            if (itemInfo.layoutColumnSpan >= 1)
                columnSpan = itemInfo.layoutColumnSpan;
            if (itemInfo.layoutRowSpan >= 1)
                rowSpan = itemInfo.layoutRowSpan;

            set(area, column, row, columnSpan, rowSpan, xTabs, yTabs);
            algebraData.addArea(area);

            // advance pointer
            columnPointer = column + columnSpan;
            rowPointer = row + (rowSpan - 1);
            if (columnPointer >= nColumns) {
                columnPointer = 0;
                rowPointer++;
            }
        }

        AlgebraSpec algebraSpec = new AlgebraSpec(algebraData);
        algebraSpec.compress();
        List<Fragment> fragments = algebraSpec.getFragments();

        AlgebraLayoutSpec layoutSpec = new AlgebraLayoutSpec();
        for (Fragment fragment : fragments)
            layoutSpec.add(fragment);
        return layoutSpec;
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
        if (layout.equalsIgnoreCase("FrameLayout"))
            return new FrameLayoutParser(parentParser);
        if (layout.equalsIgnoreCase("LinearLayout"))
            return new LinearLayoutParser(parentParser);
        if (layout.equalsIgnoreCase("TableLayout"))
            return new TableLayoutParser(parentParser, areaMap);
        if (layout.equalsIgnoreCase("TableRow"))
            return new TableRowLayoutParser(parentParser);
        if (layout.equalsIgnoreCase("GridLayout"))
            return new GridLayoutParser(parentParser);
        return null;
    }

    private ItemParser getParser(String tag, ItemParser parentParser, Map<IArea, ItemInfo> areaMap) {
        ItemParser layoutParser = getLayoutParser(tag, parentParser, areaMap);
        if (layoutParser != null)
            return layoutParser;
        return new ViewParser(parentParser);
    }

    public AlgebraLayoutSpec parse(InputStream inputStream) throws XmlPullParserException, IOException {
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

        IArea mainElement = currentParser.getLayoutSpec().getElements().get(0);
        if (mainElement instanceof Fragment)
            System.out.println(StringWriter.write((Fragment)mainElement));
        else {
            Fragment fragment = Fragment.horizontalFragment();
            fragment.add(mainElement, false);
            System.out.println(StringWriter.write(fragment));
        }

        return currentParser.getLayoutSpec();
    }
}
