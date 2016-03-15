/*
 * Copyright 2015.
 * Distributed under the terms of the LGPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm.android;

import nz.ac.auckland.alm.*;
import nz.ac.auckland.linsolve.Variable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


class AreaRef {
    public int id = -1;
    public HorizontalAlignment horizontalAlignment = HorizontalAlignment.CENTER;
    public VerticalAlignment verticalAlignment = VerticalAlignment.CENTER;
    public Area.Size explicitMinSize = new Area.Size(Area.Size.UNDEFINED, Area.Size.UNDEFINED);
    public Area.Size explicitPrefSize = new Area.Size(Area.Size.UNDEFINED, Area.Size.UNDEFINED);

    public Relation left = new Relation();
    public Relation top = new Relation();
    public Relation right = new Relation();
    public Relation bottom = new Relation();

    static class Relation {
        enum Type {
            TAB,
            TAB_NAME,
            AREA_ID,
            ALIGN_AREA_ID,
            UNSET
        }

        public Object relation;
        public Type type;

        public Relation() {
            this.type = Type.UNSET;
        }

        public void setTo(XTab tab) {
            this.relation = tab;
            this.type = Type.TAB;
        }

        public void setTo(YTab tab) {
            this.relation = tab;
            this.type = Type.TAB;
        }

        public void setTo(String tabName) {
            this.relation = tabName;
            this.type = Type.TAB_NAME;
        }

        public void setTo(Integer areaId) {
            this.relation = areaId;
            this.type = Type.AREA_ID;
        }

        public void setAlignTo(Integer areaId) {
            this.relation = areaId;
            this.type = Type.ALIGN_AREA_ID;
        }
    }
}

interface IRectDirection {
    Variable addTab(LayoutSpec layoutSpec);

    AreaRef.Relation getRelation(AreaRef areaRef);
    AreaRef.Relation getOppositeRelation(AreaRef areaRef);
}

class LeftDirection implements IRectDirection {
    @Override
    public Variable addTab(LayoutSpec layoutSpec) {
        return new XTab();
    }

    @Override
    public AreaRef.Relation getRelation(AreaRef areaRef) {
        return areaRef.left;
    }

    @Override
    public AreaRef.Relation getOppositeRelation(AreaRef areaRef) {
        return areaRef.right;
    }
}

class TopDirection implements IRectDirection {
    @Override
    public Variable addTab(LayoutSpec layoutSpec) {
        return new YTab();
    }

    @Override
    public AreaRef.Relation getRelation(AreaRef areaRef) {
        return areaRef.top;
    }

    @Override
    public AreaRef.Relation getOppositeRelation(AreaRef areaRef) {
        return areaRef.bottom;
    }
}

class RightDirection implements IRectDirection {
    @Override
    public Variable addTab(LayoutSpec layoutSpec) {
        return new XTab();
    }

    @Override
    public AreaRef.Relation getRelation(AreaRef areaRef) {
        return areaRef.right;
    }

    @Override
    public AreaRef.Relation getOppositeRelation(AreaRef areaRef) {
        return areaRef.left;
    }
}

class BottomDirection implements IRectDirection {
    @Override
    public Variable addTab(LayoutSpec layoutSpec) {
        return new YTab();
    }

    @Override
    public AreaRef.Relation getRelation(AreaRef areaRef) {
        return areaRef.bottom;
    }

    @Override
    public AreaRef.Relation getOppositeRelation(AreaRef areaRef) {
        return areaRef.top;
    }
}

class LayoutBuilder {
    static AreaRef getById(List<AreaRef> areas, int id) {
        for (AreaRef area : areas) {
            if (area.id == id)
                return area;
        }
        return null;
    }

    static <Tab extends Variable> void resolveToTabs(AreaRef area, LayoutSpec layoutSpec, List<AreaRef> areas,
                                                     Map<String, Tab> map, IRectDirection direction) {
        AreaRef.Relation relation = direction.getRelation(area);
        switch (relation.type) {
            case TAB:
                break;
            case TAB_NAME:
                String name = (String) relation.relation;
                Variable existingTab = map.get(name);
                if (existingTab == null) {
                    existingTab = direction.addTab(layoutSpec);
                    map.put(name, (Tab) existingTab);
                    existingTab.setName(name);
                }
                relation.relation = existingTab;
                relation.type = AreaRef.Relation.Type.TAB;
                break;
            case ALIGN_AREA_ID:
            case AREA_ID:
                AreaRef areaRef = getById(areas, (Integer)relation.relation);
                if (areaRef == null)
                    throw new RuntimeException("bad layout specification: can't find area " + relation.relation);
                AreaRef.Relation opRelation;
                if (relation.type == AreaRef.Relation.Type.AREA_ID)
                    opRelation = direction.getOppositeRelation(areaRef);
                else
                    opRelation = direction.getRelation(areaRef);
                if (opRelation.type == AreaRef.Relation.Type.AREA_ID
                        && (Integer)opRelation.relation != area.id)
                    throw new RuntimeException("bad layout specification: conflicting relations");
                else if (opRelation.type == AreaRef.Relation.Type.TAB)
                    existingTab = (Variable) opRelation.relation;
                else {
                    existingTab = direction.addTab(layoutSpec);
                    opRelation.relation = existingTab;
                    opRelation.type = AreaRef.Relation.Type.TAB;
                }
                relation.relation = existingTab;
                relation.type = AreaRef.Relation.Type.TAB;
                break;
        }
    }

    static public void resolveToTabs(List<AreaRef> areas, LayoutSpec layoutSpec) {
        Map<String, XTab> namedXTabs = new HashMap<String, XTab>();
        Map<String, YTab> namedYTabs = new HashMap<String, YTab>();

        for (AreaRef area : areas) {
            resolveToTabs(area, layoutSpec, areas, namedXTabs, new LeftDirection());
            resolveToTabs(area, layoutSpec, areas, namedYTabs, new TopDirection());
            resolveToTabs(area, layoutSpec, areas, namedXTabs, new RightDirection());
            resolveToTabs(area, layoutSpec, areas, namedYTabs, new BottomDirection());
        }

        // set outer tabs
        for (AreaRef area : areas) {
            if (area.left.type == AreaRef.Relation.Type.UNSET)
                area.left.setTo(layoutSpec.getLeft());
            if (area.top.type == AreaRef.Relation.Type.UNSET)
                area.top.setTo(layoutSpec.getTop());
            if (area.right.type == AreaRef.Relation.Type.UNSET)
                area.right.setTo(layoutSpec.getRight());
            if (area.bottom.type == AreaRef.Relation.Type.UNSET)
                area.bottom.setTo(layoutSpec.getBottom());
        }
    }
}
