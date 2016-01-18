/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm.algebra.string;

import nz.ac.auckland.alm.Area;
import nz.ac.auckland.alm.IArea;
import nz.ac.auckland.alm.XTab;
import nz.ac.auckland.alm.YTab;
import nz.ac.auckland.alm.algebra.Fragment;
import nz.ac.auckland.alm.algebra.IDirection;
import nz.ac.auckland.linsolve.Variable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


class FragmentParser implements Parser.IState {
    final Fragment fragment = new Fragment();
    final FragmentParser parent;

    public FragmentParser(FragmentParser parent) {
        this.parent = parent;
    }

    @Override
    public Parser.IState parse(Parser parser) {
        Lexer.Token token = parser.next();
        if (token.type == Lexer.Token.EOF) {
            if (parent != null) {
                parser.error("Closing bracket expected", token);
                return null;
            }
            parser.addTerm(fragment);
            return null;
        }
        if (token.type == Lexer.Token.ATOM) {
            IArea area = parser.getArea(token.value);
            return addItem(parser, area);
        }
        if (token.type == Lexer.Token.OPEN_BRACKET)
            return new FragmentParser(this);

        parser.error("Unexpected token", token);
        return null;
    }

    protected Parser.IState addItem(Parser parser, IArea item) {
        // empty terms with a single item
        if (item instanceof Fragment) {
            Fragment fragment = (Fragment) item;
            if (fragment.getItems().size() == 1)
                item = (IArea) fragment.getItems().get(0);
        }

        // set tab
        if (fragment.getItems().size() > 0) {
            IDirection direction = fragment.getDirection();
            List<IArea> items = fragment.getItems();
            IArea lastArea = items.get(items.size() - 1);
            Variable existingTab = direction.getTab(lastArea);
            assert (existingTab != null);
            direction.setOppositeTab(item, existingTab);
        }

        fragment.add(item, false);

        Lexer.Token token = parser.next();
        if (token.type == Lexer.Token.CLOSE_BRACKET) {
            if (parent == null) {
                parser.error("Unexpected closing bracket", token);
                return null;
            }
            return parent.addItem(parser, fragment);
        }
        if (token.type == Lexer.Token.STAR) {
            if (parent != null) {
                parser.error("Closing bracket expected", token);
                return null;
            }

            parser.addTerm(fragment);
            return new FragmentParser(null);
        }
        if (token.type == Lexer.Token.SLASH) {
            if (fragment.getDirection() != null && fragment.getDirection() != Fragment.verticalDirection) {
                parser.error("Tab direction miss match", token);
                return null;
            }
            fragment.setVerticalDirection();
            setTab(parser, parser.namedYTabs);
            return this;
        }
        if (token.type == Lexer.Token.PIPE) {
            if (fragment.getDirection() != null && fragment.getDirection() != Fragment.horizontalDirection) {
                parser.error("Tab direction miss match", token);
                return null;
            }
            fragment.setHorizontalDirection();
            setTab(parser, parser.namedXTabs);
            return this;
        }
        // we handle the EOF
        if (token.type == Lexer.Token.EOF)
            return this;

        parser.error("Unexpected token", token);
        return null;
    }

    public <Tab extends Variable> void setTab(Parser parser, Map<String, Tab> namedTabs) {
        IDirection direction = fragment.getDirection();
        List<IArea> items = fragment.getItems();
        IArea lastArea = items.get(items.size() - 1);
        Tab existingTab = (Tab)direction.getTab(lastArea);
        Tab tab;
        Lexer.Token token = parser.peek();
        if (token.type == Lexer.Token.TAB_NAME) {
            // get the token
            token = parser.next();
            tab = namedTabs.get(token.value);
            if (existingTab != null && existingTab != tab)
                parser.error("Area is already assigned to a different tabstop", token);

            if (tab == null) {
                tab = (Tab) direction.createTab();
                namedTabs.put(token.value, tab);
            }
        } else if (existingTab != null)
            tab = existingTab;
        else
            tab = (Tab)direction.createTab();

        direction.setTab(lastArea, tab);
    }
}

public class Parser {
    final Map<String, XTab> namedXTabs = new HashMap<String, XTab>();
    final Map<String, YTab> namedYTabs = new HashMap<String, YTab>();

    interface IState {
        IState parse(Parser parser);
    }

    public interface IAreaFactory {
        IArea getArea(String areaId);
    }

    public interface IListener {
        void onError(String error, Lexer.Token token);
    }

    static public IAreaFactory getDefaultAreaFactory() {
        return new IAreaFactory() {
            final Map<String, IArea> areaMap = new HashMap<String, IArea>();

            @Override
            public IArea getArea(String areaId) {
                IArea area = areaMap.get(areaId);
                if (area == null) {
                    area = new Area();
                    areaMap.put(areaId, area);
                    area.setId(areaId);
                }
                return area;
            }
        };
    }

    IState state = new FragmentParser(null);
    final List<Fragment> terms = new ArrayList<Fragment>();
    final Lexer.TokenStream tokenStream;
    final IAreaFactory areaFactory;
    final IListener listener;
    boolean hasError = false;

    public Parser(Lexer.TokenStream tokenStream, IListener listener) {
        this(tokenStream, null, listener);
    }

    public Parser(Lexer.TokenStream tokenStream, IAreaFactory areaFactory, IListener listener) {
        this.tokenStream = tokenStream;
        if (areaFactory == null)
            this.areaFactory = getDefaultAreaFactory();
        else
            this.areaFactory = areaFactory;
        this.listener = listener;
    }

    public void run() {
        while (state != null)
            state = state.parse(this);
    }

    public Lexer.Token next() {
        return tokenStream.next();
    }

    public Lexer.Token peek() {
        return tokenStream.peek();
    }

    public void error(String error, Lexer.Token token) {
        hasError = true;
        if (listener != null)
            listener.onError(error, token);
    }

    public boolean hasError() {
        return hasError;
    }

    public IArea getArea(String areaId) {
        return areaFactory.getArea(areaId);
    }

    public void addTerm(Fragment fragment) {
        if (fragment.getItems().size() == 0)
            return;
        terms.add(fragment);
    }

    public List<Fragment> getTerms() {
        return terms;
    }
}
