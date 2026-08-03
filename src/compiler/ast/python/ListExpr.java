package compiler.ast.python;

import java.util.List;
import java.util.Objects;

/**
 * Python list literal.
 */
public final class ListExpr extends LiteralExpression {

    private final List<Expression> elements;

    public ListExpr(
            int line,
            List<Expression> elements) {

        super("ListExpr", line);

        this.elements = List.copyOf(
                Objects.requireNonNull(elements)
        );
    }

    public List<Expression> getElements() {
        return elements;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitListExpr(this);
    }

    @Override
    public String prettyPrint(String indent) {

        StringBuilder sb = new StringBuilder();

        sb.append(indent)
                .append(getNodeName())
                .append(" [line ")
                .append(getLine())
                .append("]");

        for (Expression expression : elements) {

            sb.append("\n")
                    .append(expression.prettyPrint(indent + "  "));
        }

        return sb.toString();
    }

}