package compiler.ast.common;

import java.util.Objects;

/**
 * String literal.
 */
public final class StringLiteral
        extends LiteralExpression {

    private final String value;

    public StringLiteral(int line, String value) {

        super("StringLiteral", line);

        this.value = Objects.requireNonNull(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitStringLiteral(this);
    }

    @Override
    public String prettyPrint(String indent) {

        return indent +
                getNodeName() +
                " (\"" +
                value +
                "\") [line " +
                getLine() +
                "]";
    }

}
