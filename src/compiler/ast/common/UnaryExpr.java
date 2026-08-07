package compiler.ast.common;


public class UnaryExpr extends AbstractExpression  {

    private  final UnaryOperator operator;
    private final Expression expr;
    public UnaryExpr(
            int line,
            UnaryOperator operator,
            Expression expr
    ){

        super("UnaryExpr", line);
        this.operator = operator;
        this.expr = expr;
    }

    public UnaryOperator getOperator() {
        return operator;
    }

    public Expression getExpr() {
        return expr;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitUnaryExpr(this);
    }

}
