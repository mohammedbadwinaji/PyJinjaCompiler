package compiler.ast.python;

/**
 * Base class for all expression nodes.
 */
public abstract class AbstractExpression
        extends AbstractAstNode
        implements Expression {

    protected AbstractExpression(String nodeName, int line) {
        super(nodeName, line);
    }

}