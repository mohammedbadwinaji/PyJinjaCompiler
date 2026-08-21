// Generated from D:/Compiler/PyJinjaCompiler/src/compiler.grammar/jinja/JinjaParser.g4 by ANTLR 4.13.2
package compiler.generated.jinja;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link JinjaParser}.
 */
public interface JinjaParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link JinjaParser#template}.
	 * @param ctx the parse tree
	 */
	void enterTemplate(JinjaParser.TemplateContext ctx);
	/**
	 * Exit a parse tree produced by {@link JinjaParser#template}.
	 * @param ctx the parse tree
	 */
	void exitTemplate(JinjaParser.TemplateContext ctx);
	/**
	 * Enter a parse tree produced by {@link JinjaParser#templateBody}.
	 * @param ctx the parse tree
	 */
	void enterTemplateBody(JinjaParser.TemplateBodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link JinjaParser#templateBody}.
	 * @param ctx the parse tree
	 */
	void exitTemplateBody(JinjaParser.TemplateBodyContext ctx);
	/**
	 * Enter a parse tree produced by the {@code htmlElement}
	 * labeled alternative in {@link JinjaParser#element}.
	 * @param ctx the parse tree
	 */
	void enterHtmlElement(JinjaParser.HtmlElementContext ctx);
	/**
	 * Exit a parse tree produced by the {@code htmlElement}
	 * labeled alternative in {@link JinjaParser#element}.
	 * @param ctx the parse tree
	 */
	void exitHtmlElement(JinjaParser.HtmlElementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code expressionElement}
	 * labeled alternative in {@link JinjaParser#element}.
	 * @param ctx the parse tree
	 */
	void enterExpressionElement(JinjaParser.ExpressionElementContext ctx);
	/**
	 * Exit a parse tree produced by the {@code expressionElement}
	 * labeled alternative in {@link JinjaParser#element}.
	 * @param ctx the parse tree
	 */
	void exitExpressionElement(JinjaParser.ExpressionElementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ifElement}
	 * labeled alternative in {@link JinjaParser#element}.
	 * @param ctx the parse tree
	 */
	void enterIfElement(JinjaParser.IfElementContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ifElement}
	 * labeled alternative in {@link JinjaParser#element}.
	 * @param ctx the parse tree
	 */
	void exitIfElement(JinjaParser.IfElementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code forElement}
	 * labeled alternative in {@link JinjaParser#element}.
	 * @param ctx the parse tree
	 */
	void enterForElement(JinjaParser.ForElementContext ctx);
	/**
	 * Exit a parse tree produced by the {@code forElement}
	 * labeled alternative in {@link JinjaParser#element}.
	 * @param ctx the parse tree
	 */
	void exitForElement(JinjaParser.ForElementContext ctx);
	/**
	 * Enter a parse tree produced by {@link JinjaParser#expressionOutput}.
	 * @param ctx the parse tree
	 */
	void enterExpressionOutput(JinjaParser.ExpressionOutputContext ctx);
	/**
	 * Exit a parse tree produced by {@link JinjaParser#expressionOutput}.
	 * @param ctx the parse tree
	 */
	void exitExpressionOutput(JinjaParser.ExpressionOutputContext ctx);
	/**
	 * Enter a parse tree produced by {@link JinjaParser#ifStatement}.
	 * @param ctx the parse tree
	 */
	void enterIfStatement(JinjaParser.IfStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link JinjaParser#ifStatement}.
	 * @param ctx the parse tree
	 */
	void exitIfStatement(JinjaParser.IfStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link JinjaParser#elifClause}.
	 * @param ctx the parse tree
	 */
	void enterElifClause(JinjaParser.ElifClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link JinjaParser#elifClause}.
	 * @param ctx the parse tree
	 */
	void exitElifClause(JinjaParser.ElifClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link JinjaParser#elseClause}.
	 * @param ctx the parse tree
	 */
	void enterElseClause(JinjaParser.ElseClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link JinjaParser#elseClause}.
	 * @param ctx the parse tree
	 */
	void exitElseClause(JinjaParser.ElseClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link JinjaParser#forStatement}.
	 * @param ctx the parse tree
	 */
	void enterForStatement(JinjaParser.ForStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link JinjaParser#forStatement}.
	 * @param ctx the parse tree
	 */
	void exitForStatement(JinjaParser.ForStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link JinjaParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterExpr(JinjaParser.ExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link JinjaParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitExpr(JinjaParser.ExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalAndExpr}
	 * labeled alternative in {@link JinjaParser#logicalOr}.
	 * @param ctx the parse tree
	 */
	void enterLogicalAndExpr(JinjaParser.LogicalAndExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalAndExpr}
	 * labeled alternative in {@link JinjaParser#logicalOr}.
	 * @param ctx the parse tree
	 */
	void exitLogicalAndExpr(JinjaParser.LogicalAndExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code orExpr}
	 * labeled alternative in {@link JinjaParser#logicalOr}.
	 * @param ctx the parse tree
	 */
	void enterOrExpr(JinjaParser.OrExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code orExpr}
	 * labeled alternative in {@link JinjaParser#logicalOr}.
	 * @param ctx the parse tree
	 */
	void exitOrExpr(JinjaParser.OrExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code comparisonExpr}
	 * labeled alternative in {@link JinjaParser#logicalAnd}.
	 * @param ctx the parse tree
	 */
	void enterComparisonExpr(JinjaParser.ComparisonExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code comparisonExpr}
	 * labeled alternative in {@link JinjaParser#logicalAnd}.
	 * @param ctx the parse tree
	 */
	void exitComparisonExpr(JinjaParser.ComparisonExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code andExpr}
	 * labeled alternative in {@link JinjaParser#logicalAnd}.
	 * @param ctx the parse tree
	 */
	void enterAndExpr(JinjaParser.AndExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code andExpr}
	 * labeled alternative in {@link JinjaParser#logicalAnd}.
	 * @param ctx the parse tree
	 */
	void exitAndExpr(JinjaParser.AndExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code comparisonOpExpr}
	 * labeled alternative in {@link JinjaParser#comparison}.
	 * @param ctx the parse tree
	 */
	void enterComparisonOpExpr(JinjaParser.ComparisonOpExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code comparisonOpExpr}
	 * labeled alternative in {@link JinjaParser#comparison}.
	 * @param ctx the parse tree
	 */
	void exitComparisonOpExpr(JinjaParser.ComparisonOpExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code additiveExpr}
	 * labeled alternative in {@link JinjaParser#comparison}.
	 * @param ctx the parse tree
	 */
	void enterAdditiveExpr(JinjaParser.AdditiveExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code additiveExpr}
	 * labeled alternative in {@link JinjaParser#comparison}.
	 * @param ctx the parse tree
	 */
	void exitAdditiveExpr(JinjaParser.AdditiveExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code addExpr}
	 * labeled alternative in {@link JinjaParser#additive}.
	 * @param ctx the parse tree
	 */
	void enterAddExpr(JinjaParser.AddExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code addExpr}
	 * labeled alternative in {@link JinjaParser#additive}.
	 * @param ctx the parse tree
	 */
	void exitAddExpr(JinjaParser.AddExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code multiplicativeExpr}
	 * labeled alternative in {@link JinjaParser#additive}.
	 * @param ctx the parse tree
	 */
	void enterMultiplicativeExpr(JinjaParser.MultiplicativeExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code multiplicativeExpr}
	 * labeled alternative in {@link JinjaParser#additive}.
	 * @param ctx the parse tree
	 */
	void exitMultiplicativeExpr(JinjaParser.MultiplicativeExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code unaryExpr}
	 * labeled alternative in {@link JinjaParser#multiplicative}.
	 * @param ctx the parse tree
	 */
	void enterUnaryExpr(JinjaParser.UnaryExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code unaryExpr}
	 * labeled alternative in {@link JinjaParser#multiplicative}.
	 * @param ctx the parse tree
	 */
	void exitUnaryExpr(JinjaParser.UnaryExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code mulExpr}
	 * labeled alternative in {@link JinjaParser#multiplicative}.
	 * @param ctx the parse tree
	 */
	void enterMulExpr(JinjaParser.MulExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code mulExpr}
	 * labeled alternative in {@link JinjaParser#multiplicative}.
	 * @param ctx the parse tree
	 */
	void exitMulExpr(JinjaParser.MulExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code notExpr}
	 * labeled alternative in {@link JinjaParser#unary}.
	 * @param ctx the parse tree
	 */
	void enterNotExpr(JinjaParser.NotExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code notExpr}
	 * labeled alternative in {@link JinjaParser#unary}.
	 * @param ctx the parse tree
	 */
	void exitNotExpr(JinjaParser.NotExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code unaryMinus}
	 * labeled alternative in {@link JinjaParser#unary}.
	 * @param ctx the parse tree
	 */
	void enterUnaryMinus(JinjaParser.UnaryMinusContext ctx);
	/**
	 * Exit a parse tree produced by the {@code unaryMinus}
	 * labeled alternative in {@link JinjaParser#unary}.
	 * @param ctx the parse tree
	 */
	void exitUnaryMinus(JinjaParser.UnaryMinusContext ctx);
	/**
	 * Enter a parse tree produced by the {@code primaryExpr}
	 * labeled alternative in {@link JinjaParser#unary}.
	 * @param ctx the parse tree
	 */
	void enterPrimaryExpr(JinjaParser.PrimaryExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code primaryExpr}
	 * labeled alternative in {@link JinjaParser#unary}.
	 * @param ctx the parse tree
	 */
	void exitPrimaryExpr(JinjaParser.PrimaryExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code parenExpr}
	 * labeled alternative in {@link JinjaParser#primary}.
	 * @param ctx the parse tree
	 */
	void enterParenExpr(JinjaParser.ParenExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code parenExpr}
	 * labeled alternative in {@link JinjaParser#primary}.
	 * @param ctx the parse tree
	 */
	void exitParenExpr(JinjaParser.ParenExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code literalExpr}
	 * labeled alternative in {@link JinjaParser#primary}.
	 * @param ctx the parse tree
	 */
	void enterLiteralExpr(JinjaParser.LiteralExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code literalExpr}
	 * labeled alternative in {@link JinjaParser#primary}.
	 * @param ctx the parse tree
	 */
	void exitLiteralExpr(JinjaParser.LiteralExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code identifierExpr}
	 * labeled alternative in {@link JinjaParser#primary}.
	 * @param ctx the parse tree
	 */
	void enterIdentifierExpr(JinjaParser.IdentifierExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code identifierExpr}
	 * labeled alternative in {@link JinjaParser#primary}.
	 * @param ctx the parse tree
	 */
	void exitIdentifierExpr(JinjaParser.IdentifierExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code stringLiteral}
	 * labeled alternative in {@link JinjaParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterStringLiteral(JinjaParser.StringLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stringLiteral}
	 * labeled alternative in {@link JinjaParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitStringLiteral(JinjaParser.StringLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code intLiteral}
	 * labeled alternative in {@link JinjaParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterIntLiteral(JinjaParser.IntLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code intLiteral}
	 * labeled alternative in {@link JinjaParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitIntLiteral(JinjaParser.IntLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code floatLiteral}
	 * labeled alternative in {@link JinjaParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterFloatLiteral(JinjaParser.FloatLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code floatLiteral}
	 * labeled alternative in {@link JinjaParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitFloatLiteral(JinjaParser.FloatLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code trueLiteral}
	 * labeled alternative in {@link JinjaParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterTrueLiteral(JinjaParser.TrueLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code trueLiteral}
	 * labeled alternative in {@link JinjaParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitTrueLiteral(JinjaParser.TrueLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code falseLiteral}
	 * labeled alternative in {@link JinjaParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterFalseLiteral(JinjaParser.FalseLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code falseLiteral}
	 * labeled alternative in {@link JinjaParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitFalseLiteral(JinjaParser.FalseLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code noneLiteral}
	 * labeled alternative in {@link JinjaParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterNoneLiteral(JinjaParser.NoneLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code noneLiteral}
	 * labeled alternative in {@link JinjaParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitNoneLiteral(JinjaParser.NoneLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code attributeAccess}
	 * labeled alternative in {@link JinjaParser#trailer}.
	 * @param ctx the parse tree
	 */
	void enterAttributeAccess(JinjaParser.AttributeAccessContext ctx);
	/**
	 * Exit a parse tree produced by the {@code attributeAccess}
	 * labeled alternative in {@link JinjaParser#trailer}.
	 * @param ctx the parse tree
	 */
	void exitAttributeAccess(JinjaParser.AttributeAccessContext ctx);
	/**
	 * Enter a parse tree produced by the {@code indexAccess}
	 * labeled alternative in {@link JinjaParser#trailer}.
	 * @param ctx the parse tree
	 */
	void enterIndexAccess(JinjaParser.IndexAccessContext ctx);
	/**
	 * Exit a parse tree produced by the {@code indexAccess}
	 * labeled alternative in {@link JinjaParser#trailer}.
	 * @param ctx the parse tree
	 */
	void exitIndexAccess(JinjaParser.IndexAccessContext ctx);
	/**
	 * Enter a parse tree produced by the {@code callTrailer}
	 * labeled alternative in {@link JinjaParser#trailer}.
	 * @param ctx the parse tree
	 */
	void enterCallTrailer(JinjaParser.CallTrailerContext ctx);
	/**
	 * Exit a parse tree produced by the {@code callTrailer}
	 * labeled alternative in {@link JinjaParser#trailer}.
	 * @param ctx the parse tree
	 */
	void exitCallTrailer(JinjaParser.CallTrailerContext ctx);
	/**
	 * Enter a parse tree produced by {@link JinjaParser#argList}.
	 * @param ctx the parse tree
	 */
	void enterArgList(JinjaParser.ArgListContext ctx);
	/**
	 * Exit a parse tree produced by {@link JinjaParser#argList}.
	 * @param ctx the parse tree
	 */
	void exitArgList(JinjaParser.ArgListContext ctx);
}