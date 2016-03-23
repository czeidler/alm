/*
 * Copyright 2015.
 * Distributed under the terms of the LGPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm.android;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import nz.ac.auckland.alm.*;
import nz.ac.auckland.linsolve.Constraint;

import java.util.*;


public class ALMLayout extends ViewGroup implements IALMLayoutSpecs {
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
     * @attr ref android.R.styleable#ALMLayout_Layout_layout_prefWidth
     * @attr ref android.R.styleable#ALMLayout_Layout_layout_prefHeight
     */
    static public class LayoutParams extends ViewGroup.LayoutParams {
        public AreaRef areaRef = new AreaRef();

        public LayoutParams(Context context, AttributeSet attrs) {
            super(context, attrs);

            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ALMLayout_Layout);
            final int n = a.getIndexCount();
            for (int i = 0; i < n; i++) {
                int attr = a.getIndex(i);
                if (attr == R.styleable.ALMLayout_Layout_layout_toLeftOf) {
                    areaRef.right.setTo(a.getResourceId(attr, 0));

                } else if (attr == R.styleable.ALMLayout_Layout_layout_below) {
                    areaRef.top.setTo(a.getResourceId(attr, 0));

                } else if (attr == R.styleable.ALMLayout_Layout_layout_toRightOf) {
                    areaRef.left.setTo(a.getResourceId(attr, 0));

                } else if (attr == R.styleable.ALMLayout_Layout_layout_above) {
                    areaRef.bottom.setTo(a.getResourceId(attr, 0));

                } else if (attr == R.styleable.ALMLayout_Layout_layout_alignLeft) {
                    areaRef.left.setAlignTo(a.getResourceId(attr, 0));

                } else if (attr == R.styleable.ALMLayout_Layout_layout_alignTop) {
                    areaRef.top.setAlignTo(a.getResourceId(attr, 0));

                } else if (attr == R.styleable.ALMLayout_Layout_layout_alignRight) {
                    areaRef.right.setAlignTo(a.getResourceId(attr, 0));

                } else if (attr == R.styleable.ALMLayout_Layout_layout_alignBottom) {
                    areaRef.bottom.setAlignTo(a.getResourceId(attr, 0));

                } else if (attr == R.styleable.ALMLayout_Layout_layout_leftTab) {
                    areaRef.left.setTo(a.getString(attr));

                } else if (attr == R.styleable.ALMLayout_Layout_layout_topTab) {
                    areaRef.top.setTo(a.getString(attr));

                } else if (attr == R.styleable.ALMLayout_Layout_layout_rightTab) {
                    areaRef.right.setTo(a.getString(attr));

                } else if (attr == R.styleable.ALMLayout_Layout_layout_bottomTab) {
                    areaRef.bottom.setTo(a.getString(attr));

                } else if (attr == R.styleable.ALMLayout_Layout_layout_horizontal_alignment) {
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

                } else if (attr == R.styleable.ALMLayout_Layout_layout_vertical_alignment) {
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

                } else if (attr == R.styleable.ALMLayout_Layout_layout_minWidth) {
                    areaRef.explicitMinSize.setWidth(a.getDimensionPixelSize(attr, Area.Size.UNDEFINED));
                } else if (attr == R.styleable.ALMLayout_Layout_layout_minHeight) {
                    areaRef.explicitMinSize.setHeight(a.getDimensionPixelSize(attr, Area.Size.UNDEFINED));
                } else if (attr == R.styleable.ALMLayout_Layout_layout_prefWidth) {
                    areaRef.explicitPrefSize.setWidth(a.getDimensionPixelSize(attr, Area.Size.UNDEFINED));
                } else if (attr == R.styleable.ALMLayout_Layout_layout_prefHeight) {
                    areaRef.explicitPrefSize.setHeight(a.getDimensionPixelSize(attr, Area.Size.UNDEFINED));
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

    final private AbstractViewInfoParser<View> viewInfoParser = new AbstractViewInfoParser<View>() {
        @Override
        protected Area.Size getLayoutParams(View view) {
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            return new Area.Size(layoutParams.width, layoutParams.height);
        }

        @Override
        protected String getClassName(View component) {
            return component.getClass().getSimpleName();
        }

        @Override
        protected Area.Size getRootViewSize(View view) {
            View rootView = view.getRootView();
            return new Area.Size(rootView.getWidth(), rootView.getHeight());
        }

        @Override
        protected Area.Size getMinSizeRaw(View view) {
            Area.Size size = new Area.Size(-1, -1);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                size.setWidth(view.getMinimumWidth());
                size.setHeight(view.getMinimumHeight());
            }
            return size;
        }

        private Area.Size measureSizeAtMost(View view, int width, int Height, int mode) {
            view.measure(MeasureSpec.makeMeasureSpec(width, mode), MeasureSpec.makeMeasureSpec(Height, mode));
            return new Area.Size(view.getMeasuredWidth(), view.getMeasuredHeight());
        }

        @Override
        protected Area.Size getPreferredSizeRaw(View view) {
            return measureSizeAtMost(view, 0, 0, MeasureSpec.UNSPECIFIED);
        }

        @Override
        protected Area.Size getMaxSizeRaw(View view) {
            Area.Size rootSize = getRootViewSize(view);
            return measureSizeAtMost(view, (int)rootSize.getWidth(), (int)rootSize.getHeight(), MeasureSpec.AT_MOST);
        }
    };

    public ALMLayout(Context context) {
        super(context);
    }

    public ALMLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        readAttributes(context, attrs);
    }

    public ALMLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        readAttributes(context, attrs);
    }

    private void readAttributes(Context context, AttributeSet attrs) {
        int[] attrsArray = new int[] {
                android.R.attr.padding, // 0
                android.R.attr.paddingBottom,
                android.R.attr.paddingEnd,
                android.R.attr.paddingLeft,
                android.R.attr.paddingMode,
                android.R.attr.paddingRight,
                android.R.attr.paddingStart,
                android.R.attr.paddingTop
        };
        final TypedArray a = context.obtainStyledAttributes(attrs, attrsArray);
        for (int i = 0; i < a.getIndexCount(); i++) {
            int attr = a.getIndex(i);
            int padding = a.getDimensionPixelSize(attr, 0);
            switch (attr) {
                case 0: //android.R.attr.padding
                    layoutSpec.setLeftInset(padding);
                    layoutSpec.setTopInset(padding);
                    layoutSpec.setRightInset(padding);
                    layoutSpec.setBottomInset(padding);
                    break;
                case 1: //android.R.attr.paddingBottom
                    layoutSpec.setBottomInset(padding);
                    break;
                case 2: //android.R.attr.paddingEnd
                    Configuration config = getResources().getConfiguration();
                    if ((config.screenLayout & Configuration.SCREENLAYOUT_LAYOUTDIR_MASK)
                            == Configuration.SCREENLAYOUT_LAYOUTDIR_LTR)
                        layoutSpec.setRightInset(padding);
                    else
                        layoutSpec.setLeftInset(padding);
                    break;
                case 3: //android.R.attr.paddingLeft
                    layoutSpec.setLeftInset(padding);
                    break;
                case 4: //android.R.attr.paddingMode
                    break;
                case 5: //android.R.attr.paddingRight
                    layoutSpec.setRightInset(padding);
                    break;
                case 6: //android.R.attr.paddingStart
                    config = getResources().getConfiguration();
                    if ((config.screenLayout & Configuration.SCREENLAYOUT_LAYOUTDIR_MASK)
                            == Configuration.SCREENLAYOUT_LAYOUTDIR_LTR)
                        layoutSpec.setLeftInset(padding);
                    else
                        layoutSpec.setRightInset(padding);
                    break;
                case 7: //android.R.attr.paddingTop
                    layoutSpec.setTopInset(padding);
                    break;
            }
        }
        a.recycle();
    }

    private void layoutChild(View child, Area area) {
        Area.Rect frame = area.getContentRect();
        child.layout((int)frame.left, (int)frame.top, (int)frame.right, (int)frame.bottom);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        layoutSpecsNeedRebuild = true;
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

            area.setMinSize(viewInfoParser.getMinSize(child, areaRef.explicitMinSize));
            area.setPreferredSize(viewInfoParser.getPreferredSize(child, areaRef.explicitPrefSize));
            area.setMaxSize(viewInfoParser.getMaxSize(child));
            AbstractViewInfoParser.Alignment alignment = viewInfoParser.getAlignment(child);
            areaRef.horizontalAlignment = alignment.horizontalAlignment;
            areaRef.verticalAlignment = alignment.verticalAlignment;
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

    @Override
    public XTab getLeftTab() {
        return layoutSpec.getLeft();
    }

    @Override
    public XTab getRightTab() {
        return layoutSpec.getRight();
    }

    @Override
    public YTab getTopTab() {
        return layoutSpec.getTop();
    }

    @Override
    public YTab getBottomTab() {
        return layoutSpec.getBottom();
    }

    @Override
    public Area getArea(Object object) {
        return areaOf((View)object);
    }

    @Override
    public List<IArea> getAreas() {
        return new ArrayList<IArea>(layoutSpec.getAreas());
    }

    @Override
    public List<Constraint> getCustomConstraints() {
        return Collections.emptyList();
    }
}
