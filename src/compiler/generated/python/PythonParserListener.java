// Generated from D:/Compiler/PyJinjaCompiler/src/compiler.grammar/python/PythonParser.g4 by ANTLR 4.13.2
package compiler.generated.python;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link PythonParser}.
 */
public interface PythonParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link PythonParser#program}.
	 * @param ctx the parse tree
	 */
	void enterProgram(PythonParser.ProgramContext ctx);
	/**
	 * Exit a parse tree produced by {@link PythonParser#program}.
	 * @param ctx the parse tree
	 */
	void exitProgram(PythonParser.ProgramContext ctx);
	/**
	 * Enter a parse tree produced by the {@code returnStatement}
	 * labeled alternative in {@link PythonParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterReturnStatement(PythonParser.ReturnStatementContext ctx);
	/**
	 * Exit a parse tree produced by the {@code returnStatement}
	 * labeled alternative in {@link PythonParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitReturnStatement(PythonParser.ReturnStatementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code assignOrExprStatement}
	 * labeled alternative in {@link PythonParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterAssignOrExprStatement(PythonParser.AssignOrExprStatementContext ctx);
	/**
	 * Exit a parse tree produced by the {@code assignOrExprStatement}
	 * labeled alternative in {@link PythonParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitAssignOrExprStatement(PythonParser.AssignOrExprStatementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code functionDef}
	 * labeled alternative in {@link PythonParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterFunctionDef(PythonParser.FunctionDefContext ctx);
	/**
	 * Exit a parse tree produced by the {@code functionDef}
	 * labeled alternative in {@link PythonParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitFunctionDef(PythonParser.FunctionDefContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ifStatement}
	 * labeled alternative in {@link PythonParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterIfStatement(PythonParser.IfStatementContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ifStatement}
	 * labeled alternative in {@link PythonParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitIfStatement(PythonParser.IfStatementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code whileStatement}
	 * labeled alternative in {@link PythonParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterWhileStatement(PythonParser.WhileStatementContext ctx);
	/**
	 * Exit a parse tree produced by the {@code whileStatement}
	 * labeled alternative in {@link PythonParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitWhileStatement(PythonParser.WhileStatementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code forStatement}
	 * labeled alternative in {@link PythonParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterForStatement(PythonParser.ForStatementContext ctx);
	/**
	 * Exit a parse tree produced by the {@code forStatement}
	 * labeled alternative in {@link PythonParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitForStatement(PythonParser.ForStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link PythonParser#decorator}.
	 * @param ctx the parse tree
	 */
	void enterDecorator(PythonParser.DecoratorContext ctx);
	/**
	 * Exit a parse tree produced by {@link PythonParser#decorator}.
	 * @param ctx the parse tree
	 */
	void exitDecorator(PythonParser.DecoratorContext ctx);
	/**
	 * Enter a parse tree produced by {@link PythonParser#paramList}.
	 * @param ctx the parse tree
	 */
	void enterParamList(PythonParser.ParamListContext ctx);
	/**
	 * Exit a parse tree produced by {@link PythonParser#paramList}.
	 * @param ctx the parse tree
	 */
	void exitParamList(PythonParser.ParamListContext ctx);
	/**
	 * Enter a parse tree produced by the {@code singleSuite}
	 * labeled alternative in {@link PythonParser#suite}.
	 * @param ctx the parse tree
	 */
	void enterSingleSuite(PythonParser.SingleSuiteContext ctx);
	/**
	 * Exit a parse tree produced by the {@code singleSuite}
	 * labeled alternative in {@link PythonParser#suite}.
	 * @param ctx the parse tree
	 */
	void exitSingleSuite(PythonParser.SingleSuiteContext ctx);
	/**
	 * Enter a parse tree produced by the {@code indentedSuite}
	 * labeled alternative in {@link PythonParser#suite}.
	 * @param ctx the parse tree
	 */
	void enterIndentedSuite(PythonParser.IndentedSuiteContext ctx);
	/**
	 * Exit a parse tree produced by the {@code indentedSuite}
	 * labeled alternative in {@link PythonParser#suite}.
	 * @param ctx the parse tree
	 */
	void exitIndentedSuite(PythonParser.IndentedSuiteContext ctx);
	/**
	 * Enter a parse tree produced by {@link PythonParser#simpleLine}.
	 * @param ctx the parse tree
	 */
	void enterSimpleLine(PythonParser.SimpleLineContext ctx);
	/**
	 * Exit a parse tree produced by {@link PythonParser#simpleLine}.
	 * @param ctx the parse tree
	 */
	void exitSimpleLine(PythonParser.SimpleLineContext ctx);
	/**
	 * Enter a parse tree produced by {@link PythonParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterExpr(PythonParser.ExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link PythonParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitExpr(PythonParser.ExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalAndExpr}
	 * labeled alternative in {@link PythonParser#logicalOr}.
	 * @param ctx the parse tree
	 */
	void enterLogicalAndExpr(PythonParser.LogicalAndExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalAndExpr}
	 * labeled alternative in {@link PythonParser#logicalOr}.
	 * @param ctx the parse tree
	 */
	void exitLogicalAndExpr(PythonParser.LogicalAndExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code orExpr}
	 * labeled alternative in {@link PythonParser#logicalOr}.
	 * @param ctx the parse tree
	 */
	void enterOrExpr(PythonParser.OrExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code orExpr}
	 * labeled alternative in {@link PythonParser#logicalOr}.
	 * @param ctx the parse tree
	 */
	void exitOrExpr(PythonParser.OrExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalAndComparisonExpr}
	 * labeled alternative in {@link PythonParser#logicalAnd}.
	 * @param ctx the parse tree
	 */
	void enterLogicalAndComparisonExpr(PythonParser.LogicalAndComparisonExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalAndComparisonExpr}
	 * labeled alternative in {@link PythonParser#logicalAnd}.
	 * @param ctx the parse tree
	 */
	void exitLogicalAndComparisonExpr(PythonParser.LogicalAndComparisonExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code andExpr}
	 * labeled alternative in {@link PythonParser#logicalAnd}.
	 * @param ctx the parse tree
	 */
	void enterAndExpr(PythonParser.AndExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code andExpr}
	 * labeled alternative in {@link PythonParser#logicalAnd}.
	 * @param ctx the parse tree
	 */
	void exitAndExpr(PythonParser.AndExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code comparisonAdditiveExpr}
	 * labeled alternative in {@link PythonParser#comparison}.
	 * @param ctx the parse tree
	 */
	void enterComparisonAdditiveExpr(PythonParser.ComparisonAdditiveExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code comparisonAdditiveExpr}
	 * labeled alternative in {@link PythonParser#comparison}.
	 * @param ctx the parse tree
	 */
	void exitComparisonAdditiveExpr(PythonParser.ComparisonAdditiveExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code comparisonOpExpr}
	 * labeled alternative in {@link PythonParser#comparison}.
	 * @param ctx the parse tree
	 */
	void enterComparisonOpExpr(PythonParser.ComparisonOpExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code comparisonOpExpr}
	 * labeled alternative in {@link PythonParser#comparison}.
	 * @param ctx the parse tree
	 */
	void exitComparisonOpExpr(PythonParser.ComparisonOpExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code addExpr}
	 * labeled alternative in {@link PythonParser#additive}.
	 * @param ctx the parse tree
	 */
	void enterAddExpr(PythonParser.AddExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code addExpr}
	 * labeled alternative in {@link PythonParser#additive}.
	 * @param ctx the parse tree
	 */
	void exitAddExpr(PythonParser.AddExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code additiveMultiplicativeExpr}
	 * labeled alternative in {@link PythonParser#additive}.
	 * @param ctx the parse tree
	 */
	void enterAdditiveMultiplicativeExpr(PythonParser.AdditiveMultiplicativeExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code additiveMultiplicativeExpr}
	 * labeled alternative in {@link PythonParser#additive}.
	 * @param ctx the parse tree
	 */
	void exitAdditiveMultiplicativeExpr(PythonParser.AdditiveMultiplicativeExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code multiplicativeUnaryExpr}
	 * labeled alternative in {@link PythonParser#multiplicative}.
	 * @param ctx the parse tree
	 */
	void enterMultiplicativeUnaryExpr(PythonParser.MultiplicativeUnaryExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code multiplicativeUnaryExpr}
	 * labeled alternative in {@link PythonParser#multiplicative}.
	 * @param ctx the parse tree
	 */
	void exitMultiplicativeUnaryExpr(PythonParser.MultiplicativeUnaryExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code mulExpr}
	 * labeled alternative in {@link PythonParser#multiplicative}.
	 * @param ctx the parse tree
	 */
	void enterMulExpr(PythonParser.MulExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code mulExpr}
	 * labeled alternative in {@link PythonParser#multiplicative}.
	 * @param ctx the parse tree
	 */
	void exitMulExpr(PythonParser.MulExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code notExpr}
	 * labeled alternative in {@link PythonParser#unary}.
	 * @param ctx the parse tree
	 */
	void enterNotExpr(PythonParser.NotExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code notExpr}
	 * labeled alternative in {@link PythonParser#unary}.
	 * @param ctx the parse tree
	 */
	void exitNotExpr(PythonParser.NotExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code unaryMinus}
	 * labeled alternative in {@link PythonParser#unary}.
	 * @param ctx the parse tree
	 */
	void enterUnaryMinus(PythonParser.UnaryMinusContext ctx);
	/**
	 * Exit a parse tree produced by the {@code unaryMinus}
	 * labeled alternative in {@link PythonParser#unary}.
	 * @param ctx the parse tree
	 */
	void exitUnaryMinus(PythonParser.UnaryMinusContext ctx);
	/**
	 * Enter a parse tree produced by the {@code primaryExpr}
	 * labeled alternative in {@link PythonParser#unary}.
	 * @param ctx the parse tree
	 */
	void enterPrimaryExpr(PythonParser.PrimaryExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code primaryExpr}
	 * labeled alternative in {@link PythonParser#unary}.
	 * @param ctx the parse tree
	 */
	void exitPrimaryExpr(PythonParser.PrimaryExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code parenExpr}
	 * labeled alternative in {@link PythonParser#primary}.
	 * @param ctx the parse tree
	 */
	void enterParenExpr(PythonParser.ParenExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code parenExpr}
	 * labeled alternative in {@link PythonParser#primary}.
	 * @param ctx the parse tree
	 */
	void exitParenExpr(PythonParser.ParenExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code atomExpr}
	 * labeled alternative in {@link PythonParser#primary}.
	 * @param ctx the parse tree
	 */
	void enterAtomExpr(PythonParser.AtomExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code atomExpr}
	 * labeled alternative in {@link PythonParser#primary}.
	 * @param ctx the parse tree
	 */
	void exitAtomExpr(PythonParser.AtomExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link PythonParser#listItems}.
	 * @param ctx the parse tree
	 */
	void enterListItems(PythonParser.ListItemsContext ctx);
	/**
	 * Exit a parse tree produced by {@link PythonParser#listItems}.
	 * @param ctx the parse tree
	 */
	void exitListItems(PythonParser.ListItemsContext ctx);
	/**
	 * Enter a parse tree produced by the {@code listExpr}
	 * labeled alternative in {@link PythonParser#atom}.
	 * @param ctx the parse tree
	 */
	void enterListExpr(PythonParser.ListExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code listExpr}
	 * labeled alternative in {@link PythonParser#atom}.
	 * @param ctx the parse tree
	 */
	void exitListExpr(PythonParser.ListExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dictExpr}
	 * labeled alternative in {@link PythonParser#atom}.
	 * @param ctx the parse tree
	 */
	void enterDictExpr(PythonParser.DictExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dictExpr}
	 * labeled alternative in {@link PythonParser#atom}.
	 * @param ctx the parse tree
	 */
	void exitDictExpr(PythonParser.DictExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code stringLiteral}
	 * labeled alternative in {@link PythonParser#atom}.
	 * @param ctx the parse tree
	 */
	void enterStringLiteral(PythonParser.StringLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stringLiteral}
	 * labeled alternative in {@link PythonParser#atom}.
	 * @param ctx the parse tree
	 */
	void exitStringLiteral(PythonParser.StringLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code intLiteral}
	 * labeled alternative in {@link PythonParser#atom}.
	 * @param ctx the parse tree
	 */
	void enterIntLiteral(PythonParser.IntLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code intLiteral}
	 * labeled alternative in {@link PythonParser#atom}.
	 * @param ctx the parse tree
	 */
	void exitIntLiteral(PythonParser.IntLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code floatLiteral}
	 * labeled alternative in {@link PythonParser#atom}.
	 * @param ctx the parse tree
	 */
	void enterFloatLiteral(PythonParser.FloatLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code floatLiteral}
	 * labeled alternative in {@link PythonParser#atom}.
	 * @param ctx the parse tree
	 */
	void exitFloatLiteral(PythonParser.FloatLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code trueLiteral}
	 * labeled alternative in {@link PythonParser#atom}.
	 * @param ctx the parse tree
	 */
	void enterTrueLiteral(PythonParser.TrueLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code trueLiteral}
	 * labeled alternative in {@link PythonParser#atom}.
	 * @param ctx the parse tree
	 */
	void exitTrueLiteral(PythonParser.TrueLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code falseLiteral}
	 * labeled alternative in {@link PythonParser#atom}.
	 * @param ctx the parse tree
	 */
	void enterFalseLiteral(PythonParser.FalseLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code falseLiteral}
	 * labeled alternative in {@link PythonParser#atom}.
	 * @param ctx the parse tree
	 */
	void exitFalseLiteral(PythonParser.FalseLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code noneLiteral}
	 * labeled alternative in {@link PythonParser#atom}.
	 * @param ctx the parse tree
	 */
	void enterNoneLiteral(PythonParser.NoneLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code noneLiteral}
	 * labeled alternative in {@link PythonParser#atom}.
	 * @param ctx the parse tree
	 */
	void exitNoneLiteral(PythonParser.NoneLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code identifierAtom}
	 * labeled alternative in {@link PythonParser#atom}.
	 * @param ctx the parse tree
	 */
	void enterIdentifierAtom(PythonParser.IdentifierAtomContext ctx);
	/**
	 * Exit a parse tree produced by the {@code identifierAtom}
	 * labeled alternative in {@link PythonParser#atom}.
	 * @param ctx the parse tree
	 */
	void exitIdentifierAtom(PythonParser.IdentifierAtomContext ctx);
	/**
	 * Enter a parse tree produced by the {@code attributeAccess}
	 * labeled alternative in {@link PythonParser#atomTrailer}.
	 * @param ctx the parse tree
	 */
	void enterAttributeAccess(PythonParser.AttributeAccessContext ctx);
	/**
	 * Exit a parse tree produced by the {@code attributeAccess}
	 * labeled alternative in {@link PythonParser#atomTrailer}.
	 * @param ctx the parse tree
	 */
	void exitAttributeAccess(PythonParser.AttributeAccessContext ctx);
	/**
	 * Enter a parse tree produced by the {@code indexAccess}
	 * labeled alternative in {@link PythonParser#atomTrailer}.
	 * @param ctx the parse tree
	 */
	void enterIndexAccess(PythonParser.IndexAccessContext ctx);
	/**
	 * Exit a parse tree produced by the {@code indexAccess}
	 * labeled alternative in {@link PythonParser#atomTrailer}.
	 * @param ctx the parse tree
	 */
	void exitIndexAccess(PythonParser.IndexAccessContext ctx);
	/**
	 * Enter a parse tree produced by the {@code callTrailer}
	 * labeled alternative in {@link PythonParser#atomTrailer}.
	 * @param ctx the parse tree
	 */
	void enterCallTrailer(PythonParser.CallTrailerContext ctx);
	/**
	 * Exit a parse tree produced by the {@code callTrailer}
	 * labeled alternative in {@link PythonParser#atomTrailer}.
	 * @param ctx the parse tree
	 */
	void exitCallTrailer(PythonParser.CallTrailerContext ctx);
	/**
	 * Enter a parse tree produced by {@link PythonParser#argList}.
	 * @param ctx the parse tree
	 */
	void enterArgList(PythonParser.ArgListContext ctx);
	/**
	 * Exit a parse tree produced by {@link PythonParser#argList}.
	 * @param ctx the parse tree
	 */
	void exitArgList(PythonParser.ArgListContext ctx);
	/**
	 * Enter a parse tree produced by the {@code positionalArg}
	 * labeled alternative in {@link PythonParser#argument}.
	 * @param ctx the parse tree
	 */
	void enterPositionalArg(PythonParser.PositionalArgContext ctx);
	/**
	 * Exit a parse tree produced by the {@code positionalArg}
	 * labeled alternative in {@link PythonParser#argument}.
	 * @param ctx the parse tree
	 */
	void exitPositionalArg(PythonParser.PositionalArgContext ctx);
	/**
	 * Enter a parse tree produced by the {@code keywordArg}
	 * labeled alternative in {@link PythonParser#argument}.
	 * @param ctx the parse tree
	 */
	void enterKeywordArg(PythonParser.KeywordArgContext ctx);
	/**
	 * Exit a parse tree produced by the {@code keywordArg}
	 * labeled alternative in {@link PythonParser#argument}.
	 * @param ctx the parse tree
	 */
	void exitKeywordArg(PythonParser.KeywordArgContext ctx);
	/**
	 * Enter a parse tree produced by {@link PythonParser#dictItems}.
	 * @param ctx the parse tree
	 */
	void enterDictItems(PythonParser.DictItemsContext ctx);
	/**
	 * Exit a parse tree produced by {@link PythonParser#dictItems}.
	 * @param ctx the parse tree
	 */
	void exitDictItems(PythonParser.DictItemsContext ctx);
	/**
	 * Enter a parse tree produced by {@link PythonParser#dictItem}.
	 * @param ctx the parse tree
	 */
	void enterDictItem(PythonParser.DictItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link PythonParser#dictItem}.
	 * @param ctx the parse tree
	 */
	void exitDictItem(PythonParser.DictItemContext ctx);
}