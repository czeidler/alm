/*
 * Copyright 2015.
 * Distributed under the terms of the LGPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm.android;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import nz.ac.auckland.alm.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ALMLayout extends ViewGroup {
    public final static int LEFT = 1;
    public final static int RIGHT = 2;
    public final static int TOP = 1;
    public final static int BOTTOM = 2;
    public final static int CENTER = 3;
    public final static int FILL = 4;

    /**
     * Per-child layout information associated with ALMLayout.
     *
     * @attr ref android.R.styleable#ALMLayout_Layout_layout_toLeftOf
     * @attr ref android.R.styleable#ALMLayout_Layout_layout_below
     * @attr ref android.R.styleable#ALMLayout_Layout_layout_toRightOf
     * @attr ref android.R.styleable#ALMLayout_Layout_layout_above
     * @attr ref android.R.styleable#ALMLayout_Layout_layout_leftTab
     * @attr ref android.R.styleable#ALMLayout_Layout_layout_topTab
     * @attr ref android.R.styleable#ALMLayout_Layout_layout_rightTab
     * @attr ref android.R.styleable#ALMLayout_Layout_layout_bottomTab
     * @attr ref android.R.styleable#ALMLayout_Layout_layout_horizontal_alignment
     * @attr ref android.R.styleable#ALMLayout_Layout_layout_vertical_alignment
     */
    static public class LayoutParams extends ViewGroup.LayoutParams {
        public AreaRef areaRef = new AreaRef();

        public LayoutParams(Context context, AttributeSet attrs) {
            super(context, attrs);

            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ALMLayout_Layout);
            final int n = a.getIndexCount();
            for (int i = 0; i < n; i++) {
                int attr = a.getIndex(i);
                switch (attr) {
                    case R.styleable.ALMLayout_Layout_layout_toLeftOf:
                        areaRef.right.setTo(a.getResourceId(attr, 0));
                        break;
                    case R.styleable.ALMLayout_Layout_layout_below:
                        areaRef.top.setTo(a.getResourceId(attr, 0));
                        break;
                    case R.styleable.ALMLayout_Layout_layout_toRightOf:
                        areaRef.left.setTo(a.getResourceId(attr, 0));
                        break;
                    case R.styleable.ALMLayout_Layout_layout_above:
                        areaRef.bottom.setTo(a.getResourceId(attr, 0));
                        break;
                    case R.styleable.ALMLayout_Layout_layout_alignLeft:
                        areaRef.left.setAlignTo(a.getResourceId(attr, 0));
                        break;
                    case R.styleable.ALMLayout_Layout_layout_alignTop:
                        areaRef.top.setAlignTo(a.getResourceId(attr, 0));
                        break;
                    case R.styleable.ALMLayout_Layout_layout_alignRight:
                        areaRef.right.setAlignTo(a.getResourceId(attr, 0));
                        break;
                    case R.styleable.ALMLayout_Layout_layout_alignBottom:
                        areaRef.bottom.setAlignTo(a.getResourceId(attr, 0));
                        break;
                    case R.styleable.ALMLayout_Layout_layout_leftTab:
                        areaRef.left.setTo(a.getString(attr));
                        break;
                    case R.styleable.ALMLayout_Layout_layout_topTab:
                        areaRef.top.setTo(a.getString(attr));
                        break;
                    case R.styleable.ALMLayout_Layout_layout_rightTab:
                        areaRef.right.setTo(a.getString(attr));
                        break;
                    case R.styleable.ALMLayout_Layout_layout_bottomTab:
                        areaRef.bottom.setTo(a.getString(attr));
                        break;
                    case R.styleable.ALMLayout_Layout_layout_horizontal_alignment:
                        switch (a.getInteger(attr, 0)) {
                            case LEFT:
                                areaRef.horizontalAlignment = HorizontalAlignment.LEFT;
                                break;
                            case RIGHT:
                                areaRef.horizontalAlignment = HorizontalAlignment.RIGHT;
                                break;
                            case CENTER:
                                areaRef.horizontalAlignment = HorizontalAlignment.CENTER;
                                break;
                            case FILL:
                                areaRef.horizontalAlignment = HorizontalAlignment.FILL;
                                break;
                        }
                        break;
                    case R.styleable.ALMLayout_Layout_layout_vertical_alignment:
                        switch (a.getInteger(attr, 0)) {
                            case TOP:
                                areaRef.verticalAlignment = VerticalAlignment.TOP;
                                break;
                            case BOTTOM:
                                areaRef.verticalAlignment = VerticalAlignment.BOTTOM;
                                break;
                            case CENTER:
                                areaRef.verticalAlignment = VerticalAlignment.CENTER;
                                break;
                            case FILL:
                                areaRef.verticalAlignment = VerticalAlignment.FILL;
                                break;
                        }
                        break;
                }
            }
            a.recycle();

            int[] attrsArray = new int[] {
                    android.R.attr.id, // 0
            };
            a = context.obtainStyledAttributes(attrs, attrsArray);
            for (int i = 0; i < a.getIndexCount(); i++) {
                int attr = a.getIndex(i);
                switch (attr) {
                    case 0:
                        areaRef.id = a.getResourceId(attr, -1);
                        break;
                }
            }
            a.recycle();
        }

        public LayoutParams(XTab left, YTab top, XTab right, YTab bottom) {
            super(WRAP_CONTENT, WRAP_CONTENT);

            init(left, top, right, bottom);
        }

        public LayoutParams(int width, int height, XTab left, YTab top, XTab right, YTab bottom) {
            super(width, height);

            init(left, top, right, bottom);
        }

        private void init(XTab left, YTab top, XTab right, YTab bottom) {
            areaRef.left.setTo(left);
            areaRef.top.setTo(top);
            areaRef.right.setTo(right);
            areaRef.bottom.setTo(bottom);
        }
    }

    boolean layoutSpecsNeedRebuild = true;
    final LayoutSpec layoutSpec = new LayoutSpec();
    final Map<View, Area> areaMap = new HashMap<View, Area>();

    public ALMLayout(Context context) {
        super(context);
    }

    public ALMLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ALMLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void layoutChild(View child, Area area) {
        Area.Rect frame = area.getContentRect();
        child.layout((int)frame.left, (int)frame.top, (int)frame.right, (int)frame.bottom);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (layoutSpecsNeedRebuild)
            rebuildLayoutSpecs();

        layoutSpec.setRight(getWidth());
        layoutSpec.setBottom(getHeight());

        layoutSpec.solve();

        // set the calculated positions and sizes for every area
        for (Map.Entry<View, Area> entry : areaMap.entrySet()) {
            View child = entry.getKey();
            Area area = entry.getValue();
            layoutChild(child, area);
        }
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);

        layoutSpecsNeedRebuild = true;
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams params) {
        return params instanceof LayoutParams;
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(getLeftTab(), getTopTab(), getRightTab(), getBottomTab());
    }

    private void rebuildLayoutSpecs() {
        layoutSpecsNeedRebuild = false;
        layoutSpec.clear();
        areaMap.clear();

        List<AreaRef> areaRefList = new ArrayList<AreaRef>();
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            LayoutParams params = (LayoutParams)child.getLayoutParams();
            areaRefList.add(params.areaRef);
        }
        LayoutBuilder.resolveToTabs(areaRefList, layoutSpec);

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            LayoutParams params = (LayoutParams)child.getLayoutParams();
            AreaRef areaRef = params.areaRef;

            Area area = layoutSpec.addArea((XTab)areaRef.left.relation, (YTab)areaRef.top.relation,
                    (XTab)areaRef.right.relation, (YTab)areaRef.bottom.relation);

            area.setMinSize(getMinimumSize(child));
            area.setPreferredSize(getPreferredSize(child));
            area.setMaxSize(getMaximumSize(child));

            if (child.getLayoutParams().width == LayoutParams.MATCH_PARENT)
                areaRef.horizontalAlignment = HorizontalAlignment.FILL;
            if (child.getLayoutParams().height == LayoutParams.MATCH_PARENT)
                areaRef.verticalAlignment = VerticalAlignment.FILL;
            area.setAlignment(areaRef.horizontalAlignment, areaRef.verticalAlignment);

            areaMap.put(child, area);
        }
    }

    /**
     * Finds the area that contains the given control.
     *
     * @param view the control to look for
     * @return the area that contains the control
     */
    public Area areaOf(View view) {
        if (layoutSpecsNeedRebuild)
            rebuildLayoutSpecs();
        return areaMap.get(view);
    }

    private Area.Size getMinimumSize(View view) {
        return new Area.Size(view.getMinimumWidth(), view.getMinimumHeight());
    }

    private Area.Size getPreferredSize(View view) {
        view.measure(MeasureSpec.makeMeasureSpec(LayoutParams.WRAP_CONTENT, MeasureSpec.AT_MOST),
                MeasureSpec.makeMeasureSpec(LayoutParams.WRAP_CONTENT, MeasureSpec.AT_MOST));
        return new Area.Size(view.getMeasuredWidth(), view.getMeasuredHeight());
    }

    private Area.Size getMaximumSize(View view) {
        final int LARGE_SIZE = 8000;

        ViewGroup.LayoutParams viewLayoutParams = view.getLayoutParams();

        Area.Size maxSize;
        if (viewLayoutParams.width == LayoutParams.WRAP_CONTENT
                || viewLayoutParams.height == LayoutParams.WRAP_CONTENT)
            maxSize = getPreferredSize(view);
        else
            maxSize = new Area.Size(0, 0);

        // max width
        if (viewLayoutParams.width == LayoutParams.MATCH_PARENT)
            maxSize.setWidth(LARGE_SIZE);
        else if (viewLayoutParams.width != LayoutParams.WRAP_CONTENT)
            maxSize.setWidth(viewLayoutParams.width);

        // max height
        if (viewLayoutParams.height == LayoutParams.MATCH_PARENT)
            maxSize.setHeight(LARGE_SIZE);
        else if (viewLayoutParams.height != LayoutParams.WRAP_CONTENT)
            maxSize.setHeight(viewLayoutParams.height);

        return maxSize;
    }

    /**
     * Get the X-tab for the left border of the GUI
     *
     * @return X-tab for the left border of the GUI
     */
    public XTab getLeftTab() {
        return layoutSpec.getLeft();
    }

    /**
     * Get the X-tab for the right border of the GUI
     *
     * @return X-tab for the right border of the GUI
     */
    public XTab getRightTab() {
        return layoutSpec.getRight();
    }

    /**
     * Get the Y-tab for the top border of the GUI
     *
     * @return Y-tab for the top border of the GUI
     */
    public YTab getTopTab() {
        return layoutSpec.getTop();
    }

    /**
     * Get the Y-tab for the bottom border of the GUI
     *
     * @return Y-tab for the bottom border of the GUI
     */
    public YTab getBottomTab() {
        return layoutSpec.getBottom();
    }

    public void setSpacing(float horizontalSpacing, float verticalSpacing) {
        layoutSpec.setHorizontalSpacing(horizontalSpacing);
        layoutSpec.setVerticalSpacing(verticalSpacing);
    }

    public void setSpacing(float spacing) {
        setSpacing(spacing, spacing);
    }

    public void setInset(float left, float top, float right, float bottom) {
        layoutSpec.setLeftInset(left);
        layoutSpec.setTopInset(top);
        layoutSpec.setRightInset(right);
        layoutSpec.setBottomInset(bottom);
    }
}
