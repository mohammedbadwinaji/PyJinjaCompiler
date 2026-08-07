package compiler.ast.python;

import compiler.ast.common.AstVisitor;
import compiler.ast.common.Expression;

import java.util.List;
import java.util.Objects;

public final class IfStmt extends AbstractStatement {

    private final Expression condition;

    private final List<Statement> thenBody;

    private final List<ElifClause> elifClauses;

    private final ElseClause elseClause;

    public IfStmt(
            int line,
            Expression condition,
            List<Statement> thenBody,
            List<ElifClause> elifClauses,
            ElseClause elseClause) {

        super("IfStmt", line);

        this.condition = Objects.requireNonNull(condition);
        this.thenBody = List.copyOf(
                Objects.requireNonNull(thenBody));
        this.elifClauses = List.copyOf(
                Objects.requireNonNull(elifClauses));
        this.elseClause = elseClause; // Can be null
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

    public ElseClause getElseClause() {
        return elseClause;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitIfStmt(this);
    }

    @Override
    public String prettyPrint(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("IfStmt (line ").append(getLine()).append(")");
        sb.append("\n").append(indent).append("  Condition");
        sb.append("\n").append(condition.prettyPrint(indent + "    "));
        sb.append("\n").append(indent).append("  ThenBody");
        for (Statement stmt : thenBody) {
            sb.append("\n").append(stmt.prettyPrint(indent + "    "));
        }
        for (ElifClause elif : elifClauses) {
            sb.append("\n").append(elif.prettyPrint(indent + "  "));
        }
        if (elseClause != null) {
            sb.append("\n").append(elseClause.prettyPrint(indent + "  "));
        }
        return sb.toString();
    }
}