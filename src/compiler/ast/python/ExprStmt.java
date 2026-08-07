package compiler.ast.python;

import compiler.ast.common.AstVisitor;
import compiler.ast.common.Expression;

import java.util.Objects;

/**
 * Expression statement.
 *
 * Example:
 *
 * print(x)
 */
public final class ExprStmt extends AbstractStatement {

    private final Expression expression;

    public ExprStmt(
            int line,
            Expression expression) {

        super("ExprStmt", line);

        this.expression =
                Objects.requireNonNull(expression);
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitExprStmt(this);
    }

    @Override
    public String prettyPrint(String indent) {

        StringBuilder sb = new StringBuilder();

        sb.append(indent)
                .append("ExprStmt [line ")
                .append(getLine())
                .append("]");

        sb.append("\n")
                .append(expression.prettyPrint(indent + "  "));

        return sb.toString();
    }
}