package compiler.ast.jinja;

import compiler.ast.common.AstVisitor;
import compiler.ast.common.Expression;

import java.util.List;
import java.util.Objects;

/**
 * {% if condition %} thenBody {% elif ... %} ... {% else %} ... {% endif %}
 * (the Jinja-tree equivalent of IfStmt.java on the Python side).
 */
public final class JinjaIfStmt extends AbstractTemplateElement {

    private final Expression condition;

    private final List<TemplateElement> thenBody;

    private final List<JinjaElifClause> elifClauses;

    private final JinjaElseClause elseClause;

    public JinjaIfStmt(
            int line,
            Expression condition,
            List<TemplateElement> thenBody,
            List<JinjaElifClause> elifClauses,
            JinjaElseClause elseClause) {

        super("JinjaIfStmt", line);

        this.condition = Objects.requireNonNull(condition);
        this.thenBody = List.copyOf(
                Objects.requireNonNull(thenBody));
        this.elifClauses = List.copyOf(
                Objects.requireNonNull(elifClauses));
        this.elseClause = elseClause; // Can be null
    }

    public Expression getCondition() {
        return condition;
    }

    public List<TemplateElement> getThenBody() {
        return thenBody;
    }

    public List<JinjaElifClause> getElifClauses() {
        return elifClauses;
    }

    public JinjaElseClause getElseClause() {
        return elseClause;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitJinjaIfStmt(this);
    }

    @Override
    public String prettyPrint(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("JinjaIfStmt (line ").append(getLine()).append(")");
        sb.append("\n").append(indent).append("  Condition");
        sb.append("\n").append(condition.prettyPrint(indent + "    "));
        sb.append("\n").append(indent).append("  ThenBody");
        for (TemplateElement element : thenBody) {
            sb.append("\n").append(element.prettyPrint(indent + "    "));
        }
        for (JinjaElifClause elif : elifClauses) {
            sb.append("\n").append(elif.prettyPrint(indent + "  "));
        }
        if (elseClause != null) {
            sb.append("\n").append(elseClause.prettyPrint(indent + "  "));
        }
        return sb.toString();
    }
}
