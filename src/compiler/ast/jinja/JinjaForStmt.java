package compiler.ast.jinja;

import compiler.ast.common.AstVisitor;
import compiler.ast.common.Expression;
import compiler.ast.common.Identifier;

import java.util.List;
import java.util.Objects;

/**
 * {% for variable in iterable %} body {% endfor %}
 * (the Jinja-tree equivalent of ForStmt.java on the Python side).
 */
public final class JinjaForStmt extends AbstractTemplateElement {

    private final Identifier variable;

    private final Expression iterable;

    private final List<TemplateElement> body;

    public JinjaForStmt(
            int line,
            Identifier variable,
            Expression iterable,
            List<TemplateElement> body) {

        super("JinjaForStmt", line);

        this.variable =
                Objects.requireNonNull(variable);

        this.iterable =
                Objects.requireNonNull(iterable);

        this.body =
                List.copyOf(body);
    }

    public Identifier getVariable() {
        return variable;
    }

    public Expression getIterable() {
        return iterable;
    }

    public List<TemplateElement> getBody() {
        return body;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitJinjaForStmt(this);
    }

    @Override
    public String prettyPrint(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("JinjaForStmt (line ").append(getLine()).append(")");
        sb.append("\n").append(indent).append("  Variable");
        sb.append("\n").append(variable.prettyPrint(indent + "    "));
        sb.append("\n").append(indent).append("  Iterable");
        sb.append("\n").append(iterable.prettyPrint(indent + "    "));
        sb.append("\n").append(indent).append("  Body");
        for (TemplateElement element : body) {
            sb.append("\n").append(element.prettyPrint(indent + "    "));
        }
        return sb.toString();
    }

}
