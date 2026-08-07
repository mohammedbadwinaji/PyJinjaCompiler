package compiler.ast.python;

import compiler.ast.common.AstVisitor;
import compiler.ast.common.Expression;

import java.util.List;
import java.util.Objects;

/**
 * Assignment statement.
 */
public final class Assign extends AbstractStatement {

    private final List<Expression> targets;
    private final Expression value;

    public Assign(
            int line,
            List<Expression> targets,
            Expression value) {

        super("Assign", line);

        this.targets = List.copyOf(
                Objects.requireNonNull(targets));

        this.value = Objects.requireNonNull(value);
    }

    public List<Expression> getTargets() {
        return targets;
    }

    public Expression getValue() {
        return value;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitAssign(this);
    }

    @Override
    public String prettyPrint(String indent) {

        StringBuilder sb = new StringBuilder();

        sb.append(indent)
                .append("Assign [line ")
                .append(getLine())
                .append("]");

        sb.append("\n")
                .append(indent)
                .append("  Targets");

        for (Expression e : targets) {

            sb.append("\n")
                    .append(e.prettyPrint(indent + "    "));
        }

        sb.append("\n")
                .append(indent)
                .append("  Value\n")
                .append(value.prettyPrint(indent + "    "));

        return sb.toString();
    }

}