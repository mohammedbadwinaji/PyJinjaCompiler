package compiler.ast.common;

/**
 * None literal.
 */
public final class NoneLiteral
        extends LiteralExpression {

    public NoneLiteral(int line) {
        super("NoneLiteral", line);
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitNoneLiteral(this);
    }

    @Override
    public String prettyPrint(String indent) {

        return indent +
                getNodeName() +
                " [line " +
                getLine() +
                "]";
    }

}
