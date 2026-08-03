package compiler.ast.python;

import java.util.Objects;

/**
 * Represents a function parameter.
 */
public final class FunctionParameter extends AbstractAstNode {

    private final String name;

    public FunctionParameter(int line, String name) {

        super("FunctionParameter", line);

        this.name = Objects.requireNonNull(name);
    }

    public String getName() {
        return name;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitFunctionParameter(this);
    }

    @Override
    public String prettyPrint(String indent) {

        return indent +
                "Parameter(" +
                name +
                ") [line " +
                getLine() +
                "]";
    }
}