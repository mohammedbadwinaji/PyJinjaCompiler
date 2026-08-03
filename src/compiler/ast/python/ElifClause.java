package compiler.ast.python;

import java.util.List;
import java.util.Objects;

public final class ElifClause
        extends AbstractAstNode {

    private final Expression condition;

    private final List<Statement> body;

    public ElifClause(
            int line,
            Expression condition,
            List<Statement> body) {

        super("ElifClause", line);

        this.condition =
                Objects.requireNonNull(condition);

        this.body =
                List.copyOf(body);
    }

    public Expression getCondition() {
        return condition;
    }

    public List<Statement> getBody() {
        return body;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitElifClause(this);
    }

}