package compiler.ast.python;

import java.util.List;
import java.util.Objects;

public final class WhileStmt
        extends AbstractStatement {

    private final Expression condition;

    private final List<Statement> body;

    public WhileStmt(
            int line,
            Expression condition,
            List<Statement> body) {

        super("WhileStmt", line);

        this.condition = Objects.requireNonNull(condition);
        this.body = List.copyOf(body);
    }

    public Expression getCondition() {
        return condition;
    }

    public List<Statement> getBody() {
        return body;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitWhileStmt(this);
    }

    @Override
    public String prettyPrint(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("WhileStmt (line ").append(getLine()).append(")");
        sb.append("\n").append(indent).append("  Condition");
        sb.append("\n").append(condition.prettyPrint(indent + "    "));
        sb.append("\n").append(indent).append("  Body");
        for (Statement stmt : body) {
            sb.append("\n").append(stmt.prettyPrint(indent + "    "));
        }
        return sb.toString();
    }
}
