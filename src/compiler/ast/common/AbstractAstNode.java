package compiler.ast.common;

/**
 * Base implementation for AST nodes.
 */
public abstract class AbstractAstNode implements AstNode {

    private final String nodeName;
    private final int line;

    protected AbstractAstNode(String nodeName, int line) {
        this.nodeName = nodeName;
        this.line = line;
    }

    @Override
    public String getNodeName() {
        return nodeName;
    }

    @Override
    public int getLine() {
        return line;
    }

    @Override
    public String prettyPrint(String indent) {
        return indent + getNodeName() + " [line " + getLine() + "]";
    }
}
