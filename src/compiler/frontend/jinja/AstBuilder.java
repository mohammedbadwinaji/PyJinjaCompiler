package compiler.frontend.jinja;

import compiler.ast.jinja.*;
import compiler.ast.common.*;
import compiler.generated.jinja.JinjaParser;
import compiler.generated.jinja.JinjaParserBaseVisitor;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AstBuilder: converts ANTLR parse tree (compiler.generated.jinja.JinjaParser)
 * into the project's Jinja AST nodes (compiler.ast.jinja.*), reusing the
 * shared expression nodes from compiler.ast.common.* wherever possible
 * (Identifier, literals, BinaryExpr, UnaryExpr, CallExpr, AttributeAccess,
 * IndexAccess, PositionalArgument) so that the Python and Jinja trees stay
 * interoperable.
 *
 * Mirrors compiler.frontend.python.AstBuilder in style and structure.
 */
public final class AstBuilder extends JinjaParserBaseVisitor<Object> {

    /* -------------------------
       Public entry point
       ------------------------- */

    public Template build(JinjaParser.TemplateContext ctx) {
        List<TemplateElement> elements = ctx.templateBody().element().stream()
                .map(this::toElement)
                .collect(Collectors.toList());
        return new Template(lineOf(ctx), elements);
    }

    /* -------------------------
       Helpers
       ------------------------- */

    private int lineOf(org.antlr.v4.runtime.ParserRuleContext ctx) {
        return ctx == null ? -1 : ctx.getStart().getLine();
    }

    private int lineOf(TerminalNode node) {
        return node == null ? -1 : node.getSymbol().getLine();
    }

    private int lineOf(org.antlr.v4.runtime.Token token) {
        return token == null ? -1 : token.getLine();
    }

    private TemplateElement toElement(JinjaParser.ElementContext ctx) {
        Object o = visit(ctx);
        if (o == null) return null;
        if (!(o instanceof TemplateElement)) {
            throw new IllegalStateException("Expected TemplateElement but got " + o.getClass().getSimpleName()
                    + " at line " + lineOf(ctx));
        }
        return (TemplateElement) o;
    }

    private List<TemplateElement> toElementList(JinjaParser.TemplateBodyContext ctx) {
        if (ctx == null) return Collections.emptyList();
        return ctx.element().stream()
                .map(this::toElement)
                .collect(Collectors.toList());
    }

    private Expression toExpression(JinjaParser.ExprContext ctx) {
        Object o = visit(ctx);
        if (o == null) return null;
        if (!(o instanceof Expression)) {
            throw new IllegalStateException("Expected Expression but got " + o.getClass().getSimpleName()
                    + " at line " + lineOf(ctx));
        }
        return (Expression) o;
    }

    private List<Argument> toArgumentList(JinjaParser.ArgListContext ctx) {
        if (ctx == null) return Collections.emptyList();
        return ctx.expr().stream()
                .map(e -> (Argument) new PositionalArgument(lineOf(e), toExpression(e)))
                .collect(Collectors.toList());
    }

    private String unquote(String s) {
        if (s == null || s.length() < 2) return "";
        String inner = s.substring(1, s.length() - 1);
        return inner.replace("\\\"", "\"").replace("\\'", "'").replace("\\n", "\n").replace("\\t", "\t");
    }

    private BinaryOperator mapComparisonOperator(String op) {
        switch (op) {
            case "==": return BinaryOperator.EQ;
            case "!=": return BinaryOperator.NE;
            case "<":  return BinaryOperator.LT;
            case "<=": return BinaryOperator.LE;
            case ">":  return BinaryOperator.GT;
            case ">=": return BinaryOperator.GE;
            default: throw new IllegalArgumentException("Unknown comparison operator: " + op);
        }
    }

    /* -------------------------
       Template root / elements
       ------------------------- */

    @Override
    public Object visitTemplate(JinjaParser.TemplateContext ctx) {
        return build(ctx);
    }

    @Override
    public Object visitHtmlElement(JinjaParser.HtmlElementContext ctx) {
        return new HtmlText(lineOf(ctx), ctx.HTML_TEXT().getText());
    }

    @Override
    public Object visitExpressionElement(JinjaParser.ExpressionElementContext ctx) {
        return visit(ctx.expressionOutput());
    }

    @Override
    public Object visitIfElement(JinjaParser.IfElementContext ctx) {
        return visit(ctx.ifStatement());
    }

    @Override
    public Object visitForElement(JinjaParser.ForElementContext ctx) {
        return visit(ctx.forStatement());
    }

    @Override
    public Object visitExpressionOutput(JinjaParser.ExpressionOutputContext ctx) {
        // JINJA_EXPR_START expression=expr JINJA_EXPR_END
        Expression expr = toExpression(ctx.expression);
        return new ExpressionOutput(lineOf(ctx), expr);
    }

