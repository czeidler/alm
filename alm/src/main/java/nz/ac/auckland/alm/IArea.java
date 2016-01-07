/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm;


public interface IArea {
    void setId(String id);
    String getId();

    XTab getLeft();
    YTab getTop();
    XTab getRight();
    YTab getBottom();

    void setLeft(XTab value);
    void setRight(XTab value);
    void setTop(YTab value);
    void setBottom(YTab value);

    Area.Rect getRect();

    Object getCookie();

    /**
     * Assign a cookie to the Area. E.g. the associated view.
     * @param cookie the cookie object
     */
    void setCookie(Object cookie);
}
