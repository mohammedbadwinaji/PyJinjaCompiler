package compiler.ast.python;

import java.util.Objects;

public final class KeywordArgument extends AbstractAstNode implements Argument {

    private final String name;

    private final Expression value;

    public KeywordArgument(
            int line,
            String name,
            Expression value) {

        super("KeywordArgument", line);

        this.name = Objects.requireNonNull(name);
        this.value = Objects.requireNonNull(value);
    }

    public String getName() {
        return name;
    }

    public Expression getValue() {
        return value;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitKeywordArgument(this);
    }

}