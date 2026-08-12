package compiler.generator;

import compiler.ast.python.*;
import compiler.ast.common.*;
import compiler.ast.python.DictEntry;

import compiler.semantic.common.Scope;
import compiler.semantic.common.Symbol;
import compiler.semantic.common.SymbolTable;
import compiler.semantic.common.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Generator: bridges the Python tree and the Jinja tree (requirement 2).
 *
 * PythonSemanticAnalyzer.getSymbolTable(), once analyze() has returned,
 * sits back at the GLOBAL scope - every FunctionDef enters a FUNCTION
 * scope and symmetrically exits it, so a variable declared inside a
 * route function (e.g. `products` inside `def index(): products = [...]`)
 * is no longer reachable through SymbolTable.lookup() from that global
 * scope. Handing that table straight to JinjaSemanticAnalyzer therefore
 * only works for module-level Python variables, not the Flask-style
 * `render_template("index.jinja", title=title, products=products)`
 * pattern used in test01.py / test02.py.
 *
 * This class finds that render_template(...) call and, while walking
 * toward it, maintains a real evaluated environment (via
 * ExpressionEvaluator) of every simple assignment seen along the way.
 * That gives two things per call site:
 *   - contextValues: actual Java values (List/Map/String/...) - what
 *     Renderer needs to produce real HTML.
 *   - contextSymbolTable: a fresh SymbolTable of the same names, typed
 *     from those values - what JinjaSemanticAnalyzer needs to type-check
 *     the template before it's ever rendered.
 * Where a value can't be statically evaluated (e.g. it came from a
 * user-defined function call), the PythonSemanticAnalyzer-produced
 * SymbolTable is used as a type-only fallback.
 */
public final class Generator {

    private static final String RENDER_TEMPLATE = "render_template";
    private static final String RENDER_TEMPLATE_STRING = "render_template_string";

    private final ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator();
    private final FunctionEvaluator functionEvaluator = new FunctionEvaluator();

    /**
     * One resolved render_template(...) call site.
     */
    public static final class RenderCall {

        private final String templateName;
        private final int line;
        private final Map<String, Object> contextValues;
        private final SymbolTable contextSymbolTable;

        public RenderCall(
                String templateName,
                int line,
                Map<String, Object> contextValues,
                SymbolTable contextSymbolTable) {

            this.templateName = templateName;
            this.line = line;
            this.contextValues = contextValues;
            this.contextSymbolTable = contextSymbolTable;
        }

        public String getTemplateName() {
            return templateName;
        }

        public int getLine() {
            return line;
        }

        /** Real data, ready for Renderer.render(template, contextValues). */
        public Map<String, Object> getContextValues() {
            return contextValues;
        }

        /** Ready to pass straight into `new JinjaSemanticAnalyzer(...)`. */
        public SymbolTable getContextSymbolTable() {
            return contextSymbolTable;
        }
    }

    /**
     * @param program          the parsed Python AST.
     * @param analyzedSymbols  the SymbolTable produced by
     *                         PythonSemanticAnalyzer.analyze(program) -
     *                         used only as a type-only fallback for
     *                         values ExpressionEvaluator can't statically
     *                         resolve (searches every scope, not just
     *                         the currently-active one).
     * @return one RenderCall per render_template(...) / render_template_string(...)
     *         call found anywhere in the program (typically one per route
     *         function).
     */
    public List<RenderCall> generate(Program program, SymbolTable analyzedSymbols) {

        Map<String, Symbol> allSymbolsByName = indexAllScopes(analyzedSymbols);

        List<RenderCall> results = new ArrayList<>();
        // Create a single environment for module-level statements
        Map<String, Object> moduleEnv = new LinkedHashMap<>();
        
        // First pass: identify which functions are called at module level
        java.util.Set<String> calledFunctions = identifyCalledFunctions(program);
        
        // Map to store function definitions for later evaluation
        Map<String, FunctionDef> functionDefs = new HashMap<>();
        for (Statement stmt : program.getStatements()) {
            if (stmt instanceof FunctionDef) {
                functionDefs.put(((FunctionDef) stmt).getName(), (FunctionDef) stmt);
            }
        }
        
        // Map to store function call arguments
        Map<String, List<Object>> functionCallArguments = new HashMap<>();
        collectFunctionCallArguments(program, functionCallArguments, moduleEnv, allSymbolsByName, functionDefs);
        
        // Second pass: scan statements, but only enter function bodies if they're called
        for (Statement stmt : program.getStatements()) {
            scanStatement(stmt, moduleEnv, allSymbolsByName, results, calledFunctions, functionDefs, functionCallArguments);
        }

        return results;
    }

