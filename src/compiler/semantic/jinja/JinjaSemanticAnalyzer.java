package compiler.semantic.jinja;

import compiler.ast.jinja.*;
import compiler.ast.common.*;
import compiler.ast.python.*;

import compiler.semantic.common.Scope;
import compiler.semantic.common.SemanticError;
import compiler.semantic.common.Symbol;
import compiler.semantic.common.SymbolTable;
import compiler.semantic.common.Type;

import java.util.ArrayList;

import java.util.List;



public class JinjaSemanticAnalyzer implements AstVisitor<Void> {

    private final SymbolTable symbolTable;
    private final List<SemanticError> errors;


    public JinjaSemanticAnalyzer(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.errors = new ArrayList<>();
    }





    public List<SemanticError> analyze(Template template) {
        template.accept(this);
        return new ArrayList<>(errors);
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    /* -------------------------
       Jinja-tree nodes
       ------------------------- */

    @Override
    public Void visitTemplate(Template node) {
        for (TemplateElement element : node.getElements()) {
            element.accept(this);
        }
        return null;
    }

    @Override
    public Void visitHtmlText(HtmlText node) {
        // Raw text - nothing to check.
        return null;
    }

    @Override
    public Void visitExpressionOutput(ExpressionOutput node) {
        node.getExpression().accept(this);
        return null;
    }

    @Override
    public Void visitJinjaIfStmt(JinjaIfStmt node) {
        node.getCondition().accept(this);

        symbolTable.enterScope(Scope.ScopeKind.BLOCK);
        for (TemplateElement element : node.getThenBody()) {
            element.accept(this);
        }
        symbolTable.exitScope();

        for (JinjaElifClause elif : node.getElifClauses()) {
            elif.accept(this);
        }

        if (node.getElseClause() != null) {
            node.getElseClause().accept(this);
        }

        return null;
    }

    @Override
    public Void visitJinjaElifClause(JinjaElifClause node) {
        node.getCondition().accept(this);

        symbolTable.enterScope(Scope.ScopeKind.BLOCK);
        for (TemplateElement element : node.getBody()) {
            element.accept(this);
        }
        symbolTable.exitScope();

        return null;
    }

    @Override
    public Void visitJinjaElseClause(JinjaElseClause node) {
        symbolTable.enterScope(Scope.ScopeKind.BLOCK);
        for (TemplateElement element : node.getBody()) {
            element.accept(this);
        }
        symbolTable.exitScope();

        return null;
    }

    @Override
    public Void visitJinjaForStmt(JinjaForStmt node) {
        // Check if the iterable is actually iterable (visit it first so
        // undefined-variable errors inside the iterable expression are
        // still reported even when the type can't be inferred).
        node.getIterable().accept(this);

        Type iterableType = inferType(node.getIterable());
        if (iterableType != Type.UNKNOWN &&
            iterableType != Type.LIST &&
            iterableType != Type.STRING &&
            iterableType != Type.DICTIONARY) {
            errors.add(new SemanticError(
                node.getLine(),
                SemanticError.ErrorType.NON_ITERABLE_IN_FOR,
                "Non-iterable type '" + iterableType + "' used in for loop"
            ));
        }

        symbolTable.enterScope(Scope.ScopeKind.BLOCK);

        Type elementType = Type.UNKNOWN;

        switch (iterableType) {

            case LIST:
                elementType = Type.UNKNOWN;
                break;

            case STRING:
                elementType = Type.STRING;
                break;

            case DICTIONARY:
                elementType = Type.STRING;
                break;

            default:
                elementType = Type.UNKNOWN;
        }

        Symbol loopVar = new Symbol(
                node.getVariable().getName(),
                Symbol.Kind.VARIABLE,
                node.getLine()
        );

        loopVar.setInferredType(elementType);

        symbolTable.addSymbol(loopVar);

        for (TemplateElement element : node.getBody()) {
            element.accept(this);
        }

        symbolTable.exitScope();

        return null;
    }

    /* -------------------------
       Common expression nodes
       ------------------------- */

    @Override
    public Void visitIdentifier(Identifier node) {
        Symbol symbol = symbolTable.lookup(node.getName());
        if (symbol == null) {
            errors.add(new SemanticError(
                node.getLine(),
                SemanticError.ErrorType.UNDEFINED_VARIABLE,
                "Undefined variable '" + node.getName() + "'"
            ));
        }
        return null;
    }
    private boolean isBinaryOperationValid(
            BinaryExpr node,
            Type left,
            Type right) {

        switch (node.getOperator()) {

            case ADD:

                if (left == Type.STRING && right == Type.STRING)
                    return true;

                if (left == Type.INTEGER && right == Type.INTEGER)
                    return true;

                if (left == Type.FLOAT && right == Type.FLOAT)
                    return true;

                if ((left == Type.INTEGER && right == Type.FLOAT) ||
                        (left == Type.FLOAT && right == Type.INTEGER))
                    return true;

                return false;

            case SUBTRACT:
            case MULTIPLY:
            case DIVIDE:
            case MODULO:

                return (left == Type.INTEGER || left == Type.FLOAT)
                        &&
                        (right == Type.INTEGER || right == Type.FLOAT);

            case EQ:
            case NE:

                return true;

            case LT:
            case GT:
            case LE:
            case GE:

                return left == right;

            case AND:
            case OR:

                return left == Type.BOOLEAN
                        &&
                        right == Type.BOOLEAN;

            default:
                return true;
        }
    }

    @Override
    public Void visitBinaryExpr(BinaryExpr node) {

        node.getLeft().accept(this);
        node.getRight().accept(this);

        Type leftType = inferType(node.getLeft());
        Type rightType = inferType(node.getRight());

        if (leftType == Type.UNKNOWN || rightType == Type.UNKNOWN) {
            return null;
        }

        if (!isBinaryOperationValid(node, leftType, rightType)) {

            errors.add(new SemanticError(
                    node.getLine(),
                    SemanticError.ErrorType.TYPE_MISMATCH,
                    "Invalid operation: " +
                            leftType +
                            " " +
                            node.getOperator() +
                            " " +
                            rightType
            ));
        }

        return null;
    }

    @Override
    public Void visitUnaryExpr(UnaryExpr node) {
        node.getExpr().accept(this);
        return null;
    }

    @Override
    public Void visitCallExpr(CallExpr node) {
        String functionName = null;
        Symbol calleeSymbol = null;

        if (node.getCallee() instanceof Identifier) {
            functionName = ((Identifier) node.getCallee()).getName();
            calleeSymbol = symbolTable.lookup(functionName);
        } else {
            // e.g. calling the result of an attribute/index access -
            // just walk it for undefined-variable checks.
            node.getCallee().accept(this);
        }

        if (functionName != null && calleeSymbol == null) {
            errors.add(new SemanticError(
                node.getLine(),
                SemanticError.ErrorType.UNDEFINED_FUNCTION,
                "Call to undefined function '" + functionName + "'"
            ));
        } else if (calleeSymbol != null && calleeSymbol.getInferredType() != Type.FUNCTION) {
            errors.add(new SemanticError(
                node.getLine(),
                SemanticError.ErrorType.NOT_CALLABLE,
                "'" + functionName + "' is not callable"
            ));
        }

        for (Argument arg : node.getArguments()) {
            arg.accept(this);
        }

        return null;
    }

    @Override
    public Void visitAttributeAccess(AttributeAccess node) {

        node.getTarget().accept(this);

        Type targetType = inferType(node.getTarget());

        if (targetType == Type.UNKNOWN) {

            errors.add(new SemanticError(
                    node.getLine(),
                    SemanticError.ErrorType.TYPE_MISMATCH,
                    "Cannot access attribute '" +
                            node.getAttribute() +
                            "' of unknown type"));
        }

        return null;
    }

    @Override
    public Void visitIndexAccess(IndexAccess node) {

        node.getTarget().accept(this);
        node.getIndex().accept(this);

        Type target = inferType(node.getTarget());

        if (target != Type.LIST &&
                target != Type.STRING &&
                target != Type.DICTIONARY &&
                target != Type.UNKNOWN) {

            errors.add(new SemanticError(
                    node.getLine(),
                    SemanticError.ErrorType.TYPE_MISMATCH,
                    "Cannot index value of type " + target));
        }

        return null;
    }

    @Override
    public Void visitPositionalArgument(PositionalArgument node) {
        node.getValue().accept(this);
        return null;
    }

    @Override
    public Void visitKeywordArgument(KeywordArgument node) {
        // Not produced by the Jinja grammar (argList has no keyword
        // arguments), but implemented for interface completeness.
        node.getValue().accept(this);
        return null;
    }

    @Override
    public Void visitIntegerLiteral(IntegerLiteral node) {
        return null;
    }

    @Override
    public Void visitFloatLiteral(FloatLiteral node) {
        return null;
    }

    @Override
    public Void visitStringLiteral(StringLiteral node) {
        return null;
    }

    @Override
    public Void visitBooleanLiteral(BooleanLiteral node) {
        return null;
    }

    @Override
    public Void visitNoneLiteral(NoneLiteral node) {
        return null;
    }

    /**
     * Infer the type of an expression for type checking (mirrors
     * SemanticAnalyzer#inferType on the Python side).
     */
    private Type inferType(Expression expr) {
        if (expr instanceof IntegerLiteral) {
            return Type.INTEGER;
        } else if (expr instanceof FloatLiteral) {
            return Type.FLOAT;
        } else if (expr instanceof StringLiteral) {
            return Type.STRING;
        } else if (expr instanceof BooleanLiteral) {
            return Type.BOOLEAN;
        } else if (expr instanceof NoneLiteral) {
            return Type.NONE;
        } else if (expr instanceof Identifier) {
            Symbol symbol = symbolTable.lookup(((Identifier) expr).getName());
            if (symbol != null) {
                return symbol.getInferredType();
            }
        } else if (expr instanceof BinaryExpr binary) {

            Type left = inferType(binary.getLeft());
            Type right = inferType(binary.getRight());

            switch (binary.getOperator()) {

                case ADD:

                    if (left == Type.STRING &&
                            right == Type.STRING)
                        return Type.STRING;

                    if (left == Type.FLOAT ||
                            right == Type.FLOAT)
                        return Type.FLOAT;

                    if (left == Type.INTEGER &&
                            right == Type.INTEGER)
                        return Type.INTEGER;

                    return Type.UNKNOWN;

                case SUBTRACT:
                case MULTIPLY:
                case DIVIDE:
                case MODULO:

                    if (left == Type.FLOAT ||
                            right == Type.FLOAT)
                        return Type.FLOAT;

                    if (left == Type.INTEGER &&
                            right == Type.INTEGER)
                        return Type.INTEGER;

                    return Type.UNKNOWN;

                case EQ:
                case NE:
                case LT:
                case GT:
                case LE:
                case GE:
                case AND:
                case OR:

                    return Type.BOOLEAN;

                default:
                    return Type.UNKNOWN;
            }
        } else if (expr instanceof UnaryExpr) {
            return inferType(((UnaryExpr) expr).getExpr());
        }else if (expr instanceof AttributeAccess attribute) {

            Type target = inferType(attribute.getTarget());

            if (target == Type.STRING) {
                return Type.STRING;
            }

            return Type.UNKNOWN;
        }
        else if (expr instanceof IndexAccess index) {

            Type target = inferType(index.getTarget());

            switch (target) {

                case LIST:
                    return Type.UNKNOWN;

                case STRING:
                    return Type.STRING;

                case DICTIONARY:
                    return Type.UNKNOWN;

                default:
                    return Type.UNKNOWN;
            }
        }else if (expr instanceof CallExpr call) {

            if (call.getCallee() instanceof Identifier id) {

                Symbol symbol = symbolTable.lookup(id.getName());

                if (symbol != null) {
                    return symbol.getInferredType();
                }
            }

            return Type.UNKNOWN;
        }
        return Type.UNKNOWN;
    }

    /* -------------------------
       Python-tree nodes
       -------------------------
       Unreachable from a Jinja Template (see the mirror-image comment in
       compiler.semantic.python.SemanticAnalyzer). Implemented only to
       satisfy the shared AstVisitor<T> interface. */

    @Override
    public Void visitProgram(Program node) {
        return null;
    }

    @Override
    public Void visitListExpr(ListExpr node) {
        return null;
    }

    @Override
    public Void visitDictExpr(DictExpr node) {
        return null;
    }

    @Override
    public Void visitDictEntry(DictEntry node) {
        return null;
    }

    @Override
    public Void visitForStmt(ForStmt node) {
        return null;
    }

    @Override
    public Void visitAssign(Assign node) {
        return null;
    }

    @Override
    public Void visitExprStmt(ExprStmt node) {
        return null;
    }

    @Override
    public Void visitElifClause(ElifClause node) {
        return null;
    }

    @Override
    public Void visitReturnStmt(ReturnStmt node) {
        return null;
    }

    @Override
    public Void visitFunctionParameter(FunctionParameter node) {
        return null;
    }

    @Override
    public Void visitDecorator(Decorator node) {
        return null;
    }

    @Override
    public Void visitIfStmt(IfStmt node) {
        return null;
    }

    @Override
    public Void visitElseClause(ElseClause node) {
        return null;
    }

    @Override
    public Void visitWhileStmt(WhileStmt node) {
        return null;
    }

    @Override
    public Void visitFunctionDef(FunctionDef node) {
        return null;
    }
}
