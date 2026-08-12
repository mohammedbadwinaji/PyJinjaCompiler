package compiler.generator;

import compiler.ast.python.*;
import compiler.ast.common.*;
import compiler.ast.python.DictEntry;
import compiler.semantic.common.Symbol;
import compiler.semantic.common.SymbolTable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * FunctionEvaluator: evaluates user-defined function calls statically.
 * 
 * This class handles the evaluation of function bodies to determine their
 * return values when called at module level. It supports simple functions
 * with return statements, assignments, and if statements.
 */
public class FunctionEvaluator {

    private final ExpressionEvaluator expressionEvaluator;

    public FunctionEvaluator() {
        this.expressionEvaluator = new ExpressionEvaluator();
    }

    /**
     * Evaluate a function body statically and return its result.
     * This handles simple functions that return literals or expressions.
     */
    public Object evaluateFunctionBody(FunctionDef funcDef, Map<String, Object> env,
                                        Map<String, Symbol> symbols, Map<String, FunctionDef> functionDefs) {
        Map<String, Object> functionEnv = new LinkedHashMap<>(env);
        
        // Evaluate function body statements
        for (Statement stmt : funcDef.getBody()) {
            if (stmt instanceof ReturnStmt) {
                ReturnStmt returnStmt = (ReturnStmt) stmt;
                if (returnStmt.getValue().isPresent()) {
                    return expressionEvaluator.evaluate(returnStmt.getValue().get(), functionEnv);
                }
                return null;
            } else if (stmt instanceof Assign) {
                handleAssign((Assign) stmt, functionEnv, symbols, functionDefs);
            } else if (stmt instanceof IfStmt) {
                // Handle simple if statements for evaluation
                Object result = evaluateIfStatement((IfStmt) stmt, functionEnv, symbols, functionDefs);
                if (result != null) {
                    return result;
                }
            }
        }
        
        return null; // No return statement found
    }

    /**
     * Evaluate an if statement and return the result if it contains a return.
     */
    public Object evaluateIfStatement(IfStmt ifStmt, Map<String, Object> env,
                                       Map<String, Symbol> symbols, Map<String, FunctionDef> functionDefs) {
        // Evaluate condition
        Object conditionValue = expressionEvaluator.evaluate(ifStmt.getCondition(), env);
        boolean conditionTrue = expressionEvaluator.truthy(conditionValue);
        
        if (conditionTrue) {
            return evaluateBlockForReturn(ifStmt.getThenBody(), env, symbols, functionDefs);
        }
        
        for (ElifClause elif : ifStmt.getElifClauses()) {
            Object elifCondition = expressionEvaluator.evaluate(elif.getCondition(), env);
            if (expressionEvaluator.truthy(elifCondition)) {
                return evaluateBlockForReturn(elif.getBody(), env, symbols, functionDefs);
            }
        }
        
        if (ifStmt.getElseClause() != null) {
            return evaluateBlockForReturn(ifStmt.getElseClause().getBody(), env, symbols, functionDefs);
        }
        
        return null;
    }

    /**
     * Evaluate a block of statements and return the first return value found.
     */
    public Object evaluateBlockForReturn(List<Statement> statements, Map<String, Object> env,
                                          Map<String, Symbol> symbols, Map<String, FunctionDef> functionDefs) {
        for (Statement stmt : statements) {
            if (stmt instanceof ReturnStmt) {
                ReturnStmt returnStmt = (ReturnStmt) stmt;
                if (returnStmt.getValue().isPresent()) {
                    return expressionEvaluator.evaluate(returnStmt.getValue().get(), env);
                }
                return null;
            } else if (stmt instanceof Assign) {
                handleAssign((Assign) stmt, env, symbols, functionDefs);
            }
        }
        return null;
    }

