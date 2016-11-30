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
import nz.ac.auckland.alm.algebra.SoundLayoutBuilder;
import nz.ac.auckland.alm.algebra.string.AlgebraSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RelativeLayoutParser extends ItemParser {
    static class Item {
        AlgebraLayoutSpec child;
        ItemInfo childInfo;

        public Item(AlgebraLayoutSpec child, ItemInfo childInfo) {
            this.child = child;
            this.childInfo = childInfo;
        }
    }

    List<Item> itemList = new ArrayList<Item>();
    Map<String, Item> idMap = new HashMap<String, Item>();

    public RelativeLayoutParser(ItemParser parentParser) {
        super(parentParser);
    }

    public void add(AlgebraLayoutSpec child, ItemInfo childInfo) {
        Item item = new Item(child, childInfo);
        itemList.add(item);
        if (childInfo.id != null)
            idMap.put(childInfo.id, item);
    }

    private Item find(String idRef) {
        return idMap.get(ItemInfo.removeIdPrefix(idRef));
    }

    private LayoutSpec solve(AlgebraData algebraData) {
        LayoutSpec layoutSpec = new LayoutSpec(null, algebraData.getLeft(), algebraData.getTop(),
                algebraData.getRight(), algebraData.getBottom());
        for (IArea area : algebraData.getAllAreas()) {
            if (area instanceof AlgebraLayoutSpec) {
                throw new RuntimeException("implement");
            } else
                layoutSpec.addArea((ILayoutSpecArea)area);
        }

        layoutSpec.setLeft(0.d);
        layoutSpec.setTop(0.d);
        layoutSpec.setRight(1000.d);
        layoutSpec.setBottom(1000.d);

        layoutSpec.solve();
        return layoutSpec;
    }

    private AlgebraLayoutSpec algebraLayoutSpec;

    public AlgebraLayoutSpec getLayoutSpec() {
        if (algebraLayoutSpec != null)
            return algebraLayoutSpec;

        AlgebraData algebraData = new AlgebraData(new XTab(), new YTab(), new XTab(), new YTab());
        for (Item item : itemList) {
            IArea area = simplify(item.child);
            ItemInfo itemInfo = item.childInfo;
            RelativeParamsInfo relInfo = itemInfo.relativeParamsInfo;

            {
                XTab left = null;
                if (relInfo.centerH || relInfo.alignParentStart) {
                    left = algebraData.getLeft();
                } else if (relInfo.toEndOf != null) {
                    Item itemRef = find(relInfo.toEndOf);
                    if (itemRef != null) {
                        IArea ref = itemRef.child;
                        left = ref.getRight();
                        if (left == null) {
                            left = new XTab();
                            ref.setRight(left);
                        }
                    }
                } else if (relInfo.alignStart != null) {
                    Item itemRef = find(relInfo.alignStart);
                    if (itemRef != null) {
                        IArea ref = itemRef.child;
                        left = ref.getLeft();
                        if (left == null) {
                            left = new XTab();
                            ref.setLeft(left);
                        }
                    }
                }
                if (left != null)
                    area.setLeft(left);
            }

            {
                XTab right = null;
                if (relInfo.centerH || relInfo.alignParentEnd) {
                    right = algebraData.getRight();
                } else if (relInfo.toStartOf != null) {
                    Item itemRef = find(relInfo.toStartOf);
                    if (itemRef != null) {
                        IArea ref = itemRef.child;
                        right = ref.getLeft();
                        if (right == null) {
                            right = new XTab();
                            ref.setLeft(right);
                        }
                    }
                } else if (relInfo.alignEnd != null) {
                    Item itemRef = find(relInfo.alignEnd);
                    if (itemRef != null) {
                        IArea ref = itemRef.child;
                        right = ref.getRight();
                        if (right == null) {
                            right = new XTab();
                            ref.setRight(right);
                        }
                    }
                }
                if (right != null)
                    area.setRight(right);
            }

            {
                YTab top = null;
                if (relInfo.centerV || relInfo.alignParentTop) {
                    top = algebraData.getTop();
                } else if (relInfo.below != null) {
                    Item itemRef = find(relInfo.below);
                    if (itemRef != null) {
                        IArea ref = itemRef.child;
                        top = ref.getBottom();
                        if (top == null) {
                            top = new YTab();
                            ref.setBottom(top);
                        }
                    }
                } else if (relInfo.alignTop != null) {
                    Item itemRef = find(relInfo.alignTop);
                    if (itemRef != null) {
                        IArea ref = itemRef.child;
                        top = ref.getTop();
                        if (top == null) {
                            top = new YTab();
                            ref.setTop(top);
                        }
                    }
                }
                if (top != null)
                    area.setTop(top);
            }

            {
                YTab bottom = null;
                if (relInfo.centerV || relInfo.alignParentBottom) {
                    bottom = algebraData.getBottom();
                } else if (relInfo.above != null) {
                    Item itemRef = find(relInfo.above);
                    if (itemRef != null) {
                        IArea ref = itemRef.child;
                        bottom = ref.getTop();
                        if (bottom == null) {
                            bottom = new YTab();
                            ref.setTop(bottom);
                        }
                    }
                } else if (relInfo.alignBottom != null) {
                    Item itemRef = find(relInfo.alignEnd);
                    if (itemRef != null) {
                        IArea ref = itemRef.child;
                        bottom = ref.getBottom();
                        if (bottom == null) {
                            bottom = new YTab();
                            ref.setBottom(bottom);
                        }
                    }
                }
                if (bottom != null)
                    area.setBottom(bottom);
            }

            // to some basic settings
            if (area.getLeft() == null && area.getRight() == null)
                area.setLeft(algebraData.getLeft());
            if (area.getTop() == null && area.getBottom() == null)
                area.setTop(algebraData.getTop());
        }

        for (Item item : itemList) {
            IArea area = simplify(item.child);
            if (area.getLeft() == null)
                area.setLeft(new XTab());
            if (area.getRight() == null)
                area.setRight(new XTab());
            if (area.getTop() == null)
                area.setTop(new YTab());
            if (area.getBottom() == null)
                area.setBottom(new YTab());

            algebraData.addArea(area);
        }

        LayoutSpec layoutSpec = solve(algebraData);
        algebraData = SoundLayoutBuilder.fillWithEmptySpaces(layoutSpec);
        layoutSpec.release();

        AlgebraSpec algebraSpec = new AlgebraSpec(algebraData);
        algebraSpec.compress();
        List<Fragment> fragments = algebraSpec.getFragments();

        algebraLayoutSpec = new AlgebraLayoutSpec();
        for (Fragment fragment : fragments)
            algebraLayoutSpec.add(fragment);
        return algebraLayoutSpec;
    }
}
