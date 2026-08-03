package compiler.ast.python;

import java.util.Objects;

public final class PositionalArgument extends AbstractAstNode implements Argument {

    private final Expression value;

    public PositionalArgument(int line, Expression value) {

        super("PositionalArgument", line);

        this.value = Objects.requireNonNull(value);
    }

    public Expression getValue() {
        return value;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitPositionalArgument(this);
    }

}