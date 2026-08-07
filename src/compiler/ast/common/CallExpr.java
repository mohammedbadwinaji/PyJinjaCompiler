package compiler.ast.common;

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

    @Override
    public String prettyPrint(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("CallExpr (line ").append(getLine()).append(")");
        sb.append("\n").append(indent).append("  Callee");
        sb.append("\n").append(callee.prettyPrint(indent + "    "));
        sb.append("\n").append(indent).append("  Arguments");
        if (arguments.isEmpty()) {
            sb.append(" []");
        } else {
            for (Argument arg : arguments) {
                sb.append("\n").append(arg.prettyPrint(indent + "    "));
            }
        }
        return sb.toString();
    }


}
