package compiler.ast.jinja;

import compiler.ast.common.AstVisitor;

import java.util.Objects;

/**
 * Raw HTML/text content that sits between Jinja tags (the HTML_TEXT token
 * from the Jinja lexer). A leaf node in the Jinja AST.
 */
public final class HtmlText extends AbstractTemplateElement {

    private final String text;

    public HtmlText(int line, String text) {

        super("HtmlText", line);

        this.text = Objects.requireNonNull(text);
    }

    public String getText() {
        return text;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitHtmlText(this);
    }

    @Override
    public String prettyPrint(String indent) {

        String preview = text.replace("\n", "\\n").replace("\r", "");
        if (preview.length() > 40) {
            preview = preview.substring(0, 40) + "...";
        }

        return indent +
                getNodeName() +
                " (\"" +
                preview +
                "\") [line " +
                getLine() +
                "]";
    }

}
