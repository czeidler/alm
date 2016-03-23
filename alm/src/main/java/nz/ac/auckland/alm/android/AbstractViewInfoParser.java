/*
 * Copyright 2016.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm.android;

import nz.ac.auckland.alm.Area;
import nz.ac.auckland.alm.HorizontalAlignment;
import nz.ac.auckland.alm.VerticalAlignment;


/**
 * Sizes can either be parsed from Android or from the IDE.
 *
 * This abstract class defines some common behaviour.
 *
 * @param <T>
 */
abstract public class AbstractViewInfoParser<T> {
    static public final int MATCH_PARENT = -1;
    static public final int WRAP_CONTENT = -2;

    abstract protected String getClassName(T component);
    abstract protected Area.Size getRootViewSize(T component);
    abstract protected Area.Size getLayoutParams(T component);
    abstract protected Area.Size getMinSizeRaw(T component);
    abstract protected Area.Size getPreferredSizeRaw(T component);
    abstract protected Area.Size getMaxSizeRaw(T component);

    public Area.Size getMinSize(T component, Area.Size explicitSize) {
        if (explicitSize.getWidth() != Area.Size.UNDEFINED && explicitSize.getHeight() != Area.Size.UNDEFINED)
            return explicitSize;

        Area.Size minSize = getMinSizeRaw(component);
        if (minSize.getWidth() <= 0 && minSize.getHeight() <= 0)
            minSize = getPreferredSizeRaw(component);
        if (minSize.getWidth() <= 0)
            minSize.setWidth(0);
        if (minSize.getHeight() <= 0)
            minSize.setHeight(0);

        // fix size
        Area.Size layoutParams = getLayoutParams(component);
        if (layoutParams.getWidth() != MATCH_PARENT && layoutParams.getWidth() != WRAP_CONTENT)
            minSize.setWidth(layoutParams.getWidth());
        if (layoutParams.getHeight() != MATCH_PARENT && layoutParams.getHeight() != WRAP_CONTENT)
            minSize.setHeight(layoutParams.getHeight());

        // fix sizes
        Area.Size rootSize = getRootViewSize(component);
        if (minSize.getWidth() > rootSize.getWidth() / 2)
            minSize.setWidth(0);
        if (minSize.getHeight() > rootSize.getHeight() / 2)
            minSize.setHeight(0);

        if (explicitSize.getWidth() != Area.Size.UNDEFINED)
            minSize.setWidth(explicitSize.getWidth());
        if (explicitSize.getHeight() != Area.Size.UNDEFINED)
            minSize.setHeight(explicitSize.getHeight());

        return minSize;
    }

    private Area.Size validateRawSize(T component, Area.Size size, Area.Size layoutParams) {
        Area.Size rootSize = getRootViewSize(component);
        // max width
        if (layoutParams.getWidth() == MATCH_PARENT)
            size.setWidth(rootSize.getWidth());
        else if (layoutParams.getWidth() != WRAP_CONTENT)
            size.setWidth(layoutParams.getWidth());

        // max height
        if (layoutParams.getHeight() == MATCH_PARENT)
            size.setHeight(rootSize.getHeight());
        else if (layoutParams.getHeight() != WRAP_CONTENT)
            size.setHeight(layoutParams.getHeight());

        // fix sizes
        if (size.getWidth() > rootSize.getWidth() || size.getWidth() < 0)
            size.setWidth(rootSize.getWidth());
        if (size.getHeight() > rootSize.getHeight() || size.getHeight() < 0)
            size.setHeight(rootSize.getHeight());

        return size;
    }

    public Area.Size getPreferredSize(T component, Area.Size explicitSize) {
        if (explicitSize.getWidth() != Area.Size.UNDEFINED && explicitSize.getHeight() != Area.Size.UNDEFINED)
            return explicitSize;

        Area.Size prefSize = getPreferredSizeRaw(component);

        // specified size
        Area.Size layoutParams = getLayoutParams(component);
        if (layoutParams.getWidth() != MATCH_PARENT && layoutParams.getWidth() != WRAP_CONTENT)
            prefSize.setWidth(layoutParams.getWidth());
        if (layoutParams.getHeight() != MATCH_PARENT && layoutParams.getHeight() != WRAP_CONTENT)
            prefSize.setHeight(layoutParams.getHeight());

        // validate sizes
        Area.Size rootSize = getRootViewSize(component);
        if (prefSize.getWidth() > rootSize.getWidth() || prefSize.getWidth() < 0)
            prefSize.setWidth(rootSize.getWidth());
        if (prefSize.getHeight() > rootSize.getHeight() || prefSize.getHeight() < 0)
            prefSize.setHeight(rootSize.getHeight());

        // override with explicit sizes
        if (explicitSize.getWidth() != Area.Size.UNDEFINED)
            prefSize.setWidth(explicitSize.getWidth());
        if (explicitSize.getHeight() != Area.Size.UNDEFINED)
            prefSize.setHeight(explicitSize.getHeight());
        return prefSize;
    }

    public Area.Size getMaxSize(T component) {
        Area.Size layoutParams = getLayoutParams(component);
        Area.Size maxSize;
        if (layoutParams.getWidth() == WRAP_CONTENT
                || layoutParams.getHeight() == WRAP_CONTENT)
            maxSize = getMaxSizeRaw(component);
        else
            maxSize = new Area.Size(0, 0);

        return validateRawSize(component, maxSize, layoutParams);
    }

    public static class Alignment {
        public HorizontalAlignment horizontalAlignment = HorizontalAlignment.CENTER;
        public VerticalAlignment verticalAlignment = VerticalAlignment.CENTER;
    }

    public Alignment getAlignment(T component) {
        Area.Size layoutParams = getLayoutParams(component);
        Alignment alignment = new Alignment();
        if (layoutParams.getWidth() == MATCH_PARENT)
            alignment.horizontalAlignment = HorizontalAlignment.FILL;
        if (layoutParams.getHeight() == MATCH_PARENT)
            alignment.verticalAlignment = VerticalAlignment.FILL;
        if (getClassName(component).equals("TextView"))
            alignment.horizontalAlignment = HorizontalAlignment.LEFT;
        return alignment;
    }
}
