package compiler.ast.jinja;

import compiler.ast.common.AstVisitor;

import java.util.List;
import java.util.Objects;

/**
 * {% else %} body (the Jinja-tree equivalent of ElseClause.java).
 */
public final class JinjaElseClause extends AbstractTemplateElement {

    private final List<TemplateElement> body;

    public JinjaElseClause(
            int line,
            List<TemplateElement> body) {

        super("JinjaElseClause", line);

        this.body = List.copyOf(
                Objects.requireNonNull(body));
    }

    public List<TemplateElement> getBody() {
        return body;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitJinjaElseClause(this);
    }

    @Override
    public String prettyPrint(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("JinjaElseClause (line ").append(getLine()).append(")");
        sb.append("\n").append(indent).append("  Body");
        for (TemplateElement element : body) {
            sb.append("\n").append(element.prettyPrint(indent + "    "));
        }
        return sb.toString();
    }
}
