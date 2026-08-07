package compiler.ast.jinja;

import compiler.ast.common.AbstractAstNode;
import compiler.ast.common.AstVisitor;

import java.util.List;
import java.util.Objects;

/**
 * Root of the Jinja AST (the Jinja-tree equivalent of Program.java on the
 * Python side). Holds the ordered sequence of top-level template elements:
 * raw HTML text, {{ expression }} outputs, {% if %} blocks and {% for %} blocks.
 */
public final class Template extends AbstractAstNode {

    private final List<TemplateElement> elements;

    public Template(
            int line,
            List<TemplateElement> elements) {

        super("Template", line);

        this.elements = List.copyOf(
                Objects.requireNonNull(elements));
    }

    public List<TemplateElement> getElements() {
        return elements;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitTemplate(this);
    }

    @Override
    public String prettyPrint(String indent) {

        StringBuilder builder = new StringBuilder();

        builder.append(super.prettyPrint(indent));

        for (TemplateElement element : elements) {

            builder.append(System.lineSeparator())
                    .append(element.prettyPrint(indent + "  "));
        }

        return builder.toString();
    }

}
