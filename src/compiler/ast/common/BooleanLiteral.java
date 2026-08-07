package compiler.ast.common;

/**
 * Boolean literal.
 */
public final class BooleanLiteral
        extends LiteralExpression {

    private final boolean value;

    public BooleanLiteral(int line, boolean value) {

        super("BooleanLiteral", line);

        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitBooleanLiteral(this);
    }

    @Override
    public String prettyPrint(String indent) {

        return indent +
                getNodeName() +
                " (" +
                value +
                ") [line " +
                getLine() +
                "]";
    }

}
