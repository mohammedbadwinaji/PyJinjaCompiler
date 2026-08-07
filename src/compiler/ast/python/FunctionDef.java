package compiler.ast.python;

import compiler.ast.common.AstVisitor;

import java.util.List;
import java.util.Objects;

/**
 * Python function definition.
 */
public final class FunctionDef extends AbstractStatement {

    private final String name;

    private final List<FunctionParameter> parameters;

    private final List<Decorator> decorators;

    private final List<Statement> body;

    public FunctionDef(
            int line,
            String name,
            List<FunctionParameter> parameters,
            List<Decorator> decorators,
            List<Statement> body) {

        super("FunctionDef", line);

        this.name = Objects.requireNonNull(name);
        this.parameters = List.copyOf(
                Objects.requireNonNull(parameters));
        this.decorators = List.copyOf(
                Objects.requireNonNull(decorators));
        this.body = List.copyOf(
                Objects.requireNonNull(body));
    }

    public String getName() {
        return name;
    }

    public List<FunctionParameter> getParameters() {
        return parameters;
    }

    public List<Decorator> getDecorators() {
        return decorators;
    }

    public List<Statement> getBody() {
        return body;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitFunctionDef(this);
    }

    @Override
    public String prettyPrint(String indent) {

        StringBuilder sb = new StringBuilder();

        sb.append(indent)
                .append("FunctionDef(")
                .append(name)
                .append(") [line ")
                .append(getLine())
                .append("]");

        if (!decorators.isEmpty()) {

            sb.append("\n")
                    .append(indent)
                    .append("  Decorators");

            for (Decorator decorator : decorators) {
                sb.append("\n")
                        .append(decorator.prettyPrint(indent + "    "));
            }
        }

        if (!parameters.isEmpty()) {

            sb.append("\n")
                    .append(indent)
                    .append("  Parameters");

            for (FunctionParameter parameter : parameters) {
                sb.append("\n")
                        .append(parameter.prettyPrint(indent + "    "));
            }
        }

        sb.append("\n")
                .append(indent)
                .append("  Body");

        for (Statement stmt : body) {
            sb.append("\n")
                    .append(stmt.prettyPrint(indent + "    "));
        }

        return sb.toString();
    }
}