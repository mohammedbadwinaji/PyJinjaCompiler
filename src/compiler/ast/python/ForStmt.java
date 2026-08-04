package compiler.ast.python;

import java.util.List;
import java.util.Objects;

public final class ForStmt
        extends AbstractStatement {

    private final String variable;

    private final Expression iterable;

    private final List<Statement> body;

    public ForStmt(
            int line,
            String variable,
            Expression iterable,
            List<Statement> body) {

        super("ForStmt", line);

        this.variable =
                Objects.requireNonNull(variable);

        this.iterable =
                Objects.requireNonNull(iterable);

        this.body =
                List.copyOf(body);
    }

    public String getVariable() {
        return variable;
    }

    public Expression getIterable() {
        return iterable;
    }

    public List<Statement> getBody() {
        return body;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {

        return visitor.visitForStmt(this);
    }

    @Override
    public String prettyPrint(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("ForStmt (line ").append(getLine()).append(")");
        sb.append("\n").append(indent).append("  Variable: ").append(variable);
        sb.append("\n").append(indent).append("  Iterable");
        sb.append("\n").append(iterable.prettyPrint(indent + "    "));
        sb.append("\n").append(indent).append("  Body");
        for (Statement stmt : body) {
            sb.append("\n").append(stmt.prettyPrint(indent + "    "));
        }
        return sb.toString();
    }

}