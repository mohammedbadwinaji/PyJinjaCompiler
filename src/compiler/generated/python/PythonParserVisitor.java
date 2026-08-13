// Generated from D:/Compiler/PyJinjaCompiler/src/grammar/python/PythonParser.g4 by ANTLR 4.13.2
package compiler.generated.python;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link PythonParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface PythonParserVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link PythonParser#program}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProgram(PythonParser.ProgramContext ctx);
	/**
	 * Visit a parse tree produced by the {@code returnStatement}
	 * labeled alternative in {@link PythonParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReturnStatement(PythonParser.ReturnStatementContext ctx);
	/**
	 * Visit a parse tree produced by the {@code assignOrExprStatement}
	 * labeled alternative in {@link PythonParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignOrExprStatement(PythonParser.AssignOrExprStatementContext ctx);
	/**
	 * Visit a parse tree produced by the {@code functionDef}
	 * labeled alternative in {@link PythonParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionDef(PythonParser.FunctionDefContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ifStatement}
	 * labeled alternative in {@link PythonParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIfStatement(PythonParser.IfStatementContext ctx);
	/**
	 * Visit a parse tree produced by the {@code whileStatement}
	 * labeled alternative in {@link PythonParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhileStatement(PythonParser.WhileStatementContext ctx);
	/**
	 * Visit a parse tree produced by the {@code forStatement}
	 * labeled alternative in {@link PythonParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForStatement(PythonParser.ForStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link PythonParser#decorator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDecorator(PythonParser.DecoratorContext ctx);
	/**
	 * Visit a parse tree produced by {@link PythonParser#paramList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParamList(PythonParser.ParamListContext ctx);
	/**
	 * Visit a parse tree produced by the {@code singleSuite}
	 * labeled alternative in {@link PythonParser#suite}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSingleSuite(PythonParser.SingleSuiteContext ctx);
	/**
	 * Visit a parse tree produced by the {@code indentedSuite}
	 * labeled alternative in {@link PythonParser#suite}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndentedSuite(PythonParser.IndentedSuiteContext ctx);
	/**
	 * Visit a parse tree produced by {@link PythonParser#simpleLine}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimpleLine(PythonParser.SimpleLineContext ctx);
	/**
	 * Visit a parse tree produced by {@link PythonParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpr(PythonParser.ExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicalAndExpr}
	 * labeled alternative in {@link PythonParser#logicalOr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalAndExpr(PythonParser.LogicalAndExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code orExpr}
	 * labeled alternative in {@link PythonParser#logicalOr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrExpr(PythonParser.OrExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicalAndComparisonExpr}
	 * labeled alternative in {@link PythonParser#logicalAnd}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalAndComparisonExpr(PythonParser.LogicalAndComparisonExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code andExpr}
	 * labeled alternative in {@link PythonParser#logicalAnd}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAndExpr(PythonParser.AndExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code comparisonAdditiveExpr}
	 * labeled alternative in {@link PythonParser#comparison}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparisonAdditiveExpr(PythonParser.ComparisonAdditiveExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code comparisonOpExpr}
	 * labeled alternative in {@link PythonParser#comparison}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparisonOpExpr(PythonParser.ComparisonOpExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code addExpr}
	 * labeled alternative in {@link PythonParser#additive}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAddExpr(PythonParser.AddExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code additiveMultiplicativeExpr}
	 * labeled alternative in {@link PythonParser#additive}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAdditiveMultiplicativeExpr(PythonParser.AdditiveMultiplicativeExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code multiplicativeUnaryExpr}
	 * labeled alternative in {@link PythonParser#multiplicative}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultiplicativeUnaryExpr(PythonParser.MultiplicativeUnaryExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code mulExpr}
	 * labeled alternative in {@link PythonParser#multiplicative}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMulExpr(PythonParser.MulExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code notExpr}
	 * labeled alternative in {@link PythonParser#unary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNotExpr(PythonParser.NotExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code unaryMinus}
	 * labeled alternative in {@link PythonParser#unary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnaryMinus(PythonParser.UnaryMinusContext ctx);
	/**
	 * Visit a parse tree produced by the {@code primaryExpr}
	 * labeled alternative in {@link PythonParser#unary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimaryExpr(PythonParser.PrimaryExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code parenExpr}
	 * labeled alternative in {@link PythonParser#primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParenExpr(PythonParser.ParenExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code atomExpr}
	 * labeled alternative in {@link PythonParser#primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAtomExpr(PythonParser.AtomExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link PythonParser#listItems}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitListItems(PythonParser.ListItemsContext ctx);
	/**
	 * Visit a parse tree produced by the {@code listExpr}
	 * labeled alternative in {@link PythonParser#atom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitListExpr(PythonParser.ListExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dictExpr}
	 * labeled alternative in {@link PythonParser#atom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDictExpr(PythonParser.DictExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code stringLiteral}
	 * labeled alternative in {@link PythonParser#atom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringLiteral(PythonParser.StringLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code intLiteral}
	 * labeled alternative in {@link PythonParser#atom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntLiteral(PythonParser.IntLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code floatLiteral}
	 * labeled alternative in {@link PythonParser#atom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFloatLiteral(PythonParser.FloatLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code trueLiteral}
	 * labeled alternative in {@link PythonParser#atom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTrueLiteral(PythonParser.TrueLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code falseLiteral}
	 * labeled alternative in {@link PythonParser#atom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFalseLiteral(PythonParser.FalseLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code noneLiteral}
	 * labeled alternative in {@link PythonParser#atom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNoneLiteral(PythonParser.NoneLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code identifierAtom}
	 * labeled alternative in {@link PythonParser#atom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifierAtom(PythonParser.IdentifierAtomContext ctx);
	/**
	 * Visit a parse tree produced by the {@code attributeAccess}
	 * labeled alternative in {@link PythonParser#atomTrailer}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttributeAccess(PythonParser.AttributeAccessContext ctx);
	/**
	 * Visit a parse tree produced by the {@code indexAccess}
	 * labeled alternative in {@link PythonParser#atomTrailer}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndexAccess(PythonParser.IndexAccessContext ctx);
	/**
	 * Visit a parse tree produced by the {@code callTrailer}
	 * labeled alternative in {@link PythonParser#atomTrailer}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCallTrailer(PythonParser.CallTrailerContext ctx);
	/**
	 * Visit a parse tree produced by {@link PythonParser#argList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArgList(PythonParser.ArgListContext ctx);
	/**
	 * Visit a parse tree produced by the {@code positionalArg}
	 * labeled alternative in {@link PythonParser#argument}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPositionalArg(PythonParser.PositionalArgContext ctx);
	/**
	 * Visit a parse tree produced by the {@code keywordArg}
	 * labeled alternative in {@link PythonParser#argument}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKeywordArg(PythonParser.KeywordArgContext ctx);
	/**
	 * Visit a parse tree produced by {@link PythonParser#dictItems}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDictItems(PythonParser.DictItemsContext ctx);
	/**
	 * Visit a parse tree produced by {@link PythonParser#dictItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDictItem(PythonParser.DictItemContext ctx);
}