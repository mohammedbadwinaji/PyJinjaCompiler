package compiler.ast.python;

import java.util.List;
import java.util.Objects;

public final class CallExpr extends AbstractExpression {

    private final Expression callee;

    private final List<Argument> arguments;

    public CallExpr(
            int line,
            Expression callee,
            List<Argument> arguments) {

        super("CallExpr", line);

        this.callee = Objects.requireNonNull(callee);
        this.arguments = List.copyOf(arguments);
    }

    public Expression getCallee() {
        return callee;
    }

    public List<Argument> getArguments() {
        return arguments;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitCallExpr(this);
    }

}