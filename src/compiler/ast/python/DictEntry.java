package compiler.ast.python;

import java.util.Objects;

/**
 * Single dictionary entry.
 */
public final class DictEntry extends AbstractAstNode {

    private final Expression key;

    private final Expression value;

    public DictEntry(
            int line,
            Expression key,
            Expression value) {

        super("DictEntry", line);

        this.key = Objects.requireNonNull(key);
        this.value = Objects.requireNonNull(value);
    }

    public Expression getKey() {
        return key;
    }

    public Expression getValue() {
        return value;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitDictEntry(this);
    }

    @Override
    public String prettyPrint(String indent) {

        return indent + "DictEntry\n"
                + key.prettyPrint(indent + "  ")
                + "\n"
                + value.prettyPrint(indent + "  ");
    }

}