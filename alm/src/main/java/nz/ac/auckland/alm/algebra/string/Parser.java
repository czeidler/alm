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


class TermClosedParser implements Parser.IState {
    final Term closedTerm;

    public TermClosedParser(Term closedTerm) {
        this.closedTerm = closedTerm;
    }

    @Override
    public Parser.IState parse(Parser parser, Lexer.Token token) {
        if (token.type == Lexer.Token.STAR) {
            parser.addTerm(closedTerm);
            return new RootTermParser();
        }
        // if its not a star we are not finished yet
        Term term = closedTerm;
        if (term.getItems().size() > 1) {
            term = new Term();
            term.add(closedTerm);
        }
        TermContainerParser containerParser = new TermContainerParser(term, false, null);
        return containerParser.parse(parser, token);
    }
}

class TermContainerParser implements Parser.IState {
    final boolean inBrackets;
    final Term term;
    final TermContainerParser parent;

    public TermContainerParser(IArea atom, boolean inBrackets, TermContainerParser parent) {
        this.inBrackets = inBrackets;
        this.term = new Term();
        this.term.add(atom);
        this.parent = parent;
    }

    public TermContainerParser(Term term, boolean inBrackets, TermContainerParser parent) {
        this.inBrackets = inBrackets;
        this.term = term;
        this.parent = parent;
    }

    @Override
    public Parser.IState parse(Parser parser, Lexer.Token token) {
        if (token.type == Lexer.Token.PIPE) {
            if (term.direction != null && term.direction != Term.horizontalDirection) {
                parser.error("Tab direction miss match", token);
                return null;
            }
            term.setHorizontalDirection();
            return new TabNameParser<XTab>(this, parser.namedXTabs);
        } else if (token.type == Lexer.Token.SLASH) {
            if (term.direction != null && term.direction != Term.verticalDirection) {
                parser.error("Tab direction miss match", token);
                return null;
            }
            term.setVerticalDirection();
            return new TabNameParser<YTab>(this, parser.namedYTabs);
        }

        if (token.type == Lexer.Token.TERM_END) {
            if (!inBrackets) {
                parser.error("Unexpected bracket", token);
                return null;
            }
            if (parent != null) {
                parent.term.add(term);
                return parent;
            } else {
                return new TermClosedParser(term);
            }
        } else if (token.type == Lexer.Token.STAR || token.type == Lexer.Token.EOF) {
            if (inBrackets || parent != null) {
                parser.error("Closing bracket expected", token);
                return null;
            }
            parser.addTerm(term);
            return new RootTermParser();
        }

        // error
        if (inBrackets)
            parser.error("Tab or closing bracket expected", token);
        else
            parser.error("Tab expected", token);
        return null;
    }
}

class TabNameParser<Tab extends Variable> implements Parser.IState {
    final TermContainerParser container;
    final Map<String, Tab> namedTabs;

    TabNameParser(TermContainerParser container, Map<String, Tab> namedTabs) {
        this.container = container;
        this.namedTabs = namedTabs;
    }

    @Override
    public Parser.IState parse(Parser parser, Lexer.Token token) {
        Term term = container.term;
        IDirection direction = term.direction;
        List<IArea> items = term.getItems();
        IArea lastArea = items.get(items.size() - 1);
        Tab existingTab = (Tab) direction.getTab(lastArea);
        if (token.type == Lexer.Token.TAB_NAME) {
            Tab tab = namedTabs.get(token.value);
            if (tab == null) {
                tab = (Tab) direction.createTab();
                namedTabs.put(token.value, tab);
            }
            if (existingTab != null && existingTab != tab) {
                parser.error("tab name mismatch", token);
                return null;
            }
            direction.setTab(lastArea, tab);
            return this;
        } else {
            ContainerItemParser containerItemParser = new ContainerItemParser(container);
            return containerItemParser.parse(parser, token);
        }
    }
}

class ContainerItemParser<Tab extends Variable> implements Parser.IState {
    final TermContainerParser container;

    ContainerItemParser(TermContainerParser container) {
        this.container = container;
    }

    @Override
    public Parser.IState parse(Parser parser, Lexer.Token token) {
        Term term = container.term;
        IDirection direction = term.direction;
        List<IArea> items = term.getItems();
        IArea lastArea = items.get(items.size() - 1);
        Tab existingTab = (Tab) direction.getTab(lastArea);

        if (token.type == Lexer.Token.ATOM) {
            if (existingTab == null) {
                Tab tab = (Tab) direction.createTab();
                direction.setTab(lastArea, tab);
            }
            IArea area = parser.getArea(token.value);
            term.add(area);
            return container;
        }
        if (token.type == Lexer.Token.TERM_START)
            return new OpenBracketTermParser(container);

        parser.error("atom expected", token);
        return null;
    }
}

class OpenBracketTermParser implements Parser.IState {
    final TermContainerParser parent;

    public OpenBracketTermParser(TermContainerParser parent) {
        this.parent = parent;
    }

    @Override
    public Parser.IState parse(Parser parser, Lexer.Token token) {
        if (token.type == Lexer.Token.ATOM)
            return new TermContainerParser(parser.getArea(token.value), true, parent);
        else if (token.type == Lexer.Token.TERM_START) {
            TermContainerParser current = new TermContainerParser(new Term(), true, parent);
            return new OpenBracketTermParser(current);
        }

        parser.error("No term found", token);
        return null;
    }
}

class RootTermParser implements Parser.IState {
    @Override
    public Parser.IState parse(Parser parser, Lexer.Token token) {
        if (token.type == Lexer.Token.EOF)
            return null;

        if (token.type == Lexer.Token.TERM_START)
            return new OpenBracketTermParser(null);

        if (token.type == Lexer.Token.ATOM)
            return new TermContainerParser(parser.getArea(token.value), false, null);

        parser.error("No term found", token);
        return null;
    }
}

public class Parser implements Lexer.IListener {
    final Map<String, IArea> areaMap = new HashMap<String, IArea>();
    final Map<String, XTab> namedXTabs = new HashMap<String, XTab>();
    final Map<String, YTab> namedYTabs = new HashMap<String, YTab>();

    interface IState {
        IState parse(Parser parser, Lexer.Token token);
    }

    IState state = new RootTermParser();
    final List<IArea> terms = new ArrayList<IArea>();
    String error = "";
    Lexer.Token errorToken = null;

    @Override
    public void onNewToken(Lexer.Token token) {
        if (token.type == Lexer.Token.ERROR) {
            error(token.value, token);
            state = null;
        }

        if (state != null)
            state = state.parse(this, token);
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

    public void addTerm(IArea term) {
        terms.add(term);
    }

    public List<IArea> getTerms() {
        return terms;
    }
}
