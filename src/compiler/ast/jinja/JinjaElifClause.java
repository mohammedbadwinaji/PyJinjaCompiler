package compiler.ast.jinja;

import compiler.ast.common.AstVisitor;
import compiler.ast.common.Expression;

import java.util.List;
import java.util.Objects;

/**
 * {% elif condition %} body (the Jinja-tree equivalent of ElifClause.java).
 */
public final class JinjaElifClause extends AbstractTemplateElement {

    private final Expression condition;

    private final List<TemplateElement> body;

    public JinjaElifClause(
            int line,
            Expression condition,
            List<TemplateElement> body) {

        super("JinjaElifClause", line);

        this.condition =
                Objects.requireNonNull(condition);

        this.body =
                List.copyOf(body);
    }

    public Expression getCondition() {
        return condition;
    }

    public List<TemplateElement> getBody() {
        return body;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitJinjaElifClause(this);
    }

    @Override
    public String prettyPrint(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("JinjaElifClause (line ").append(getLine()).append(")");
        sb.append("\n").append(indent).append("  Condition");
        sb.append("\n").append(condition.prettyPrint(indent + "    "));
        sb.append("\n").append(indent).append("  Body");
        for (TemplateElement element : body) {
            sb.append("\n").append(element.prettyPrint(indent + "    "));
        }
        return sb.toString();
    }
}
