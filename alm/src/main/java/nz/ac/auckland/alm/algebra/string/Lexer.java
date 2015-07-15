/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm.algebra.string;


class MainLexer implements Lexer.IState {
    @Override
    public Lexer.IState lex(Lexer lexer) {
        while (true) {
            char rune = lexer.next();
            if (rune == 0) {
                lexer.emit(Lexer.Token.EOF);
                return null;
            }
            switch (rune) {
                case ' ':
                    lexer.ignore();
                    break;
                case '(':
                    lexer.emit(Lexer.Token.TERM_START);
                    break;
                case ')':
                    lexer.emit(Lexer.Token.TERM_END);
                    break;
                case '*':
                    lexer.emit(Lexer.Token.STAR);
                    break;
                case '|':
                    lexer.emit(Lexer.Token.PIPE);
                    return new TabNameStartLexer();
                case '/':
                    lexer.emit(Lexer.Token.SLASH);
                    return new TabNameStartLexer();
                default:
                    // must be an atom
                    lexer.backup();
                    return new AtomParser();
            }
        }
    }


}

class AtomParser implements Lexer.IState {
    Lexer.IAcceptFunction acceptFunction = new Lexer.IAcceptFunction() {
        @Override
        public boolean accept(char character) {
            return Character.isLetterOrDigit(character);
        }
    };

    @Override
    public Lexer.IState lex(Lexer lexer) {
        if (lexer.accept(acceptFunction)) {
            lexer.acceptRun(acceptFunction);
            lexer.emit(Lexer.Token.ATOM);
            return new MainLexer();
        } else {
            lexer.error("Atom expected");
            return null;
        }
    }
}

class TabNameStartLexer implements Lexer.IState {
    @Override
    public Lexer.IState lex(Lexer lexer) {
        lexer.ignoreAll(' ');
        while (true) {
            if (lexer.next() == '{') {
                lexer.ignore();
                return new TabNameLexer();
            } else {
                lexer.backup();
                return new MainLexer();
            }
        }
    }
}

class TabNameLexer implements Lexer.IState {
    Lexer.IAcceptFunction acceptFunction = new Lexer.IAcceptFunction() {
        @Override
        public boolean accept(char character) {
            return Character.isLetterOrDigit(character);
        }
    };

    @Override
    public Lexer.IState lex(Lexer lexer) {
        lexer.ignoreAll(' ');

        if (lexer.accept(acceptFunction)) {
            lexer.acceptRun(acceptFunction);
            lexer.emit(Lexer.Token.TAB_NAME);
            return new TabNameEndLexer();
        } else {
            lexer.error("Tab name expected");
            return null;
        }
    }
}

class TabNameEndLexer implements Lexer.IState {
    @Override
    public Lexer.IState lex(Lexer lexer) {
        lexer.ignoreAll(' ');
        while (true) {
            if (lexer.next() == '}') {
                lexer.ignore();
                return new MainLexer();
            } else {
                lexer.error("Tab finish } expected");
                return null;
            }
        }
    }

}

public class Lexer {
    static public class Token {
        final static public int ERROR = -2;
        final static public int EOF = -1;
        final static public int TERM_START = 0;
        final static public int TERM_END = 1;
        final static public int ATOM = 2;
        final static public int SLASH = 3;
        final static public int PIPE = 4;
        final static public int TAB_NAME = 5;
        final static public int STAR = 6;

        final public int type;
        final public String value;
        final public int position;

        public Token(int type, String value, int position) {
            this.type = type;
            this.value = value;
            this.position = position;
        }

        @Override
        public String toString() {
            if (type == EOF)
                return "EOF";
            if (type == ERROR)
                return "error at " + position + ": " + value;
            return value;
        }
    }

    public interface IState {
        IState lex(Lexer lexer);
    }

    public interface IListener {
        void onNewToken(Token token);
    }

    final String input;
    final IListener listener;
    int itemStart = 0;
    int itemPosition = 0;
    int runeWidth = 1;

    public Lexer(String input, IListener listener) {
        this.input = input;
        this.listener = listener;
    }

    public void run() {
        for (IState state = new MainLexer(); state != null;)
            state = state.lex(this);
    }

    public void emit(int type) {
        Token token = new Token(type, input.substring(itemStart, itemPosition), itemStart);
        emit(token);
    }

    public void error(String error) {
        Token token = new Token(Token.ERROR, error, itemStart);
        emit(token);
    }

    private void emit(Token token) {
        itemStart = itemPosition;
        listener.onNewToken(token);
    }

    public char next() {
        if (itemPosition >= input.length()) {
            runeWidth = 0;
            return 0;
        }
        char character = input.charAt(itemPosition);
        runeWidth = 1;
        itemPosition++;
        return character;
    }

    public void ignore() {
        itemStart = itemPosition;
    }

    public void ignoreAll(char character) {
        while (next() == character) ignore();
        backup();
    }

    public void backup() {
        itemPosition -= runeWidth;
    }

    public char peek() {
        char character = next();
        backup();
        return character;
    }

    public interface IAcceptFunction {
        boolean accept(char character);
    }

    public boolean accept(IAcceptFunction acceptFunction) {
        if (acceptFunction.accept(next()))
            return true;
        backup();
        return false;
    }

    public void acceptRun(IAcceptFunction acceptFunction) {
        while (acceptFunction.accept(next())) continue;
        backup();
    }
}
