package compiler.generator;

import compiler.ast.python.*;
import compiler.ast.common.*;

import compiler.semantic.common.Scope;
import compiler.semantic.common.Symbol;
import compiler.semantic.common.SymbolTable;
import compiler.semantic.common.Type;

import java.util.ArrayList;
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

    private final ExpressionEvaluator evaluator = new ExpressionEvaluator();

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
        for (Statement stmt : program.getStatements()) {
            scanStatement(stmt, new LinkedHashMap<>(), allSymbolsByName, results);
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
       Statement scan, tracking a real evaluated environment
       ------------------------- */

    private void scanStatement(
            Statement stmt,
            Map<String, Object> env,
            Map<String, Symbol> symbols,
            List<RenderCall> results) {

        if (stmt instanceof FunctionDef) {
            // Fresh environment per function - Python variables don't
            // leak across functions.
            Map<String, Object> functionEnv = new LinkedHashMap<>();
            for (Statement inner : ((FunctionDef) stmt).getBody()) {
                scanStatement(inner, functionEnv, symbols, results);
            }

        } else if (stmt instanceof Assign) {
            handleAssign((Assign) stmt, env);
            checkForRenderCall(((Assign) stmt).getValue(), env, symbols, results);

        } else if (stmt instanceof ExprStmt) {
            checkForRenderCall(((ExprStmt) stmt).getExpression(), env, symbols, results);

        } else if (stmt instanceof ReturnStmt) {
            ((ReturnStmt) stmt).getValue()
                    .ifPresent(expr -> checkForRenderCall(expr, env, symbols, results));

        } else if (stmt instanceof IfStmt) {
            IfStmt ifStmt = (IfStmt) stmt;
            for (Statement inner : ifStmt.getThenBody()) scanStatement(inner, env, symbols, results);
            for (ElifClause elif : ifStmt.getElifClauses()) {
                for (Statement inner : elif.getBody()) scanStatement(inner, env, symbols, results);
            }
            if (ifStmt.getElseClause() != null) {
                for (Statement inner : ifStmt.getElseClause().getBody()) scanStatement(inner, env, symbols, results);
            }

        } else if (stmt instanceof ForStmt) {
            for (Statement inner : ((ForStmt) stmt).getBody()) scanStatement(inner, env, symbols, results);

        } else if (stmt instanceof WhileStmt) {
            for (Statement inner : ((WhileStmt) stmt).getBody()) scanStatement(inner, env, symbols, results);
        }
    }

    private void handleAssign(Assign assign, Map<String, Object> env) {

        if (assign.getTargets().size() != 1) {
            return; // a = b = c = 0 style chains aren't data sources here
        }

        Expression target = assign.getTargets().get(0);
        if (!(target instanceof Identifier)) {
            return;
        }

        String name = ((Identifier) target).getName();
        env.put(name, evaluator.evaluate(assign.getValue(), env));
    }

    private void checkForRenderCall(
            Expression expr,
            Map<String, Object> env,
            Map<String, Symbol> symbols,
            List<RenderCall> results) {

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
                Object value = evaluator.evaluate(((PositionalArgument) arg).getValue(), env);
                if (templateName == null && value instanceof String) {
                    templateName = (String) value;
                }

            } else if (arg instanceof KeywordArgument) {
                KeywordArgument kw = (KeywordArgument) arg;

                Object value = evaluator.evaluate(kw.getValue(), env);
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
                contextTable.addSymbol(contextSymbol);
            }
        }

        results.add(new RenderCall(templateName, call.getLine(), contextValues, contextTable));
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
}