    /* -------------------------
       Indexing every scope by symbol name (type-only fallback)
       ------------------------- */

    private Map<String, Symbol> indexAllScopes(SymbolTable symbolTable) {
        Map<String, Symbol> byName = new LinkedHashMap<>();
        for (Scope scope : symbolTable.getAllScopes()) {
            byName.putAll(scope.getSymbols());
        }
        return byName;
    }

    /* -------------------------
       Identify which functions are called at module level
       ------------------------- */

    private java.util.Set<String> identifyCalledFunctions(Program program) {
        java.util.Set<String> calledFunctions = new java.util.HashSet<>();
        
        for (Statement stmt : program.getStatements()) {
            if (stmt instanceof ExprStmt) {
                Expression expr = ((ExprStmt) stmt).getExpression();
                if (expr instanceof CallExpr) {
                    Expression callee = ((CallExpr) expr).getCallee();
                    if (callee instanceof Identifier) {
                        calledFunctions.add(((Identifier) callee).getName());
                    }
                }
            } else if (stmt instanceof Assign) {
                Expression value = ((Assign) stmt).getValue();
                if (value instanceof CallExpr) {
                    Expression callee = ((CallExpr) value).getCallee();
                    if (callee instanceof Identifier) {
                        calledFunctions.add(((Identifier) callee).getName());
                    }
                }
            }
        }
        
        return calledFunctions;
    }

    /**
     * Collect function call arguments by evaluating them at module level.
     */
    private void collectFunctionCallArguments(Program program, Map<String, List<Object>> functionCallArguments,
                                               Map<String, Object> env, Map<String, Symbol> symbols,
                                               Map<String, FunctionDef> functionDefs) {
        for (Statement stmt : program.getStatements()) {
            if (stmt instanceof ExprStmt) {
                Expression expr = ((ExprStmt) stmt).getExpression();
                if (expr instanceof CallExpr) {
                    CallExpr call = (CallExpr) expr;
                    if (call.getCallee() instanceof Identifier) {
                        String funcName = ((Identifier) call.getCallee()).getName();
                        List<Object> args = functionEvaluator.evaluateCallArguments(call, env, symbols, functionDefs);
                        if (args != null && !args.isEmpty()) {
                            functionCallArguments.put(funcName, args);
                        }
                    }
                }
            }
        }
    }

    /* -------------------------
       Statement scan, tracking a real evaluated environment
       ------------------------- */

