package compiler.ast.common;

/**
 * Base implementation for expression nodes.
 */
public abstract class AbstractExpression extends AbstractAstNode implements Expression {

    protected AbstractExpression(String nodeName, int line) {
        super(nodeName, line);
    }
}
