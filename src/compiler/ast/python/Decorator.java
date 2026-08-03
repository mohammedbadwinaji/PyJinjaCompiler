package compiler.ast.python;

import java.util.List;
import java.util.Objects;

public final class Decorator extends AbstractAstNode {

    private final Expression target;

    private final List<Argument> arguments;

    public Decorator(
            int line,
            Expression target,
            List<Argument> arguments) {

        super("Decorator", line);

        this.target = Objects.requireNonNull(target);
        this.arguments = List.copyOf(
                Objects.requireNonNull(arguments));
    }

    public Expression getTarget() {
        return target;
    }

    public List<Argument> getArguments() {
        return arguments;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitDecorator(this);
    }

    @Override
    public String prettyPrint(String indent) {

        StringBuilder sb = new StringBuilder();

        sb.append(indent)
                .append("Decorator [line ")
                .append(getLine())
                .append("]");

        sb.append("\n")
                .append(target.prettyPrint(indent + "  "));

        for (Argument arg : arguments) {
            sb.append("\n")
                    .append(arg.prettyPrint(indent + "  "));
        }

        return sb.toString();
    }
}