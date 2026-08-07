package compiler.ast.jinja;

import compiler.ast.common.AstVisitor;
import compiler.ast.common.Expression;

import java.util.Objects;

/**
 * A {{ expression }} output tag. Wraps the expression that should be
 * evaluated and substituted into the rendered output.
 */
public final class ExpressionOutput extends AbstractTemplateElement {

    private final Expression expression;

    public ExpressionOutput(int line, Expression expression) {

        super("ExpressionOutput", line);

        this.expression = Objects.requireNonNull(expression);
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitExpressionOutput(this);
    }

    @Override
    public String prettyPrint(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("ExpressionOutput (line ").append(getLine()).append(")");
        sb.append("\n").append(expression.prettyPrint(indent + "  "));
        return sb.toString();
    }

}
