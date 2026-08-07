package compiler.ast.common;

import java.util.Objects;

/**
 * target[index]
 */
public final class IndexAccess extends AbstractExpression {

    private final Expression target;

    private final Expression index;

    public IndexAccess(
            int line,
            Expression target,
            Expression index) {

        super("IndexAccess", line);

        this.target = Objects.requireNonNull(target);
        this.index = Objects.requireNonNull(index);
    }

    public Expression getTarget() {
        return target;
    }

    public Expression getIndex() {
        return index;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitIndexAccess(this);
    }

}
