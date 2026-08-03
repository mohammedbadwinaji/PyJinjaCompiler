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

}