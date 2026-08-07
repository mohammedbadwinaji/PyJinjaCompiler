package compiler.ast.common;

/**
 * Base class for all literal expressions.
 *
 * Examples:
 * - Integer
 * - Float
 * - String
 * - Boolean
 * - None
 * - List
 * - Dictionary
 */
public abstract class LiteralExpression
        extends AbstractExpression {

    protected LiteralExpression(
            String nodeName,
            int line) {

        super(nodeName, line);
    }

}
