package compiler.semantic.python;

import compiler.ast.python.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Semantic analyzer for Python AST.
 * Performs symbol table construction and semantic checks.
 */
public class SemanticAnalyzer implements AstVisitor<Void> {
    
    private final SymbolTable symbolTable;
    private final List<SemanticError> errors;
    private final Map<String, Integer> functionParameterCounts;
    
    public SemanticAnalyzer() {
        this.symbolTable = new SymbolTable();
        this.errors = new ArrayList<>();
        this.functionParameterCounts = new HashMap<>();
    }
    
    public List<SemanticError> analyze(Program program) {
        program.accept(this);
        return new ArrayList<>(errors);
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
    
    @Override
    public Void visitFunctionDef(FunctionDef node) {
        // Check for duplicate function definition
        Symbol existing = symbolTable.lookup(node.getName());
        if (existing != null && existing.getKind() == Symbol.Kind.FUNCTION) {
            errors.add(new SemanticError(
                node.getLine(),
                SemanticError.ErrorType.DUPLICATE_FUNCTION_DEFINITION,
                "Duplicate function definition '" + node.getName() + "'"
            ));
        } else {
            Symbol functionSymbol = new Symbol(
                node.getName(),
                Symbol.Kind.FUNCTION,
                node.getLine()
            );
            functionSymbol.setInferredType(Type.FUNCTION);
            symbolTable.addSymbol(functionSymbol);
        }
        
        // Store parameter count for call checking
        functionParameterCounts.put(node.getName(), node.getParameters().size());
        
        // Check for duplicate parameter names
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
            node.getVariable(),
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
        if (functionName != null && functionParameterCounts.containsKey(functionName)) {
            int expectedParams = functionParameterCounts.get(functionName);
            int actualArgs = node.getArguments().size();
            
            if (expectedParams != actualArgs) {
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
        
        // Check for type mismatch in operators
        if (leftType != Type.UNKNOWN && rightType != Type.UNKNOWN && leftType != rightType) {
            // Only allow certain numeric combinations (int + float)
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
        
        if (!node.getElseBody().isEmpty()) {
            symbolTable.enterScope(Scope.ScopeKind.BLOCK);
            for (Statement stmt : node.getElseBody()) {
                stmt.accept(this);
            }
            symbolTable.exitScope();
        }
        
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
            // For arithmetic, result is numeric
            if (left == Type.FLOAT || right == Type.FLOAT) {
                return Type.FLOAT;
            } else if (left == Type.INTEGER && right == Type.INTEGER) {
                return Type.INTEGER;
            }
        }
        return Type.UNKNOWN;
    }
}
