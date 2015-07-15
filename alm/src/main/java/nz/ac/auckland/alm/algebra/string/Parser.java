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
import nz.ac.auckland.alm.algebra.IDirection;
import nz.ac.auckland.linsolve.Variable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


class TabParser implements Parser.IState {
    final FragmentParser fragmentParser;

    public TabParser(FragmentParser fragmentParser) {
        this.fragmentParser = fragmentParser;
    }

    @Override
    public Parser.IState parse(Parser parser) {
        Fragment fragment = fragmentParser.fragment;
        Lexer.Token token = parser.next();
        if (token.type == Lexer.Token.PIPE) {
            if (fragment.direction != null && fragment.direction != Fragment.horizontalDirection) {
                parser.error("Tab direction miss match", token);
                return null;
            }
            fragment.setHorizontalDirection();
            setTab(parser, parser.namedXTabs);
        } else if (token.type == Lexer.Token.SLASH) {
            if (fragment.direction != null && fragment.direction != Fragment.verticalDirection) {
                parser.error("Tab direction miss match", token);
                return null;
            }
            fragment.setVerticalDirection();
            setTab(parser, parser.namedYTabs);
        } else {
            parser.error("Internal error: Tiling operator expected", token);
            return null;
        }
        return fragmentParser;
    }

    public <Tab extends Variable> void setTab(Parser parser, Map<String, Tab> namedTabs) {
        Fragment fragment = fragmentParser.fragment;
        IDirection direction = fragment.direction;
        List<IArea> items = fragment.getItems();
        IArea lastArea = items.get(items.size() - 1);
        Tab tab;
        Lexer.Token token = parser.peek();
        if (token.type == Lexer.Token.TAB_NAME) {
            // get the token
            token = parser.next();
            tab = namedTabs.get(token.value);
            if (tab == null) {
                tab = (Tab) direction.createTab();
                namedTabs.put(token.value, tab);
            }
        } else
            tab = (Tab)direction.createTab();

        direction.setTab(lastArea, tab);
    }
}

class CloseFragmentByBracketParser implements Parser.IState {
    final FragmentParser fragmentParser;

    public CloseFragmentByBracketParser(FragmentParser parent) {
        this.fragmentParser = parent;
    }

    @Override
    public Parser.IState parse(Parser parser) {
        Lexer.Token token = parser.next();
        if (token.type != Lexer.Token.TERM_END) {
            parser.error("Internal error: closing bracket expected", token);
            return null;
        }
        if (fragmentParser.parent == null) {
            parser.error("Unexpected closing bracket", token);
            return null;
        }
        return fragmentParser.parent.addItem(parser, fragmentParser.fragment);
    }
}

class CloseFragmentByStarParser implements Parser.IState {
    final FragmentParser fragmentParser;

    public CloseFragmentByStarParser(FragmentParser parent) {
        this.fragmentParser = parent;
    }

    @Override
    public Parser.IState parse(Parser parser) {
        Lexer.Token token = parser.next();
        if (token.type != Lexer.Token.STAR) {
            parser.error("Internal error: star expected", token);
            return null;
        }

        if (fragmentParser.parent != null) {
            parser.error("Closing bracket expected", token);
            return null;
        }

        parser.addTerm(fragmentParser.fragment);
        return new FragmentParser(null);
    }
}

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
        if (token.type == Lexer.Token.TERM_START)
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
            IDirection direction = fragment.direction;
            List<IArea> items = fragment.getItems();
            IArea lastArea = items.get(items.size() - 1);
            Variable existingTab = direction.getTab(lastArea);
            assert (existingTab != null);
            direction.setOppositeTab(item, existingTab);
        }

        fragment.add(item);

        Lexer.Token peek = parser.peek();
        if (peek.type == Lexer.Token.TERM_END)
            return new CloseFragmentByBracketParser(this);
        if (peek.type == Lexer.Token.STAR)
            return new CloseFragmentByStarParser(this);
        if (peek.type == Lexer.Token.SLASH || peek.type == Lexer.Token.PIPE)
            return new TabParser(this);
        // we handle the EOF
        if (peek.type == Lexer.Token.EOF)
            return this;

        parser.error("Unexpected token", peek);
        return null;
    }
}

public class Parser implements Lexer.IListener {
    final Map<String, IArea> areaMap = new HashMap<String, IArea>();
    final Map<String, XTab> namedXTabs = new HashMap<String, XTab>();
    final Map<String, YTab> namedYTabs = new HashMap<String, YTab>();

    interface IState {
        IState parse(Parser parser);
    }

    public interface IListener {
        void onError(String error, Lexer.Token token);
    }

    IState state = new FragmentParser(null);
    final List<IArea> terms = new ArrayList<IArea>();
    final IListener listener;

    final static int MIN_QUEUE_SIZE = 2;
    List<Lexer.Token> tokenQueue = new ArrayList<Lexer.Token>(MIN_QUEUE_SIZE);

    public Parser(IListener listener) {
        this.listener = listener;
    }

    @Override
    public void onNewToken(Lexer.Token token) {
        if (token.type == Lexer.Token.ERROR) {
            error(token.value, token);
            state = null;
        }
        tokenQueue.add(token);

        while (state != null && (tokenQueue.size() >= MIN_QUEUE_SIZE || token.type == Lexer.Token.EOF))
            state = state.parse(this);
    }

    public Lexer.Token next() {
        if (peek().type == Lexer.Token.EOF)
            return peek();
        return tokenQueue.remove(0);
    }

    public Lexer.Token peek() {
        return tokenQueue.get(0);
    }

    public void error(String error, Lexer.Token token) {
        if (listener != null)
            listener.onError(error, token);
    }

    public IArea getArea(String name) {
        IArea area = areaMap.get(name);
        if (area == null) {
            area = new Area();
            areaMap.put(name, area);
        }
        return area;
    }

    public void addTerm(Fragment fragment) {
        if (fragment.getItems().size() == 0)
            return;
        if (fragment.getItems().size() == 1)
            terms.add((IArea) fragment.getItems().get(0));
        else
            terms.add(fragment);
    }

    public List<IArea> getTerms() {
        return terms;
    }
}