    @Override
    public Object visitIfStatement(JinjaParser.IfStatementContext ctx) {
        // JINJA_STMT_START IF condition=expr JINJA_STMT_END
        //   thenBody=templateBody
        //   elifClause*
        //   elseClause?
        // JINJA_STMT_START ENDIF JINJA_STMT_END
        Expression cond = toExpression(ctx.condition);
        List<TemplateElement> thenBody = toElementList(ctx.thenBody);

        List<JinjaElifClause> elifs = ctx.elifClause().stream()
                .map(e -> (JinjaElifClause) visit(e))
                .collect(Collectors.toList());

        JinjaElseClause elseClause = ctx.elseClause() == null
                ? null
                : (JinjaElseClause) visit(ctx.elseClause());

        return new JinjaIfStmt(lineOf(ctx), cond, thenBody, elifs, elseClause);
    }

    @Override
    public Object visitElifClause(JinjaParser.ElifClauseContext ctx) {
        // JINJA_STMT_START ELIF condition=expr JINJA_STMT_END body=templateBody
        Expression cond = toExpression(ctx.condition);
        List<TemplateElement> body = toElementList(ctx.body);
        return new JinjaElifClause(lineOf(ctx), cond, body);
    }

    @Override
    public Object visitElseClause(JinjaParser.ElseClauseContext ctx) {
        // JINJA_STMT_START ELSE JINJA_STMT_END body=templateBody
        List<TemplateElement> body = toElementList(ctx.body);
        return new JinjaElseClause(lineOf(ctx), body);
    }

    @Override
    public Object visitForStatement(JinjaParser.ForStatementContext ctx) {
        // JINJA_STMT_START FOR variable=IDENTIFIER IN iterable=expr JINJA_STMT_END
        //   body=templateBody
        // JINJA_STMT_START ENDFOR JINJA_STMT_END
        Identifier var = new Identifier(lineOf(ctx.variable), ctx.variable.getText());
        Expression iterable = toExpression(ctx.iterable);
        List<TemplateElement> body = toElementList(ctx.body);
        return new JinjaForStmt(lineOf(ctx), var, iterable, body);
    }

    /* -------------------------
       Expression visitors (labels) - operator precedence chain
       ------------------------- */

    @Override
    public Object visitExpr(JinjaParser.ExprContext ctx) {
        return visit(ctx.logicalOr());
    }

    @Override
    public Object visitOrExpr(JinjaParser.OrExprContext ctx) {
        Expression left = (Expression) visit(ctx.left);
        Expression right = (Expression) visit(ctx.right);
        return new BinaryExpr(lineOf(ctx), left, BinaryOperator.OR, right);
    }

    @Override
    public Object visitLogicalAndExpr(JinjaParser.LogicalAndExprContext ctx) {
        // Pass-through: logicalOr -> logicalAnd
        return visit(ctx.logicalAnd());
    }

    @Override
    public Object visitAndExpr(JinjaParser.AndExprContext ctx) {
        Expression left = (Expression) visit(ctx.left);
        Expression right = (Expression) visit(ctx.right);
        return new BinaryExpr(lineOf(ctx), left, BinaryOperator.AND, right);
    }

    @Override
    public Object visitComparisonExpr(JinjaParser.ComparisonExprContext ctx) {
        // Pass-through: logicalAnd -> comparison
        return visit(ctx.comparison());
    }

    @Override
    public Object visitComparisonOpExpr(JinjaParser.ComparisonOpExprContext ctx) {
        Expression left = (Expression) visit(ctx.left);
        Expression right = (Expression) visit(ctx.right);
        BinaryOperator op = mapComparisonOperator(ctx.op.getText());
        return new BinaryExpr(lineOf(ctx), left, op, right);
    }

    @Override
    public Object visitAdditiveExpr(JinjaParser.AdditiveExprContext ctx) {
        // Pass-through: comparison -> additive
        return visit(ctx.additive());
    }

    @Override
    public Object visitAddExpr(JinjaParser.AddExprContext ctx) {
        Expression left = (Expression) visit(ctx.left);
        Expression right = (Expression) visit(ctx.right);
        BinaryOperator op = ctx.op.getText().equals("+") ? BinaryOperator.ADD : BinaryOperator.SUBTRACT;
        return new BinaryExpr(lineOf(ctx), left, op, right);
    }

    @Override
    public Object visitMultiplicativeExpr(JinjaParser.MultiplicativeExprContext ctx) {
        // Pass-through: additive -> multiplicative
        return visit(ctx.multiplicative());
    }

