package compiler.ast.common;

import java.util.Objects;

/**
 * Binary expression.
 */
public final class BinaryExpr extends AbstractExpression {

    private final Expression left;

    private final BinaryOperator operator;

    private final Expression right;

    public BinaryExpr(
            int line,
            Expression left,
            BinaryOperator operator,
            Expression right) {

        super("BinaryExpr", line);

        this.left = Objects.requireNonNull(left);
        this.operator = Objects.requireNonNull(operator);
        this.right = Objects.requireNonNull(right);
    }

    public Expression getLeft() {
        return left;
    }

    public BinaryOperator getOperator() {
        return operator;
    }

    public Expression getRight() {
        return right;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitBinaryExpr(this);
    }

    @Override
    public String prettyPrint(String indent) {

        String child = indent + "  ";

        return indent + "BinaryExpr (" + operator + ") [line " + getLine() + "]\n"
                + left.prettyPrint(child) + "\n"
                + right.prettyPrint(child);
    }

}
