package compiler.ast.python;

import java.util.Optional;

/**
 * return statement.
 */
public final class ReturnStmt
        extends AbstractStatement {

    private final Expression value;

    public ReturnStmt(
            int line,
            Expression value) {

        super("ReturnStmt", line);

        this.value = value;
    }

    public Optional<Expression> getValue() {

        return Optional.ofNullable(value);
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {

        return visitor.visitReturnStmt(this);
    }

}