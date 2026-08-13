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
     * A callable wrapper for user-defined functions that can be passed to Jinja templates.
     */
    public static class CallableFunction {
        private final FunctionDef funcDef;
        private final Map<String, Object> env;
        private final Map<String, Symbol> symbols;
        private final Map<String, FunctionDef> functionDefs;
        private final FunctionEvaluator evaluator;

        public CallableFunction(FunctionDef funcDef, Map<String, Object> env,
                               Map<String, Symbol> symbols, Map<String, FunctionDef> functionDefs,
                               FunctionEvaluator evaluator) {
            this.funcDef = funcDef;
            this.env = env;
            this.symbols = symbols;
            this.functionDefs = functionDefs;
            this.evaluator = evaluator;
        }

        public Object call(Object... args) {
            List<Object> argList = new ArrayList<>();
            for (Object arg : args) {
                argList.add(arg);
            }
            return evaluator.evaluateFunctionBodyWithArgs(funcDef, env, symbols, functionDefs, argList);
        }

        @Override
        public String toString() {
            return "<function " + funcDef.getName() + ">";
        }
    }

    /**
     * Evaluate a function body statically and return its result.
     * This handles simple functions that return literals or expressions.
     */
    public Object evaluateFunctionBody(FunctionDef funcDef, Map<String, Object> env,
                                        Map<String, Symbol> symbols, Map<String, FunctionDef> functionDefs) {
        return evaluateFunctionBodyWithArgs(funcDef, env, symbols, functionDefs, null);
    }

    /**
     * Evaluate a function body with specific argument values.
     */
    public Object evaluateFunctionBodyWithArgs(FunctionDef funcDef, Map<String, Object> env,
                                               Map<String, Symbol> symbols, Map<String, FunctionDef> functionDefs,
                                               List<Object> argValues) {
        Map<String, Object> functionEnv = new LinkedHashMap<>(env);
        
        // Bind parameters to argument values
        if (argValues != null && !argValues.isEmpty()) {
            List<compiler.ast.python.FunctionParameter> params = funcDef.getParameters();
            for (int i = 0; i < params.size() && i < argValues.size(); i++) {
                functionEnv.put(params.get(i).getName(), argValues.get(i));
            }
        }
        
        // Evaluate function body statements
        for (Statement stmt : funcDef.getBody()) {
            if (stmt instanceof ReturnStmt) {
                ReturnStmt returnStmt = (ReturnStmt) stmt;
                if (returnStmt.getValue().isPresent()) {
                    return evaluateReturnExpression(returnStmt.getValue().get(), functionEnv, symbols, functionDefs);
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
            } else if (stmt instanceof ForStmt) {
                // Handle for loops
                evaluateForStatement((ForStmt) stmt, functionEnv, symbols, functionDefs);
            }
        }
        
        return null; // No return statement found
    }

    /**
     * Evaluate a return expression, handling recursive function calls.
     */
    private Object evaluateReturnExpression(Expression expr, Map<String, Object> env,
                                          Map<String, Symbol> symbols, Map<String, FunctionDef> functionDefs) {
        if (expr instanceof CallExpr) {
            CallExpr call = (CallExpr) expr;
            if (call.getCallee() instanceof Identifier) {
                String funcName = ((Identifier) call.getCallee()).getName();
                FunctionDef funcDef = functionDefs.get(funcName);
                if (funcDef != null) {
                    // Evaluate arguments
                    List<Object> args = evaluateCallArguments(call, env, symbols, functionDefs);
                    // Recursively evaluate the function with arguments
                    return evaluateFunctionBodyWithArgs(funcDef, env, symbols, functionDefs, args);
                }
            }
        } else if (expr instanceof BinaryExpr) {
            // Handle binary expressions that may contain function calls
            BinaryExpr binary = (BinaryExpr) expr;
            Object left = evaluateReturnExpression(binary.getLeft(), env, symbols, functionDefs);
            Object right = evaluateReturnExpression(binary.getRight(), env, symbols, functionDefs);
            // Evaluate the binary operation with the evaluated operands
            return expressionEvaluator.evaluateBinary(binary.getOperator(), left, right);
        }
        return expressionEvaluator.evaluate(expr, env);
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
                    return evaluateReturnExpression(returnStmt.getValue().get(), env, symbols, functionDefs);
                }
                return null;
            } else if (stmt instanceof Assign) {
                handleAssign((Assign) stmt, env, symbols, functionDefs);
            } else if (stmt instanceof IfStmt) {
                Object result = evaluateIfStatement((IfStmt) stmt, env, symbols, functionDefs);
                if (result != null) {
                    return result;
                }
            } else if (stmt instanceof ForStmt) {
                evaluateForStatement((ForStmt) stmt, env, symbols, functionDefs);
            }
        }
        return null;
    }

    /**
     * Evaluate a for statement.
     */
    private void evaluateForStatement(ForStmt forStmt, Map<String, Object> env,
                                     Map<String, Symbol> symbols, Map<String, FunctionDef> functionDefs) {
        // Evaluate the iterable
        Object iterableValue = expressionEvaluator.evaluate(forStmt.getIterable(), env);
        
        if (iterableValue instanceof List) {
            List<?> list = (List<?>) iterableValue;
            String varName = forStmt.getVariable().getName();
            
            for (Object item : list) {
                // Create a new environment for each iteration
                Map<String, Object> loopEnv = new LinkedHashMap<>(env);
                loopEnv.put(varName, item);
                
                // Evaluate the loop body
                for (Statement stmt : forStmt.getBody()) {
                    if (stmt instanceof Assign) {
                        handleAssign((Assign) stmt, loopEnv, symbols, functionDefs);
                    } else if (stmt instanceof IfStmt) {
                        evaluateIfStatement((IfStmt) stmt, loopEnv, symbols, functionDefs);
                    } else if (stmt instanceof ForStmt) {
                        evaluateForStatement((ForStmt) stmt, loopEnv, symbols, functionDefs);
                    }
                }
                
                // Update the outer environment with changes from the loop iteration
                env.putAll(loopEnv);
            }
        }
    }

    /**
     * Evaluate an argument expression, handling function calls that may return values.
     */
    public Object evaluateArgument(Expression expr, Map<String, Object> env,
                                   Map<String, Symbol> symbols, Map<String, FunctionDef> functionDefs) {
        // If the expression is a function call, try to evaluate the user-defined function
        if (expr instanceof CallExpr) {
            CallExpr call = (CallExpr) expr;
            if (call.getCallee() instanceof Identifier) {
                String funcName = ((Identifier) call.getCallee()).getName();
                FunctionDef funcDef = functionDefs.get(funcName);
                if (funcDef != null) {
                    // Evaluate arguments
                    List<Object> args = evaluateCallArguments(call, env, symbols, functionDefs);
                    // Evaluate the function with arguments
                    return evaluateFunctionBodyWithArgs(funcDef, env, symbols, functionDefs, args);
                }
            }
        } else if (expr instanceof Identifier) {
            // If the expression is an identifier that references a function, return a callable wrapper
            String name = ((Identifier) expr).getName();
            FunctionDef funcDef = functionDefs.get(name);
            if (funcDef != null) {
                // Return a callable wrapper that can evaluate the function
                return new CallableFunction(funcDef, env, symbols, functionDefs, this);
            }
        }
        
        return expressionEvaluator.evaluate(expr, env);
    }

    /**
     * Evaluate the arguments of a function call.
     */
    public List<Object> evaluateCallArguments(CallExpr call, Map<String, Object> env,
                                                 Map<String, Symbol> symbols, Map<String, FunctionDef> functionDefs) {
        List<Object> args = new ArrayList<>();
        for (Argument arg : call.getArguments()) {
            if (arg instanceof PositionalArgument) {
                Expression argExpr = ((PositionalArgument) arg).getValue();
                // Evaluate argument expression (may include function calls)
                Object value = evaluateArgumentExpression(argExpr, env, symbols, functionDefs);
                args.add(value);
            }
        }
        return args;
    }

    /**
     * Evaluate an argument expression, handling function calls recursively.
     */
    private Object evaluateArgumentExpression(Expression expr, Map<String, Object> env,
                                           Map<String, Symbol> symbols, Map<String, FunctionDef> functionDefs) {
        // If the expression is a function call, evaluate it recursively
        if (expr instanceof CallExpr) {
            return evaluateFunctionCall((CallExpr) expr, env, symbols, functionDefs);
        }
        
        return expressionEvaluator.evaluate(expr, env);
    }

    /**
     * Evaluate a function call with proper argument handling.
     */
    private Object evaluateFunctionCall(CallExpr call, Map<String, Object> env,
                                       Map<String, Symbol> symbols, Map<String, FunctionDef> functionDefs) {
        if (call.getCallee() instanceof Identifier) {
            String funcName = ((Identifier) call.getCallee()).getName();
            FunctionDef funcDef = functionDefs.get(funcName);
            if (funcDef != null) {
                // Evaluate arguments (each argument may be a function call itself)
                List<Object> args = new ArrayList<>();
                for (Argument arg : call.getArguments()) {
                    if (arg instanceof PositionalArgument) {
                        Expression argExpr = ((PositionalArgument) arg).getValue();
                        Object value = evaluateArgumentExpression(argExpr, env, symbols, functionDefs);
                        args.add(value);
                    }
                }
                // Evaluate the function with arguments
                return evaluateFunctionBodyWithArgs(funcDef, env, symbols, functionDefs, args);
            }
        }
        
        return expressionEvaluator.evaluate(call, env);
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
        } else if (expr instanceof BinaryExpr) {
            // Handle binary expressions that may contain function calls
            return evaluateReturnExpression(expr, env, symbols, functionDefs);
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

        // Evaluate arguments
        List<Object> args = evaluateCallArguments(call, env, symbols, functionDefs);
        
        // Evaluate function body with arguments to get return value
        Object returnValue = evaluateFunctionBodyWithArgs(funcDef, env, symbols, functionDefs, args);
        
        // Store the result in environment using a temporary name
        // This allows the result to be used as arguments to other function calls
        String tempName = "__temp_" + funcName + "_" + call.getLine();
        env.put(tempName, returnValue);
    }
}
