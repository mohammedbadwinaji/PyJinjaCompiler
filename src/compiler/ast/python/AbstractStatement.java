package compiler.ast.python;

/**
 * Base class for all statement nodes.
 */
public abstract class AbstractStatement
        extends AbstractAstNode
        implements Statement {

    protected AbstractStatement(String nodeName, int line) {
        super(nodeName, line);
    }

}