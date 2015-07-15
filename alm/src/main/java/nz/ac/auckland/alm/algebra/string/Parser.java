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
    final TermParser termParser;

    public TabParser(TermParser termParser) {
        this.termParser = termParser;
    }

    @Override
    public Parser.IState parse(Parser parser) {
        Fragment fragment = termParser.fragment;
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
        return termParser;
    }

    public <Tab extends Variable> void setTab(Parser parser, Map<String, Tab> namedTabs) {
        Fragment fragment = termParser.fragment;
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

class CloseTermByBracketParser implements Parser.IState {
    final TermParser termParser;

    public CloseTermByBracketParser(TermParser parent) {
        this.termParser = parent;
    }

    @Override
    public Parser.IState parse(Parser parser) {
        Lexer.Token token = parser.next();
        if (token.type != Lexer.Token.TERM_END) {
            parser.error("Internal error: closing bracket expected", token);
            return null;
        }
        if (termParser.parent == null) {
            parser.error("Unexpected closing bracket", token);
            return null;
        }
        return termParser.parent.addItem(parser, termParser.fragment);
    }
}

class CloseTermByStarParser implements Parser.IState {
    final TermParser termParser;

    public CloseTermByStarParser(TermParser parent) {
        this.termParser = parent;
    }

    @Override
    public Parser.IState parse(Parser parser) {
        Lexer.Token token = parser.next();
        if (token.type != Lexer.Token.STAR) {
            parser.error("Internal error: star expected", token);
            return null;
        }

        if (termParser.parent != null) {
            parser.error("Closing bracket expected", token);
            return null;
        }

        parser.addTerm(termParser.fragment);
        return new TermParser(null);
    }
}

class TermParser implements Parser.IState {
    final Fragment fragment = new Fragment();
    final TermParser parent;

    public TermParser(TermParser parent) {
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
            return new TermParser(this);

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
            return new CloseTermByBracketParser(this);
        if (peek.type == Lexer.Token.STAR)
            return new CloseTermByStarParser(this);
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

    IState state = new TermParser(null);
    final List<IArea> terms = new ArrayList<IArea>();
    String error = "";
    Lexer.Token errorToken = null;

    final static int MIN_QUEUE_SIZE = 2;
    List<Lexer.Token> tokenQueue = new ArrayList<Lexer.Token>(MIN_QUEUE_SIZE);

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
        this.error = error;
        this.errorToken = token;
    }

    public String getError() {
        return error;
    }

    public Lexer.Token getErrorToken() {
        return errorToken;
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
