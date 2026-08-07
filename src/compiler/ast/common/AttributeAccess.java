package compiler.ast.common;

import java.util.Objects;

/**
 * obj.attribute
 */
public final class AttributeAccess extends AbstractExpression {

    private final Expression target;

    private final String attribute;

    public AttributeAccess(
            int line,
            Expression target,
            String attribute) {

        super("AttributeAccess", line);

        this.target = Objects.requireNonNull(target);
        this.attribute = Objects.requireNonNull(attribute);
    }

    public Expression getTarget() {
        return target;
    }

    public String getAttribute() {
        return attribute;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitAttributeAccess(this);
    }

}