    /**
     * Evaluate an argument expression, handling function calls that may return values.
     */
    public Object evaluateArgument(Expression expr, Map<String, Object> env,
                                   Map<String, Symbol> symbols, Map<String, FunctionDef> functionDefs) {
        Object value = expressionEvaluator.evaluate(expr, env);
        
        // If evaluation returned null and the expression is a function call,
        // try to evaluate the user-defined function
        if (value == null && expr instanceof CallExpr) {
            CallExpr call = (CallExpr) expr;
            if (call.getCallee() instanceof Identifier) {
                String funcName = ((Identifier) call.getCallee()).getName();
                FunctionDef funcDef = functionDefs.get(funcName);
                if (funcDef != null) {
                    value = evaluateFunctionBody(funcDef, env, symbols, functionDefs);
                }
            }
        }
        
        return value;
    }

    /**
     * Evaluate the arguments of a function call.
     */
    public List<Object> evaluateCallArguments(CallExpr call, Map<String, Object> env,
                                                 Map<String, Symbol> symbols, Map<String, FunctionDef> functionDefs) {
        List<Object> args = new ArrayList<>();
        for (Argument arg : call.getArguments()) {
            if (arg instanceof PositionalArgument) {
                Object value = evaluateArgument(((PositionalArgument) arg).getValue(), env, symbols, functionDefs);
                args.add(value);
            }
        }
        return args;
    }

    /**
     * Handle assignment statements in function bodies.
     */
    private void handleAssign(Assign assign, Map<String, Object> env, 
                               Map<String, Symbol> symbols, Map<String, FunctionDef> functionDefs) {
        if (assign.getTargets().size() != 1) {
            return; // a = b = c = 0 style chains aren't data sources here
        }

        Expression target = assign.getTargets().get(0);
        if (!(target instanceof Identifier)) {
            return;
        }

        String name = ((Identifier) target).getName();
        Object value = evaluateAssignmentValue(assign.getValue(), env, symbols, functionDefs);
        env.put(name, value);
    }

    /**
     * Evaluate an assignment value, handling function calls within lists/dicts.
     */
    private Object evaluateAssignmentValue(Expression expr, Map<String, Object> env,
                                           Map<String, Symbol> symbols, Map<String, FunctionDef> functionDefs) {
        if (expr instanceof ListExpr) {
            List<Object> list = new ArrayList<>();
            for (Expression element : ((ListExpr) expr).getElements()) {
                Object value = evaluateArgument(element, env, symbols, functionDefs);
                list.add(value);
            }
            return list;
        } else if (expr instanceof DictExpr) {
            Map<String, Object> map = new LinkedHashMap<>();
            for (DictEntry entry : ((DictExpr) expr).getEntries()) {
                Object value = evaluateArgument(entry.getValue(), env, symbols, functionDefs);
                map.put(keyOf(entry.getKey()), value);
            }
            return map;
        } else {
            return evaluateArgument(expr, env, symbols, functionDefs);
        }
    }

    private String keyOf(Expression key) {
        if (key instanceof StringLiteral) {
            return ((StringLiteral) key).getValue();
        }
        if (key instanceof Identifier) {
            return ((Identifier) key).getName();
        }
        return String.valueOf(key);
    }

    /**
     * Evaluate function calls at module level to populate environment with
     * results from user-defined functions.
     */
    public void evaluateFunctionCall(Expression expr, Map<String, Object> env, 
                                      Map<String, Symbol> symbols, Map<String, FunctionDef> functionDefs) {
        if (!(expr instanceof CallExpr)) {
            return;
        }

        CallExpr call = (CallExpr) expr;
        if (!(call.getCallee() instanceof Identifier)) {
            return;
        }

        String funcName = ((Identifier) call.getCallee()).getName();
        FunctionDef funcDef = functionDefs.get(funcName);
        
        if (funcDef == null) {
            return; // Not a user-defined function
        }

        // Evaluate function body to get return value
        Object returnValue = evaluateFunctionBody(funcDef, env, symbols, functionDefs);
        
        // Store the result in environment using a temporary name
        // This allows the result to be used as arguments to other function calls
        String tempName = "__temp_" + funcName + "_" + call.getLine();
        env.put(tempName, returnValue);
    }
}
