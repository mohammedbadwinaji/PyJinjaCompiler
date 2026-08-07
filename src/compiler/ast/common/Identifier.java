package compiler.ast.common;

import java.util.Objects;

/**
 * Identifier expression.
 */
public final class Identifier extends AbstractExpression {

    private final String name;

    public Identifier(int line, String name) {

        super("Identifier", line);

        this.name = Objects.requireNonNull(name);
    }

    public String getName() {
        return name;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitIdentifier(this);
    }

    @Override
    public String prettyPrint(String indent) {

        return indent +
                getNodeName() +
                " (" +
                name +
                ") [line " +
                getLine() +
                "]";
    }

}
