package compiler.ast.python;

import java.util.List;
import java.util.Objects;

public final class IfStmt extends AbstractStatement {

    private final Expression condition;

    private final List<Statement> thenBody;

    private final List<ElifClause> elifClauses;

    private final List<Statement> elseBody;

    public IfStmt(
            int line,
            Expression condition,
            List<Statement> thenBody,
            List<ElifClause> elifClauses,
            List<Statement> elseBody) {

        super("IfStmt", line);

        this.condition = Objects.requireNonNull(condition);
        this.thenBody = List.copyOf(
                Objects.requireNonNull(thenBody));
        this.elifClauses = List.copyOf(
                Objects.requireNonNull(elifClauses));
        this.elseBody = List.copyOf(
                Objects.requireNonNull(elseBody));
    }

    public Expression getCondition() {
        return condition;
    }

    public List<Statement> getThenBody() {
        return thenBody;
    }

    public List<ElifClause> getElifClauses() {
        return elifClauses;
    }

    public List<Statement> getElseBody() {
        return elseBody;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitIfStmt(this);
    }
}