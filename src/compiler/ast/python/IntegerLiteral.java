package compiler.ast.python;

/**
 * Integer literal.
 */
public final class IntegerLiteral
        extends LiteralExpression {

    private final long value;

    public IntegerLiteral(int line, long value) {

        super("IntegerLiteral", line);

        this.value = value;
    }

    public long getValue() {
        return value;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitIntegerLiteral(this);
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