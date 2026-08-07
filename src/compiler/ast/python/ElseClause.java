package compiler.ast.python;

import compiler.ast.common.AstVisitor;

import java.util.List;
import java.util.Objects;

/**
 * Else clause for if statements.
 */
public final class ElseClause extends AbstractStatement {

    private final List<Statement> body;

    public ElseClause(
            int line,
            List<Statement> body) {

        super("ElseClause", line);

        this.body = List.copyOf(
                Objects.requireNonNull(body));
    }

    public List<Statement> getBody() {
        return body;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitElseClause(this);
    }

    @Override
    public String prettyPrint(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("ElseClause (line ").append(getLine()).append(")");
        sb.append("\n").append(indent).append("  Body");
        for (Statement stmt : body) {
            sb.append("\n").append(stmt.prettyPrint(indent + "    "));
        }
        return sb.toString();
    }
}
