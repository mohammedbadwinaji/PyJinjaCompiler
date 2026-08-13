package compiler.generated.python;


import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.Pair;


import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Queue;


public class BasePythonLexer extends PythonLexer {


    private final Deque<Integer> indents = new ArrayDeque<>();
    private final Queue<Token> pending = new ArrayDeque<>();
    private int opened = 0;


    private final int NEWLINE_T;
    private final int INDENT_T;
    private final int DEDENT_T;
    private final int LPAREN_T;
    private final int RPAREN_T;
    private final int LBRACK_T;
    private final int RBRACK_T;
    private final int LBRACE_T;
    private final int RBRACE_T;


    public BasePythonLexer(CharStream input) {
        super(input);


        NEWLINE_T = resolveTokenType("NEWLINE");
        INDENT_T  = resolveTokenType("INDENT");
        DEDENT_T  = resolveTokenType("DEDENT");
        LPAREN_T  = resolveTokenType("LPAREN");
        RPAREN_T  = resolveTokenType("RPAREN");
        LBRACK_T  = resolveTokenType("LBRACK");
        RBRACK_T  = resolveTokenType("RBRACK");
        LBRACE_T  = resolveTokenType("LBRACE");
        RBRACE_T  = resolveTokenType("RBRACE");


        indents.addFirst(0);
    }


    @Override
    public Token nextToken() {


        if (!pending.isEmpty()) {
            return pending.poll();
        }


        Token raw = super.nextToken();
        if (raw == null) return null;


        // Skip NEWLINE inside grouping
        while (raw.getType() == NEWLINE_T && opened > 0) {
            raw = super.nextToken();
            if (raw == null) return null;
        }


        // EOF: emit final NEWLINE + all DEDENTs
        if (raw.getType() == Token.EOF) {


            if (indents.getFirst() != 0) {
                pending.add(makeSynthetic(NEWLINE_T, "\n"));
            }


            while (indents.getFirst() > 0) {
                indents.removeFirst();
                pending.add(makeSynthetic(DEDENT_T, ""));
            }


            if (!pending.isEmpty()) {
                return pending.poll();
            }


            return raw;
        }


        // Track grouping
        if (raw.getType() == LPAREN_T || raw.getType() == LBRACK_T || raw.getType() == LBRACE_T) {
            opened++;
            return raw;
        }
        if (raw.getType() == RPAREN_T || raw.getType() == RBRACK_T || raw.getType() == RBRACE_T) {
            if (opened > 0) opened--;
            return raw;
        }


        // Handle NEWLINE (only when opened == 0)
        if (raw.getType() == NEWLINE_T) {


            IntStream input = _input;
            int startIndex = input.index();
            int indent = 0;
            int la = input.LA(1);


            while (la == ' ' || la == '\t') {
                indent += (la == ' ') ? 1 : 8;
                input.consume();
                la = input.LA(1);
            }


            // Blank or comment-only line → skip entirely
            if (la == '\r' || la == '\n' || la == '#' || la == IntStream.EOF) {
                input.seek(startIndex);
                return nextToken();
            }


            input.seek(startIndex);


            pending.add(makeSynthetic(raw)); // NEWLINE


            int prev = indents.getFirst();


            if (indent > prev) {
                indents.addFirst(indent);
                pending.add(makeSynthetic(INDENT_T, ""));
            } else if (indent < prev) {
                while (indents.getFirst() > indent) {
                    indents.removeFirst();
                    pending.add(makeSynthetic(DEDENT_T, ""));
                }
                if (indents.getFirst() != indent) {
                    throw new RuntimeException("Unindent does not match any outer indentation level at line " + raw.getLine());
                }
            }


            return pending.poll();
        }


        return raw;
    }


    /**
     * Create synthetic tokens with correct TokenSource + InputStream
     */
    private Token makeSynthetic(int type, String text) {
        int start = getCharIndex();
        int stop  = start + text.length() - 1;


        CommonToken t = new CommonToken(
                new Pair<>(this, _input),
                type,
                Token.DEFAULT_CHANNEL,
                start,
                stop
        );
        t.setText(text);
        return t;
    }


    private Token makeSynthetic(Token original) {
        CommonToken t = new CommonToken(
                new Pair<>(this, _input),
                original.getType(),
                original.getChannel(),
                original.getStartIndex(),
                original.getStopIndex()
        );
        t.setText(original.getText());
        return t;
    }


    private int resolveTokenType(String name) {
        try {
            java.lang.reflect.Field f = this.getClass().getSuperclass().getField(name);
            if (f.getType() == int.class) return f.getInt(null);
        } catch (Exception ignored) {}


        try {
            Class<?> parserClass = Class.forName("compiler.generated.python.PythonParser");
            java.lang.reflect.Field pf = parserClass.getField(name);
            if (pf.getType() == int.class) return pf.getInt(null);
        } catch (Exception ignored) {}


        Vocabulary vocab = this.getVocabulary();
        String[] tokenNames = this.getTokenNames();
        if (tokenNames != null) {
            for (int i = 0; i < tokenNames.length; i++) {
                String sym = vocab.getSymbolicName(i);
                if (name.equals(sym)) return i;
            }
        }


        throw new RuntimeException("Token type constant not found: " + name);
    }
}