    @Override
    public Object visitMulExpr(JinjaParser.MulExprContext ctx) {
        Expression left = (Expression) visit(ctx.left);
        Expression right = (Expression) visit(ctx.right);
        BinaryOperator op;
        String opText = ctx.op.getText();
        if (opText.equals("*")) op = BinaryOperator.MULTIPLY;
        else if (opText.equals("/")) op = BinaryOperator.DIVIDE;
        else op = BinaryOperator.MODULO;
        return new BinaryExpr(lineOf(ctx), left, op, right);
    }

    @Override
    public Object visitUnaryExpr(JinjaParser.UnaryExprContext ctx) {
        // Pass-through: multiplicative -> unary
        return visit(ctx.unary());
    }

    @Override
    public Object visitNotExpr(JinjaParser.NotExprContext ctx) {
        Expression operand = (Expression) visit(ctx.operand);
        return new UnaryExpr(lineOf(ctx), UnaryOperator.NOT, operand);
    }

    @Override
    public Object visitUnaryMinus(JinjaParser.UnaryMinusContext ctx) {
        Expression operand = (Expression) visit(ctx.operand);
        return new UnaryExpr(lineOf(ctx), UnaryOperator.MINUS, operand);
    }

    @Override
    public Object visitPrimaryExpr(JinjaParser.PrimaryExprContext ctx) {
        // Pass-through: unary -> primary
        return visit(ctx.primary());
    }

    /* -------------------------
       Primary / literal visitors
       ------------------------- */

    @Override
    public Object visitParenExpr(JinjaParser.ParenExprContext ctx) {
        return toExpression(ctx.expression);
    }

    @Override
    public Object visitLiteralExpr(JinjaParser.LiteralExprContext ctx) {
        // Pass-through: primary -> literal
        return visit(ctx.literal());
    }

    @Override
    public Object visitIdentifierExpr(JinjaParser.IdentifierExprContext ctx) {
        // identifier=IDENTIFIER trailers+=trailer*
        Expression base = new Identifier(lineOf(ctx), ctx.identifier.getText());
        for (JinjaParser.TrailerContext t : ctx.trailer()) {
            base = applyTrailer(base, t);
        }
        return base;
    }

    @Override
    public Object visitStringLiteral(JinjaParser.StringLiteralContext ctx) {
        return new StringLiteral(lineOf(ctx), unquote(ctx.STRING().getText()));
    }

    @Override
    public Object visitIntLiteral(JinjaParser.IntLiteralContext ctx) {
        long v = Long.parseLong(ctx.INT().getText());
        return new IntegerLiteral(lineOf(ctx), v);
    }

    @Override
    public Object visitFloatLiteral(JinjaParser.FloatLiteralContext ctx) {
        double v = Double.parseDouble(ctx.FLOAT().getText());
        return new FloatLiteral(lineOf(ctx), v);
    }

    @Override
    public Object visitTrueLiteral(JinjaParser.TrueLiteralContext ctx) {
        return new BooleanLiteral(lineOf(ctx), true);
    }

    @Override
    public Object visitFalseLiteral(JinjaParser.FalseLiteralContext ctx) {
        return new BooleanLiteral(lineOf(ctx), false);
    }

    @Override
    public Object visitNoneLiteral(JinjaParser.NoneLiteralContext ctx) {
        return new NoneLiteral(lineOf(ctx));
    }

    /* -------------------------
       Trailer helpers (mirrors Python's applyTrailer)
       ------------------------- */

    private Expression applyTrailer(Expression base, JinjaParser.TrailerContext t) {

        // trailer alternatives (labeled):
        //   DOT attribute=IDENTIFIER            #attributeAccess
        //   | LBRACK index=expr RBRACK          #indexAccess
        //   | LPAREN arguments=argList? RPAREN  #callTrailer

        if (t instanceof JinjaParser.AttributeAccessContext) {
            JinjaParser.AttributeAccessContext ac = (JinjaParser.AttributeAccessContext) t;
            String attr = ac.attribute.getText();
            return new AttributeAccess(lineOf(ac), base, attr);
        }

        if (t instanceof JinjaParser.IndexAccessContext) {
            JinjaParser.IndexAccessContext ic = (JinjaParser.IndexAccessContext) t;
            Expression idx = toExpression(ic.index);
            return new IndexAccess(lineOf(ic), base, idx);
        }

        if (t instanceof JinjaParser.CallTrailerContext) {
            JinjaParser.CallTrailerContext cc = (JinjaParser.CallTrailerContext) t;
            List<Argument> args = toArgumentList(cc.arguments);
            return new CallExpr(lineOf(cc), base, args);
        }

        throw new IllegalStateException("Unknown trailer alternative at line " + lineOf(t));
    }

    /* -------------------------
       Fallbacks
       ------------------------- */

    @Override
    public Object visitChildren(org.antlr.v4.runtime.tree.RuleNode node) {
        return super.visitChildren(node);
    }

    @Override
    protected Object defaultResult() {
        return null;
    }
}
