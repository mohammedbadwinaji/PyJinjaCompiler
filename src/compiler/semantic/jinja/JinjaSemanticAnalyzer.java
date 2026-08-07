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
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Semantic analyzer for the Jinja AST.
 * Mirrors compiler.semantic.python.SemanticAnalyzer in structure, and
 * deliberately reuses its SymbolTable / Scope / Symbol / Type /
 * SemanticError classes rather than duplicating them, since the type
 * system and scoping rules are the same regardless of which AST is being
 * walked.
 *
 * The global scope can be pre-populated with a "context" -- the set of
 * variables a template is rendered with (e.g. the `products` list a
 * Python view function passes into render_template(...)). Wiring that
 * context up from the actual Python AST/values is the Generator's job
 * (see compiler.generator.*); this class only needs the resulting
 * name -> Type map.
 */
public class JinjaSemanticAnalyzer implements AstVisitor<Void> {

    private final SymbolTable symbolTable;
    private final List<SemanticError> errors;

    public JinjaSemanticAnalyzer() {
        this(Collections.emptyMap());
    }

    /**
     * @param context variables available to the template at render time,
     *                e.g. the data a Generator pulled out of the Python
     *                AST/runtime (name -> inferred Type).
     */
    public JinjaSemanticAnalyzer(Map<String, Type> context) {
        this.symbolTable = new SymbolTable();
        this.errors = new ArrayList<>();
        registerBuiltins();
        registerContext(context);
    }

    private void registerBuiltins() {
        for (String name : new String[]{"len", "range", "str", "int", "float", "bool"}) {
            Symbol builtin = new Symbol(name, Symbol.Kind.FUNCTION, -1);
            builtin.setInferredType(Type.FUNCTION);
            symbolTable.addSymbol(builtin);
        }
    }

    private void registerContext(Map<String, Type> context) {
        for (Map.Entry<String, Type> entry : context.entrySet()) {
            Symbol symbol = new Symbol(entry.getKey(), Symbol.Kind.VARIABLE, -1);
            symbol.setInferredType(entry.getValue());
            symbolTable.addSymbol(symbol);
        }
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

        Symbol loopVar = new Symbol(
            node.getVariable().getName(),
            Symbol.Kind.VARIABLE,
            node.getLine()
        );
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

    @Override
    public Void visitBinaryExpr(BinaryExpr node) {
        node.getLeft().accept(this);
        node.getRight().accept(this);

        Type leftType = inferType(node.getLeft());
        Type rightType = inferType(node.getRight());

        if (leftType != Type.UNKNOWN && rightType != Type.UNKNOWN && leftType != rightType) {
            boolean isNumericMix = (leftType == Type.INTEGER && rightType == Type.FLOAT) ||
                                   (leftType == Type.FLOAT && rightType == Type.INTEGER);

            if (!isNumericMix) {
                errors.add(new SemanticError(
                    node.getLine(),
                    SemanticError.ErrorType.TYPE_MISMATCH,
                    "Type mismatch in binary expression: '" + leftType +
                    "' " + node.getOperator() + " '" + rightType + "'"
                ));
            }
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
        return null;
    }

    @Override
    public Void visitIndexAccess(IndexAccess node) {
        node.getTarget().accept(this);
        node.getIndex().accept(this);
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
        } else if (expr instanceof BinaryExpr) {
            BinaryExpr binary = (BinaryExpr) expr;
            Type left = inferType(binary.getLeft());
            Type right = inferType(binary.getRight());
            if (left == Type.FLOAT || right == Type.FLOAT) {
                return Type.FLOAT;
            } else if (left == Type.INTEGER && right == Type.INTEGER) {
                return Type.INTEGER;
            }
        } else if (expr instanceof UnaryExpr) {
            return inferType(((UnaryExpr) expr).getExpr());
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
