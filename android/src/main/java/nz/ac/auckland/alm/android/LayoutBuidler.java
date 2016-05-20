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
    public String id = "";
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

        private Object relation;
        private Type type;

        public Relation() {
            this.type = Type.UNSET;
        }

        public Type getType() {
            return type;
        }

        public void setTo(Variable tab) {
            this.relation = tab;
            this.type = Type.TAB;
        }

        public void setTo(XTab tab) {
            this.relation = tab;
            this.type = Type.TAB;
        }

        public void setTo(YTab tab) {
            this.relation = tab;
            this.type = Type.TAB;
        }

        public void setToTabName(String tabName) {
            this.relation = tabName;
            this.type = Type.TAB_NAME;
        }

        public void setToAreaId(String areaId) {
            this.relation = areaId;
            this.type = Type.AREA_ID;
        }

        public void setToAlignAreaId(String areaId) {
            this.relation = areaId;
            this.type = Type.ALIGN_AREA_ID;
        }

        public Variable getTab() {
            assert type == Type.TAB;
            return (Variable)relation;
        }

        public String getTabName() {
            assert type == Type.TAB_NAME;
            return (String)relation;
        }

        public String getAreaId() {
            assert type == Type.AREA_ID || type == Type.ALIGN_AREA_ID;
            return (String) relation;
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
    static AreaRef getById(List<AreaRef> areas, String id) {
        for (AreaRef area : areas) {
            if (area.id.equals(id))
                return area;
        }
        return null;
    }

    static <Tab extends Variable> void resolveToTabs(AreaRef area, LayoutSpec layoutSpec, List<AreaRef> areas,
                                                     Map<String, Tab> map, IRectDirection direction) {
        AreaRef.Relation relation = direction.getRelation(area);
        switch (relation.getType()) {
            case TAB:
                break;
            case TAB_NAME:
                String name = relation.getTabName();
                Tab existingTab = map.get(name);
                if (existingTab == null) {
                    existingTab = (Tab)direction.addTab(layoutSpec);
                    map.put(name, existingTab);
                    existingTab.setName(name);
                }
                relation.setTo(existingTab);
                break;
            case ALIGN_AREA_ID:
            case AREA_ID:
                AreaRef areaRef = getById(areas, relation.getAreaId());
                if (areaRef == null)
                    throw new RuntimeException("bad layout specification: can't find area " + relation.getAreaId());
                AreaRef.Relation opRelation;
                if (relation.getType() == AreaRef.Relation.Type.AREA_ID)
                    opRelation = direction.getOppositeRelation(areaRef);
                else
                    opRelation = direction.getRelation(areaRef);
                if (opRelation.getType() == AreaRef.Relation.Type.TAB)
                    existingTab = (Tab) opRelation.getTab();
                else {
                    existingTab = (Tab)direction.addTab(layoutSpec);
                    opRelation.setTo(existingTab);
                }
                relation.setTo(existingTab);
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
            if (area.left.getType() == AreaRef.Relation.Type.UNSET)
                area.left.setTo(layoutSpec.getLeft());
            if (area.top.getType() == AreaRef.Relation.Type.UNSET)
                area.top.setTo(layoutSpec.getTop());
            if (area.right.getType() == AreaRef.Relation.Type.UNSET)
                area.right.setTo(layoutSpec.getRight());
            if (area.bottom.getType() == AreaRef.Relation.Type.UNSET)
                area.bottom.setTo(layoutSpec.getBottom());
        }
    }
}
