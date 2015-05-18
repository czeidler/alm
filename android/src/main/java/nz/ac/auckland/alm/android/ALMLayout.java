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

import java.util.HashMap;
import java.util.Map;


public class ALMLayout extends ViewGroup {
    static public class LayoutParams extends ViewGroup.LayoutParams {
        public XTab left;
        public YTab top;
        public XTab right;
        public YTab bottom;

        public LayoutParams(Context context, AttributeSet attrs) {
            super(context, attrs);

            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ALMLayout_Layout);
            for (int i = 0; i < a.getIndexCount(); i++) {
                int attr = a.getIndex(i);
                switch (attr) {
                    case R.styleable.ALMLayout_Layout_layout_toLeftOf:
                        break;
                    case R.styleable.ALMLayout_Layout_layout_below:
                        break;
                    case R.styleable.ALMLayout_Layout_layout_toRightOf:
                        break;
                    case R.styleable.ALMLayout_Layout_layout_above:
                        break;
                }
            }
            a.recycle();
        }

        public LayoutParams(XTab left, YTab top, XTab right, YTab bottom) {
            super(WRAP_CONTENT, WRAP_CONTENT);
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
        }

        public LayoutParams(int width, int height, XTab left, YTab top, XTab right, YTab bottom) {
            super(width, height);
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
        }
    }

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

        if (!(params instanceof LayoutParams))
            throw new RuntimeException();

        LayoutParams constraintParams = (LayoutParams)params;
        Area area = layoutSpec.addArea(constraintParams.left, constraintParams.top, constraintParams.right,
                constraintParams.bottom);

        area.setMinSize(getMinimumSize(child));
        area.setPreferredSize(getPreferredSize(child));
        area.setMaxSize(getMaximumSize(child));

        areaMap.put(child, area);
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

    /**
     * Adds a new x-tab to the specification.
     *
     * @return the new x-tab
     */
    public XTab addXTab() {
        return layoutSpec.addXTab();
    }

    /**
     * Adds a new y-tab to the specification.
     *
     * @return the new y-tab
     */
    public YTab addYTab() {
        return layoutSpec.addYTab();
    }


    /**
     * Finds the area that contains the given control.
     *
     * @param view the control to look for
     * @return the area that contains the control
     */
    public Area areaOf(View view) {
        return areaMap.get(view);
    }

    private Area.Size getMinimumSize(View view) {
        return new Area.Size(view.getMinimumWidth(), view.getMinimumHeight());
    }

    private Area.Size getPreferredSize(View view) {
        view.measure(MeasureSpec.makeMeasureSpec(ViewGroup.LayoutParams.WRAP_CONTENT, MeasureSpec.AT_MOST),
                MeasureSpec.makeMeasureSpec(ViewGroup.LayoutParams.WRAP_CONTENT, MeasureSpec.AT_MOST));
        return new Area.Size(view.getMeasuredWidth(), view.getMeasuredHeight());
    }

    private Area.Size getMaximumSize(View view) {
        return getPreferredSize(view);
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
}
