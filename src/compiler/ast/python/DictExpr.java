package compiler.ast.python;

import java.util.List;
import java.util.Objects;

/**
 * Python dictionary literal.
 */
public final class DictExpr extends LiteralExpression {

    private final List<DictEntry> entries;

    public DictExpr(
            int line,
            List<DictEntry> entries) {

        super("DictExpr", line);

        this.entries = List.copyOf(
                Objects.requireNonNull(entries)
        );
    }

    public List<DictEntry> getEntries() {
        return entries;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitDictExpr(this);
    }

    @Override
    public String prettyPrint(String indent) {

        StringBuilder sb = new StringBuilder();

        sb.append(indent)
                .append(getNodeName())
                .append(" [line ")
                .append(getLine())
                .append("]");

        for (DictEntry entry : entries) {

            sb.append("\n")
                    .append(entry.prettyPrint(indent + "  "));
        }

        return sb.toString();
    }

}