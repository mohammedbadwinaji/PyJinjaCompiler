package compiler.semantic.python;

import compiler.ast.python.*;
import compiler.ast.common.AstVisitor;
import compiler.ast.common.*;
import compiler.semantic.common.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Semantic analyzer for Python AST.
 * Performs symbol table construction and semantic checks.
 */
public class PythonSemanticAnalyzer implements AstVisitor<Void> {

    private final SymbolTable symbolTable;
    private final List<SemanticError> errors;
    private final Map<String, Integer> functionParameterCounts;

    public PythonSemanticAnalyzer() {
        this.symbolTable = new SymbolTable();
        this.errors = new ArrayList<>();
        this.functionParameterCounts = new HashMap<>();
        initializeBuiltInFunctions();
    }

    public List<SemanticError> analyze(Program program) {
        // First pass: collect all function definitions
        collectFunctionDefinitions(program);
        
        // Second pass: full semantic analysis
        program.accept(this);
        return new ArrayList<>(errors);
    }

    /**
     * First pass: collect all function definitions and add them to the symbol table.
     * This allows functions to be called before they are defined.
     */
    private void collectFunctionDefinitions(Program program) {
        for (Statement stmt : program.getStatements()) {
            if (stmt instanceof FunctionDef) {
                FunctionDef funcDef = (FunctionDef) stmt;
                
                // Check for duplicate function definition
                Symbol existing = symbolTable.lookup(funcDef.getName());
                if (existing != null && existing.getKind() == Symbol.Kind.FUNCTION) {
                    errors.add(new SemanticError(
                            funcDef.getLine(),
                            SemanticError.ErrorType.DUPLICATE_FUNCTION_DEFINITION,
                            "Duplicate function definition '" + funcDef.getName() + "'"
                    ));
                } else {
                    Symbol functionSymbol = new Symbol(
                            funcDef.getName(),
                            Symbol.Kind.FUNCTION,
                            funcDef.getLine()
                    );
                    functionSymbol.setInferredType(Type.FUNCTION);
                    symbolTable.addSymbol(functionSymbol);
                }

                // Store parameter count for call checking
                functionParameterCounts.put(funcDef.getName(), funcDef.getParameters().size());
            }
        }
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    @Override
    public Void visitProgram(Program node) {
        for (Statement stmt : node.getStatements()) {
            stmt.accept(this);
        }
        return null;
    }

    /**
     * Initialize built-in functions that don't need to be defined in Python code.
     * This makes it easy to add more built-in functions later.
     */
    private void initializeBuiltInFunctions() {
        addBuiltInFunction("render_template", -1);
    }

    private void addBuiltInFunction(String name, int paramCount) {
        Symbol functionSymbol = new Symbol(name, Symbol.Kind.FUNCTION, 0);
        functionSymbol.setInferredType(Type.FUNCTION);
        symbolTable.addSymbol(functionSymbol);
        functionParameterCounts.put(name, paramCount);
    }

    @Override
    public Void visitFunctionDef(FunctionDef node) {
        // Function symbol already added in first pass, just check for duplicate parameter names
        for (int i = 0; i < node.getParameters().size(); i++) {
            FunctionParameter param = node.getParameters().get(i);
            for (int j = i + 1; j < node.getParameters().size(); j++) {
                if (param.getName().equals(node.getParameters().get(j).getName())) {
                    errors.add(new SemanticError(
                            node.getLine(),
                            SemanticError.ErrorType.DUPLICATE_PARAMETER,
                            "Duplicate parameter name '" + param.getName() + "' in function '" + node.getName() + "'"
                    ));
                }
            }
        }

        // Enter function scope
        symbolTable.enterScope(Scope.ScopeKind.FUNCTION);

        // Add parameters to scope
        for (FunctionParameter param : node.getParameters()) {
            Symbol paramSymbol = new Symbol(
                    param.getName(),
                    Symbol.Kind.PARAMETER,
                    param.getLine()
            );
            symbolTable.addSymbol(paramSymbol);
        }

        // Visit decorators
        for (Decorator decorator : node.getDecorators()) {
            decorator.accept(this);
        }

        // Visit body
        for (Statement stmt : node.getBody()) {
            stmt.accept(this);
        }

        // Exit function scope
        symbolTable.exitScope();

        return null;
    }

    @Override
    public Void visitAssign(Assign node) {
        // First, visit the value expression (this triggers type checking in visitBinaryExpr)
        node.getValue().accept(this);

        // Then, infer its type for symbol table
        Type valueType = inferType(node.getValue());

        // Then, add targets to symbol table
        for (Expression target : node.getTargets()) {
            if (target instanceof Identifier) {
                Identifier ident = (Identifier) target;
                Symbol existing = symbolTable.getCurrentScope().lookupLocal(ident.getName());
                if (existing == null) {
                    Symbol varSymbol = new Symbol(
                            ident.getName(),
                            Symbol.Kind.VARIABLE,
                            ident.getLine()
                    );
                    varSymbol.setInferredType(valueType);
                    symbolTable.addSymbol(varSymbol);
                } else {
                    // Variable already exists in this scope, update type
                    existing.setInferredType(valueType);
                }
            }
            // For more complex targets (attributes, indices), we'd need more handling
            target.accept(this);
        }

        return null;
    }

    @Override
    public Void visitIdentifier(Identifier node) {
        // Check if identifier is defined
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
    public Void visitForStmt(ForStmt node) {
        // Check if iterable is actually iterable
        Type iterableType = inferType(node.getIterable());
        if (iterableType != Type.UNKNOWN &&
                iterableType != Type.LIST &&
                iterableType != Type.STRING) {
            errors.add(new SemanticError(
                    node.getLine(),
                    SemanticError.ErrorType.NON_ITERABLE_IN_FOR,
                    "Non-iterable type '" + iterableType + "' used in for loop"
            ));
        }

        // Add loop variable to scope
        Symbol loopVar = new Symbol(
                node.getVariable().getName(),
                Symbol.Kind.VARIABLE,
                node.getLine()
        );
        symbolTable.addSymbol(loopVar);

        // Visit iterable expression
        node.getIterable().accept(this);

        // Visit body
        for (Statement stmt : node.getBody()) {
            stmt.accept(this);
        }

        return null;
    }

    @Override
    public Void visitCallExpr(CallExpr node) {
        // Resolve callee
        String functionName = null;
        Symbol calleeSymbol = null;

        if (node.getCallee() instanceof Identifier) {
            functionName = ((Identifier) node.getCallee()).getName();
            calleeSymbol = symbolTable.lookup(functionName);
        }

        // Check if callee exists
        if (calleeSymbol == null && functionName != null) {
            errors.add(new SemanticError(
                    node.getLine(),
                    SemanticError.ErrorType.UNDEFINED_FUNCTION,
                    "Undefined function '" + functionName + "'"
            ));
            // Continue analysis - visit arguments but don't check argument count
            for (Argument arg : node.getArguments()) {
                arg.accept(this);
            }
            return null;
        }

        // Check if callee is callable (must be a function)
        if (calleeSymbol != null && calleeSymbol.getKind() != Symbol.Kind.FUNCTION) {
            errors.add(new SemanticError(
                    node.getLine(),
                    SemanticError.ErrorType.NOT_CALLABLE,
                    "Variable '" + functionName + "' is not callable"
            ));
            // Continue analysis - visit arguments but don't check argument count
            for (Argument arg : node.getArguments()) {
                arg.accept(this);
            }
            return null;
        }

        // Check argument count (only if function exists and is actually a function)
        // Skip checking for built-in functions with -1 parameter count (variable arguments)
        if (functionName != null && functionParameterCounts.containsKey(functionName)) {
            int expectedParams = functionParameterCounts.get(functionName);
            int actualArgs = node.getArguments().size();

            // -1 indicates variable arguments (built-in functions like render_template)
            if (expectedParams != -1 && expectedParams != actualArgs) {
                errors.add(new SemanticError(
                        node.getLine(),
                        SemanticError.ErrorType.WRONG_ARGUMENT_COUNT,
                        "Wrong number of arguments for function '" + functionName +
                                "': expected " + expectedParams + ", got " + actualArgs
                ));
            }
        }

        // Visit arguments
        for (Argument arg : node.getArguments()) {
            arg.accept(this);
        }

        return null;
    }

    @Override
    public Void visitBinaryExpr(BinaryExpr node) {
        // Visit both sides
        node.getLeft().accept(this);
        node.getRight().accept(this);

        // Infer types
        Type leftType = inferType(node.getLeft());
        Type rightType = inferType(node.getRight());

        // If either type is UNKNOWN, skip validation (cannot determine incompatibility)
        if (leftType == Type.UNKNOWN || rightType == Type.UNKNOWN) {
            return null;
        }

        // Check operator-specific compatibility
        if (!isBinaryOperationValid(node, leftType, rightType)) {
            errors.add(new SemanticError(
                    node.getLine(),
                    SemanticError.ErrorType.TYPE_MISMATCH,
                    "Invalid operation: " + leftType + " " + node.getOperator() + " " + rightType
            ));
        }

        return null;
    }

    /**
     * Check if a binary operation is valid for the given operand types.
     * Operator-specific compatibility rules.
     */
    private boolean isBinaryOperationValid(BinaryExpr node, Type left, Type right) {
        switch (node.getOperator()) {
            case ADD:
                // STRING + STRING -> STRING
                if (left == Type.STRING && right == Type.STRING)
                    return true;
                // INTEGER + INTEGER -> INTEGER
                if (left == Type.INTEGER && right == Type.INTEGER)
                    return true;
                // FLOAT + FLOAT -> FLOAT
                if (left == Type.FLOAT && right == Type.FLOAT)
                    return true;
                // INTEGER + FLOAT or FLOAT + INTEGER -> FLOAT
                if ((left == Type.INTEGER && right == Type.FLOAT) ||
                        (left == Type.FLOAT && right == Type.INTEGER))
                    return true;
                return false;

            case SUBTRACT:
            case MULTIPLY:
            case DIVIDE:
            case MODULO:
                // Only numeric types allowed
                return (left == Type.INTEGER || left == Type.FLOAT) &&
                        (right == Type.INTEGER || right == Type.FLOAT);

            case EQ:
            case NE:
                // Equality/inequality allowed for any types
                return true;

            case LT:
            case GT:
            case LE:
            case GE:
                // Ordered comparisons only for same types (numeric/numeric or string/string)
                return left == right &&
                        (left == Type.INTEGER || left == Type.FLOAT || left == Type.STRING);

            case AND:
            case OR:
                // Boolean operations only for BOOLEAN
                return left == Type.BOOLEAN && right == Type.BOOLEAN;

            default:
                return true;
        }
    }

    @Override
    public Void visitUnaryExpr(UnaryExpr node) {
        node.getExpr().accept(this);

        Type operandType = inferType(node.getExpr());

        // If operand type is UNKNOWN, skip validation
        if (operandType == Type.UNKNOWN) {
            return null;
        }

        // Check operator-specific compatibility
        switch (node.getOperator()) {
            case PLUS:
            case MINUS:
                // +x and -x only valid for numeric types
                if (operandType != Type.INTEGER && operandType != Type.FLOAT) {
                    errors.add(new SemanticError(
                            node.getLine(),
                            SemanticError.ErrorType.TYPE_MISMATCH,
                            "Invalid operation: " + node.getOperator() + " " + operandType
                    ));
                }
                break;

            case NOT:
                // not x only valid for BOOLEAN
                if (operandType != Type.BOOLEAN) {
                    errors.add(new SemanticError(
                            node.getLine(),
                            SemanticError.ErrorType.TYPE_MISMATCH,
                            "Invalid operation: " + node.getOperator() + " " + operandType
                    ));
                }
                break;

            default:
                break;
        }

        return null;
    }

    @Override
    public Void visitIfStmt(IfStmt node) {
        node.getCondition().accept(this);

        symbolTable.enterScope(Scope.ScopeKind.BLOCK);
        for (Statement stmt : node.getThenBody()) {
            stmt.accept(this);
        }
        symbolTable.exitScope();

        for (ElifClause elif : node.getElifClauses()) {
            elif.accept(this);
        }

        if (node.getElseClause() != null) {
            node.getElseClause().accept(this);
        }

        return null;
    }

    @Override
    public Void visitElseClause(ElseClause node) {
        symbolTable.enterScope(Scope.ScopeKind.BLOCK);
        for (Statement stmt : node.getBody()) {
            stmt.accept(this);
        }
        symbolTable.exitScope();

        return null;
    }

    @Override
    public Void visitElifClause(ElifClause node) {
        node.getCondition().accept(this);

        symbolTable.enterScope(Scope.ScopeKind.BLOCK);
        for (Statement stmt : node.getBody()) {
            stmt.accept(this);
        }
        symbolTable.exitScope();

        return null;
    }

    @Override
    public Void visitWhileStmt(WhileStmt node) {
        node.getCondition().accept(this);

        symbolTable.enterScope(Scope.ScopeKind.BLOCK);
        for (Statement stmt : node.getBody()) {
            stmt.accept(this);
        }
        symbolTable.exitScope();

        return null;
    }

    @Override
    public Void visitExprStmt(ExprStmt node) {
        node.getExpression().accept(this);
        return null;
    }

    @Override
    public Void visitReturnStmt(ReturnStmt node) {
        node.getValue().ifPresent(value -> value.accept(this));
        return null;
    }

    // Expression visitors - mostly for type inference

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

    @Override
    public Void visitListExpr(ListExpr node) {
        for (Expression elem : node.getElements()) {
            elem.accept(this);
        }
        return null;
    }

    @Override
    public Void visitDictExpr(DictExpr node) {
        for (DictEntry entry : node.getEntries()) {
            entry.accept(this);
        }
        return null;
    }

    @Override
    public Void visitDictEntry(DictEntry node) {
        node.getKey().accept(this);
        node.getValue().accept(this);
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
        node.getValue().accept(this);
        return null;
    }

    @Override
    public Void visitFunctionParameter(FunctionParameter node) {
        return null;
    }

    @Override
    public Void visitDecorator(Decorator node) {
        node.getTarget().accept(this);
        for (Argument arg : node.getArguments()) {
            arg.accept(this);
        }
        return null;
    }

    /**
     * Infer the type of an expression for type checking.
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
        } else if (expr instanceof ListExpr) {
            return Type.LIST;
        } else if (expr instanceof DictExpr) {
            return Type.DICTIONARY;
        } else if (expr instanceof Identifier) {
            Symbol symbol = symbolTable.lookup(((Identifier) expr).getName());
            if (symbol != null) {
                return symbol.getInferredType();
            }
        } else if (expr instanceof BinaryExpr) {
            BinaryExpr binary = (BinaryExpr) expr;
            Type left = inferType(binary.getLeft());
            Type right = inferType(binary.getRight());

            switch (binary.getOperator()) {
                case ADD:
                    // STRING + STRING -> STRING
                    if (left == Type.STRING && right == Type.STRING)
                        return Type.STRING;
                    // INTEGER + FLOAT or FLOAT + INTEGER -> FLOAT
                    if (left == Type.FLOAT || right == Type.FLOAT)
                        return Type.FLOAT;
                    // INTEGER + INTEGER -> INTEGER
                    if (left == Type.INTEGER && right == Type.INTEGER)
                        return Type.INTEGER;
                    return Type.UNKNOWN;

                case SUBTRACT:
                case MULTIPLY:
                case DIVIDE:
                case MODULO:
                    // Numeric operations
                    if (left == Type.FLOAT || right == Type.FLOAT)
                        return Type.FLOAT;
                    if (left == Type.INTEGER && right == Type.INTEGER)
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
                    // All comparisons and boolean operations return BOOLEAN
                    return Type.BOOLEAN;

                default:
                    return Type.UNKNOWN;
            }
        } else if (expr instanceof UnaryExpr) {
            UnaryExpr unary = (UnaryExpr) expr;
            Type operandType = inferType(unary.getExpr());

            switch (unary.getOperator()) {
                case PLUS:
                case MINUS:
                    // +x or -x preserves numeric type
                    if (operandType == Type.INTEGER || operandType == Type.FLOAT)
                        return operandType;
                    return Type.UNKNOWN;

                case NOT:
                    // not x always returns BOOLEAN
                    return Type.BOOLEAN;

                default:
                    return Type.UNKNOWN;
            }
        } else if (expr instanceof CallExpr) {
            // For function calls, if we know the callee's return type, use it
            // Otherwise return UNKNOWN
            CallExpr call = (CallExpr) expr;
            if (call.getCallee() instanceof Identifier) {
                Symbol symbol = symbolTable.lookup(((Identifier) call.getCallee()).getName());
                if (symbol != null && symbol.getKind() == Symbol.Kind.FUNCTION) {
                    // We don't track function return types in this simple system
                    // Return UNKNOWN for function call results
                    return Type.UNKNOWN;
                }
            }
            return Type.UNKNOWN;
        }
        return Type.UNKNOWN;
    }
    /* -------------------------
       Jinja-tree nodes
       -------------------------
       These are part of the shared AstVisitor<T> interface because the
       Python AST and Jinja AST both flow through the same visitor
       contract (see AstVisitor.java). This analyzer only ever walks a
       Python Program, so it will never actually be handed a Jinja node
       -- a dedicated JinjaSemanticAnalyzer (implementing this same
       interface) is responsible for those. These overrides exist purely
       to satisfy the interface and are unreachable in practice. */

    @Override
    public Void visitTemplate(compiler.ast.jinja.Template node) {
        return null;
    }

    @Override
    public Void visitHtmlText(compiler.ast.jinja.HtmlText node) {
        return null;
    }

    @Override
    public Void visitExpressionOutput(compiler.ast.jinja.ExpressionOutput node) {
        return null;
    }

    @Override
    public Void visitJinjaIfStmt(compiler.ast.jinja.JinjaIfStmt node) {
        return null;
    }

    @Override
    public Void visitJinjaElifClause(compiler.ast.jinja.JinjaElifClause node) {
        return null;
    }

    @Override
    public Void visitJinjaElseClause(compiler.ast.jinja.JinjaElseClause node) {
        return null;
    }

    @Override
    public Void visitJinjaForStmt(compiler.ast.jinja.JinjaForStmt node) {
        return null;
    }
}
