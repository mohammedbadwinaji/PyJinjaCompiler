// Generated from D:/Compiler/PyJinjaCompiler/src/grammar/jinja/JinjaParser.g4 by ANTLR 4.13.2
package compiler.generated.jinja;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link JinjaParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface JinjaParserVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link JinjaParser#template}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTemplate(JinjaParser.TemplateContext ctx);
	/**
	 * Visit a parse tree produced by {@link JinjaParser#templateBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTemplateBody(JinjaParser.TemplateBodyContext ctx);
	/**
	 * Visit a parse tree produced by the {@code htmlElement}
	 * labeled alternative in {@link JinjaParser#element}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHtmlElement(JinjaParser.HtmlElementContext ctx);
	/**
	 * Visit a parse tree produced by the {@code expressionElement}
	 * labeled alternative in {@link JinjaParser#element}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpressionElement(JinjaParser.ExpressionElementContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ifElement}
	 * labeled alternative in {@link JinjaParser#element}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIfElement(JinjaParser.IfElementContext ctx);
	/**
	 * Visit a parse tree produced by the {@code forElement}
	 * labeled alternative in {@link JinjaParser#element}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForElement(JinjaParser.ForElementContext ctx);
	/**
	 * Visit a parse tree produced by {@link JinjaParser#expressionOutput}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpressionOutput(JinjaParser.ExpressionOutputContext ctx);
	/**
	 * Visit a parse tree produced by {@link JinjaParser#ifStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIfStatement(JinjaParser.IfStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link JinjaParser#elifClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitElifClause(JinjaParser.ElifClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link JinjaParser#elseClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitElseClause(JinjaParser.ElseClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link JinjaParser#forStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForStatement(JinjaParser.ForStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link JinjaParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpr(JinjaParser.ExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicalAndExpr}
	 * labeled alternative in {@link JinjaParser#logicalOr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalAndExpr(JinjaParser.LogicalAndExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code orExpr}
	 * labeled alternative in {@link JinjaParser#logicalOr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrExpr(JinjaParser.OrExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code comparisonExpr}
	 * labeled alternative in {@link JinjaParser#logicalAnd}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparisonExpr(JinjaParser.ComparisonExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code andExpr}
	 * labeled alternative in {@link JinjaParser#logicalAnd}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAndExpr(JinjaParser.AndExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code comparisonOpExpr}
	 * labeled alternative in {@link JinjaParser#comparison}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparisonOpExpr(JinjaParser.ComparisonOpExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code additiveExpr}
	 * labeled alternative in {@link JinjaParser#comparison}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAdditiveExpr(JinjaParser.AdditiveExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code addExpr}
	 * labeled alternative in {@link JinjaParser#additive}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAddExpr(JinjaParser.AddExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code multiplicativeExpr}
	 * labeled alternative in {@link JinjaParser#additive}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultiplicativeExpr(JinjaParser.MultiplicativeExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code unaryExpr}
	 * labeled alternative in {@link JinjaParser#multiplicative}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnaryExpr(JinjaParser.UnaryExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code mulExpr}
	 * labeled alternative in {@link JinjaParser#multiplicative}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMulExpr(JinjaParser.MulExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code notExpr}
	 * labeled alternative in {@link JinjaParser#unary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNotExpr(JinjaParser.NotExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code unaryMinus}
	 * labeled alternative in {@link JinjaParser#unary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnaryMinus(JinjaParser.UnaryMinusContext ctx);
	/**
	 * Visit a parse tree produced by the {@code primaryExpr}
	 * labeled alternative in {@link JinjaParser#unary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimaryExpr(JinjaParser.PrimaryExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code parenExpr}
	 * labeled alternative in {@link JinjaParser#primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParenExpr(JinjaParser.ParenExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code literalExpr}
	 * labeled alternative in {@link JinjaParser#primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLiteralExpr(JinjaParser.LiteralExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code identifierExpr}
	 * labeled alternative in {@link JinjaParser#primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifierExpr(JinjaParser.IdentifierExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code stringLiteral}
	 * labeled alternative in {@link JinjaParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringLiteral(JinjaParser.StringLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code intLiteral}
	 * labeled alternative in {@link JinjaParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntLiteral(JinjaParser.IntLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code floatLiteral}
	 * labeled alternative in {@link JinjaParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFloatLiteral(JinjaParser.FloatLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code trueLiteral}
	 * labeled alternative in {@link JinjaParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTrueLiteral(JinjaParser.TrueLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code falseLiteral}
	 * labeled alternative in {@link JinjaParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFalseLiteral(JinjaParser.FalseLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code noneLiteral}
	 * labeled alternative in {@link JinjaParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNoneLiteral(JinjaParser.NoneLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code attributeAccess}
	 * labeled alternative in {@link JinjaParser#trailer}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttributeAccess(JinjaParser.AttributeAccessContext ctx);
	/**
	 * Visit a parse tree produced by the {@code indexAccess}
	 * labeled alternative in {@link JinjaParser#trailer}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndexAccess(JinjaParser.IndexAccessContext ctx);
	/**
	 * Visit a parse tree produced by the {@code callTrailer}
	 * labeled alternative in {@link JinjaParser#trailer}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCallTrailer(JinjaParser.CallTrailerContext ctx);
	/**
	 * Visit a parse tree produced by {@link JinjaParser#argList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArgList(JinjaParser.ArgListContext ctx);
}