package compiler.ast.common;

import compiler.ast.python.*;
import compiler.ast.jinja.*;

/**
 * Generic visitor for the entire compiler AST.
 * Supports both Python-specific and common expression nodes, plus
 * Jinja-specific template nodes.
 */
public interface AstVisitor<T> {

    // Common expression nodes
    T visitIdentifier(Identifier node);

    T visitIntegerLiteral(IntegerLiteral node);

    T visitFloatLiteral(FloatLiteral node);

    T visitStringLiteral(StringLiteral node);

    T visitBooleanLiteral(BooleanLiteral node);

    T visitNoneLiteral(NoneLiteral node);

    T visitBinaryExpr(BinaryExpr node);

    T visitUnaryExpr(UnaryExpr node);

    T visitCallExpr(CallExpr node);

    T visitAttributeAccess(AttributeAccess node);

    T visitIndexAccess(IndexAccess node);

    T visitPositionalArgument(PositionalArgument node);

    T visitKeywordArgument(KeywordArgument node);

    // Python-specific nodes
    T visitProgram(Program node);

    T visitListExpr(ListExpr node);

    T visitDictExpr(DictExpr node);

    T visitDictEntry(DictEntry node);

    T visitForStmt(ForStmt node);

    T visitAssign(Assign node);

    T visitExprStmt (ExprStmt node);

    T visitElifClause(ElifClause node);

    T visitReturnStmt(ReturnStmt node);

    T visitFunctionParameter(FunctionParameter node);

    T visitDecorator(Decorator node);

    T visitIfStmt(IfStmt node);

    T visitElseClause(ElseClause node);

    T visitWhileStmt(WhileStmt node);

    T visitFunctionDef(FunctionDef node);

    // Jinja-specific nodes
    T visitTemplate(Template node);

    T visitHtmlText(HtmlText node);

    T visitExpressionOutput(ExpressionOutput node);

    T visitJinjaIfStmt(JinjaIfStmt node);

    T visitJinjaElifClause(JinjaElifClause node);

    T visitJinjaElseClause(JinjaElseClause node);

    T visitJinjaForStmt(JinjaForStmt node);
}
