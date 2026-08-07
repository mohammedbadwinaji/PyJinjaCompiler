package compiler.ast.python;

import compiler.ast.common.AstVisitor;
import compiler.ast.common.Expression;

import java.util.List;
import java.util.Objects;

public final class ElifClause extends AbstractStatement {

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

    @Override
    public String prettyPrint(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("ElifClause (line ").append(getLine()).append(")");
        sb.append("\n").append(indent).append("  Condition");
        sb.append("\n").append(condition.prettyPrint(indent + "    "));
        sb.append("\n").append(indent).append("  Body");
        for (Statement stmt : body) {
            sb.append("\n").append(stmt.prettyPrint(indent + "    "));
        }
        return sb.toString();
    }
}