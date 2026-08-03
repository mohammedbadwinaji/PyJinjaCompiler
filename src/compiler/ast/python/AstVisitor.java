package compiler.ast.python;

/**
 * Generic visitor for Python AST.
 */
public interface AstVisitor<T> {

    T visitProgram(Program node);

    T visitIdentifier(Identifier node);

    T visitIntegerLiteral(IntegerLiteral node);

    T visitFloatLiteral(FloatLiteral node);

    T visitStringLiteral(StringLiteral node);

    T visitBooleanLiteral(BooleanLiteral node);

    T visitNoneLiteral(NoneLiteral node);

    T visitListExpr(ListExpr node);

    T visitDictExpr(DictExpr node);

    T visitDictEntry(DictEntry node);

    T visitPositionalArgument(PositionalArgument node);

    T visitIndexAccess(IndexAccess node);

    T visitKeywordArgument(KeywordArgument node);

    T visitAttributeAccess(AttributeAccess node);

    T visitCallExpr(CallExpr node);

    T visitBinaryExpr(BinaryExpr node);

    T visitForStmt(ForStmt node);

    T visitAssign(Assign node);

    T visitExprStmt (ExprStmt node);

    T visitElifClause(ElifClause node);

    T visitReturnStmt(ReturnStmt node);

    T visitFunctionParameter(FunctionParameter node);

    T visitDecorator(Decorator node);

    T visitIfStmt(IfStmt node);

    T visitFunctionDef(FunctionDef node);
}