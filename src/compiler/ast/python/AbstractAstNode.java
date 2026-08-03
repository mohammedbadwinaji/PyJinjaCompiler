package compiler.ast.python;

import java.util.Objects;

/**
 * Shared implementation for all AST nodes.
 */
public abstract class AbstractAstNode implements AstNode {

    private final String nodeName;
    private final int line;

    protected AbstractAstNode(String nodeName, int line) {

        this.nodeName = Objects.requireNonNull(nodeName);
        this.line = line;
    }

    @Override
    public final String getNodeName() {
        return nodeName;
    }

    @Override
    public final int getLine() {
        return line;
    }

    @Override
    public String prettyPrint(String indent) {

        return indent +
                nodeName +
                " (line " +
                line +
                ")";
    }

    @Override
    public String toString() {
        return prettyPrint("");
    }

}