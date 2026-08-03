package compiler.ast.python;

/**
 * Floating-point literal.
 */
public final class FloatLiteral
        extends LiteralExpression {

    private final double value;

    public FloatLiteral(int line, double value) {

        super("FloatLiteral", line);

        this.value = value;
    }

    public double getValue() {
        return value;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitFloatLiteral(this);
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