    private void scanStatement(
            Statement stmt,
            Map<String, Object> env,
            Map<String, Symbol> symbols,
            List<RenderCall> results,
            java.util.Set<String> calledFunctions,
            Map<String, FunctionDef> functionDefs,
            Map<String, List<Object>> functionCallArguments) {

        if (stmt instanceof FunctionDef) {
            FunctionDef funcDef = (FunctionDef) stmt;
            // Only scan function body if this function is called at module level
            if (calledFunctions.contains(funcDef.getName())) {
                // Get the call arguments for this function
                List<Object> callArguments = functionCallArguments.get(funcDef.getName());
                
                // Fresh environment per function - Python variables don't
                // leak across functions.
                Map<String, Object> functionEnv = new LinkedHashMap<>();
                
                // Bind parameters to their evaluated argument values
                if (callArguments != null) {
                    int paramIndex = 0;
                    for (FunctionParameter param : funcDef.getParameters()) {
                        if (paramIndex < callArguments.size()) {
                            functionEnv.put(param.getName(), callArguments.get(paramIndex));
                            paramIndex++;
                        }
                    }
                }
                
                for (Statement inner : funcDef.getBody()) {
                    scanStatement(inner, functionEnv, symbols, results, calledFunctions, functionDefs, functionCallArguments);
                }
            }

        } else if (stmt instanceof Assign) {
            handleAssign((Assign) stmt, env, symbols, functionDefs);
            checkForRenderCall(((Assign) stmt).getValue(), env, symbols, results, functionDefs);

        } else if (stmt instanceof ExprStmt) {
            Expression expr = ((ExprStmt) stmt).getExpression();
            // Evaluate function calls at module level to populate environment
            functionEvaluator.evaluateFunctionCall(expr, env, symbols, functionDefs);
            checkForRenderCall(expr, env, symbols, results, functionDefs);

        } else if (stmt instanceof ReturnStmt) {
            ((ReturnStmt) stmt).getValue()
                    .ifPresent(expr -> checkForRenderCall(expr, env, symbols, results, functionDefs));

        } else if (stmt instanceof IfStmt) {
            IfStmt ifStmt = (IfStmt) stmt;
            // Create branch-local environments to prevent contamination
            Map<String, Object> thenEnv = new LinkedHashMap<>(env);
            for (Statement inner : ifStmt.getThenBody()) scanStatement(inner, thenEnv, symbols, results, calledFunctions, functionDefs, functionCallArguments);

            for (ElifClause elif : ifStmt.getElifClauses()) {
                Map<String, Object> elifEnv = new LinkedHashMap<>(env);
                for (Statement inner : elif.getBody()) scanStatement(inner, elifEnv, symbols, results, calledFunctions, functionDefs, functionCallArguments);
            }

            if (ifStmt.getElseClause() != null) {
                Map<String, Object> elseEnv = new LinkedHashMap<>(env);
                for (Statement inner : ifStmt.getElseClause().getBody()) scanStatement(inner, elseEnv, symbols, results, calledFunctions, functionDefs, functionCallArguments);
            }

        } else if (stmt instanceof ForStmt) {
            for (Statement inner : ((ForStmt) stmt).getBody()) scanStatement(inner, env, symbols, results, calledFunctions, functionDefs, functionCallArguments);

        } else if (stmt instanceof WhileStmt) {
            for (Statement inner : ((WhileStmt) stmt).getBody()) scanStatement(inner, env, symbols, results, calledFunctions, functionDefs, functionCallArguments);
        }
    }

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
                Object value = functionEvaluator.evaluateArgument(element, env, symbols, functionDefs);
                list.add(value);
            }
            return list;
        } else if (expr instanceof DictExpr) {
            Map<String, Object> map = new LinkedHashMap<>();
            for (DictEntry entry : ((DictExpr) expr).getEntries()) {
                Object value = functionEvaluator.evaluateArgument(entry.getValue(), env, symbols, functionDefs);
                map.put(keyOf(entry.getKey()), value);
            }
            return map;
        } else {
            return functionEvaluator.evaluateArgument(expr, env, symbols, functionDefs);
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

    private void checkForRenderCall(
            Expression expr,
            Map<String, Object> env,
            Map<String, Symbol> symbols,
            List<RenderCall> results,
            Map<String, FunctionDef> functionDefs) {

        if (!(expr instanceof CallExpr)) {
            return;
        }

        CallExpr call = (CallExpr) expr;
        if (!(call.getCallee() instanceof Identifier)) {
            return;
        }

        String calleeName = ((Identifier) call.getCallee()).getName();
        if (!calleeName.equals(RENDER_TEMPLATE) && !calleeName.equals(RENDER_TEMPLATE_STRING)) {
            return;
        }

        String templateName = null;
        Map<String, Object> contextValues = new LinkedHashMap<>();
        SymbolTable contextTable = new SymbolTable();

        for (Argument arg : call.getArguments()) {

            if (arg instanceof PositionalArgument) {
                Object value = expressionEvaluator.evaluate(((PositionalArgument) arg).getValue(), env);
                if (templateName == null && value instanceof String) {
                    templateName = (String) value;
                }

            } else if (arg instanceof KeywordArgument) {
                KeywordArgument kw = (KeywordArgument) arg;

                Object value = functionEvaluator.evaluateArgument(kw.getValue(), env, symbols, functionDefs);
                Type type = typeOf(value);

                if (value == null) {
                    // Couldn't statically evaluate it (or it's a real
                    // None) - fall back to the symbol's declared type,
                    // if one is known.
                    Symbol resolved = resolveSymbol(kw.getName(), kw.getValue(), symbols);
                    if (resolved != null) {
                        type = resolved.getInferredType();
                    }
                }

                contextValues.put(kw.getName(), value);

                Symbol contextSymbol = new Symbol(kw.getName(), Symbol.Kind.VARIABLE, kw.getLine());
                contextSymbol.setInferredType(type);
                
                // Extract collection type information
                if (value instanceof List) {
                    Type elementType = extractElementType((List<?>) value);
                    contextSymbol.setElementType(elementType);
                    // If list contains dictionaries, extract field types from elements
                    if (elementType == Type.DICTIONARY) {
                        Map<String, Type> elementFieldTypes = extractElementFieldTypes((List<?>) value);
                        contextSymbol.setElementFieldTypes(elementFieldTypes);
                    }
                } else if (value instanceof Map) {
                    Map<String, Type> fieldTypes = extractFieldTypes((Map<?, ?>) value);
                    contextSymbol.setFieldTypes(fieldTypes);
                }
                
                contextTable.addSymbol(contextSymbol);
            }
        }

        // Create a snapshot of the context to prevent later mutations
        Map<String, Object> contextSnapshot = new LinkedHashMap<>(contextValues);
        results.add(new RenderCall(templateName, call.getLine(), contextSnapshot, contextTable));
    }

    /**
     * Resolves a render_template(...) keyword argument's value back to a
     * declared Symbol, as a type-only fallback for whatever
     * ExpressionEvaluator couldn't statically resolve (e.g. the result
     * of a user-defined function call).
     */
    private Symbol resolveSymbol(String keywordName, Expression value, Map<String, Symbol> symbols) {
        if (value instanceof Identifier) {
            return symbols.get(((Identifier) value).getName());
        }
        return symbols.get(keywordName);
    }

    private Type typeOf(Object value) {
        if (value == null) return Type.NONE;
        if (value instanceof Long) return Type.INTEGER;
        if (value instanceof Double) return Type.FLOAT;
        if (value instanceof String) return Type.STRING;
        if (value instanceof Boolean) return Type.BOOLEAN;
        if (value instanceof List) return Type.LIST;
        if (value instanceof Map) return Type.DICTIONARY;
        return Type.UNKNOWN;
    }

    /**
     * Extract element type information from a list value.
     */
    private Type extractElementType(List<?> list) {
        if (list.isEmpty()) {
            return Type.UNKNOWN;
        }
        
        Type firstElementType = typeOf(list.get(0));
        if (firstElementType == Type.UNKNOWN) {
            return Type.UNKNOWN;
        }
        
        // Check if all elements have the same type
        for (Object element : list) {
            Type elementType = typeOf(element);
            if (elementType != firstElementType) {
                return Type.UNKNOWN; // Mixed types
            }
        }
        
        return firstElementType;
    }

    /**
     * Extract field type information from a dictionary value.
     */
    private Map<String, Type> extractFieldTypes(Map<?, ?> map) {
        if (map.isEmpty()) {
            return null;
        }
        
        Map<String, Type> fieldTypes = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String key = String.valueOf(entry.getKey());
            Type valueType = typeOf(entry.getValue());
            fieldTypes.put(key, valueType);
        }
        
        return fieldTypes;
    }

    /**
     * Extract field types from list elements when they are dictionaries.
     * Merges field types from all dictionary elements in the list.
     */
    private Map<String, Type> extractElementFieldTypes(List<?> list) {
        if (list.isEmpty()) {
            return null;
        }
        
        Map<String, Type> mergedFieldTypes = new LinkedHashMap<>();
        for (Object element : list) {
            if (element instanceof Map) {
                Map<?, ?> elementMap = (Map<?, ?>) element;
                for (Map.Entry<?, ?> entry : elementMap.entrySet()) {
                    String key = String.valueOf(entry.getKey());
                    Type valueType = typeOf(entry.getValue());
                    // If we've seen this field before with a different type, mark as UNKNOWN
                    if (mergedFieldTypes.containsKey(key) && mergedFieldTypes.get(key) != valueType) {
                        mergedFieldTypes.put(key, Type.UNKNOWN);
                    } else {
                        mergedFieldTypes.put(key, valueType);
                    }
                }
            }
        }
        
        return mergedFieldTypes.isEmpty() ? null : mergedFieldTypes;
    }
}
