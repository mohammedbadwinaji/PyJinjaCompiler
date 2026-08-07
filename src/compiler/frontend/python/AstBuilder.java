package compiler.frontend.python;

import compiler.ast.python.*;
import compiler.ast.common.*;
import compiler.generated.python.PythonParser;
import compiler.generated.python.PythonParserBaseVisitor;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AstBuilder: converts ANTLR parse tree (compiler.generated.python.PythonParser)
 * into the project's AST nodes (compiler.ast.python.*).
 *
 * This implementation matches the labeled alternatives in your PythonParser.g4.
 * It returns AST nodes as Object (the base visitor is parameterized with Object).
 */
public final class AstBuilder extends PythonParserBaseVisitor<Object> {

    /* -------------------------
       Public entry point
       ------------------------- */

    public Program build(PythonParser.ProgramContext ctx) {
        List<Statement> statements = ctx.statement().stream()
                .map(this::toStatement)
                .collect(Collectors.toList());
        return new Program(lineOf(ctx), statements);
    }

    /* -------------------------
       Helpers
       ------------------------- */

    private int lineOf(org.antlr.v4.runtime.ParserRuleContext ctx) {
        return ctx == null ? -1 : ctx.getStart().getLine();
    }

    private int lineOf(TerminalNode node) {
        return node == null ? -1 : node.getSymbol().getLine();
    }

    private Statement toStatement(PythonParser.StatementContext ctx) {
        Object o = visit(ctx);
        if (o == null) return null;
        if (!(o instanceof Statement)) {
            throw new IllegalStateException("Expected Statement but got " + o.getClass().getSimpleName()
                    + " at line " + lineOf(ctx));
        }
        return (Statement) o;
    }

    private Expression toExpression(PythonParser.ExprContext ctx) {
        Object o = visit(ctx);
        if (o == null) return null;
        if (!(o instanceof Expression)) {
            throw new IllegalStateException("Expected Expression but got " + o.getClass().getSimpleName()
                    + " at line " + lineOf(ctx));
        }
        return (Expression) o;
    }

    private List<Argument> toArgumentList(PythonParser.ArgListContext ctx) {
        if (ctx == null) return Collections.emptyList();
        return ctx.argument().stream()
                .map(a -> (Argument) visit(a))
                .collect(Collectors.toList());
    }

    private String unquote(String s) {
        if (s == null || s.length() < 2) return "";
        String inner = s.substring(1, s.length() - 1);
        return inner.replace("\\\"", "\"").replace("\\'", "'").replace("\\n", "\n").replace("\\t", "\t");
    }

    private BinaryOperator mapComparisonOperator(String op) {
        switch (op) {
            case "==": return BinaryOperator.EQ;
            case "!=": return BinaryOperator.NE;
            case "<":  return BinaryOperator.LT;
            case "<=": return BinaryOperator.LE;
            case ">":  return BinaryOperator.GT;
            case ">=": return BinaryOperator.GE;
            default: throw new IllegalArgumentException("Unknown comparison operator: " + op);
        }
    }

    /* -------------------------
       Statement visitors (labels from grammar)
       ------------------------- */

    @Override
    public Object visitProgram(PythonParser.ProgramContext ctx) {
        return build(ctx);
    }

    @Override
    public Object visitReturnStatement(PythonParser.ReturnStatementContext ctx) {
        // RETURN expr (COMMA expr)* NEWLINE?
        List<PythonParser.ExprContext> exprs = ctx.expr();
        if (exprs == null || exprs.isEmpty()) {
            return new ReturnStmt(lineOf(ctx), null);
        }
        // For now, handle single expr returns. Tuple returns would need a TupleExpr AST node.
        // Taking the first expression as the return value.
        Expression expr = toExpression(exprs.get(0));
        return new ReturnStmt(lineOf(ctx), expr);
    }

    @Override
    public Object visitAssignOrExprStatement(PythonParser.AssignOrExprStatementContext ctx) {
        // expr (ASSIGN expr)* NEWLINE
        List<PythonParser.ExprContext> exprs = ctx.expr();
        if (ctx.ASSIGN().size() > 0) {
            List<Expression> targets = exprs.subList(0, exprs.size() - 1).stream()
                    .map(this::toExpression)
                    .collect(Collectors.toList());
            Expression value = toExpression(exprs.get(exprs.size() - 1));
            return new Assign(lineOf(ctx), targets, value);
        } else {
            Expression e = toExpression(exprs.get(0));
            return new ExprStmt(lineOf(ctx), e);
        }
    }

    @Override
    public Object visitFunctionDef(PythonParser.FunctionDefContext ctx) {
        // decorator* DEF IDENTIFIER LPAREN paramList? RPAREN COLON suite
        List<Decorator> decorators = ctx.decorator().stream()
                .map(d -> (Decorator) visit(d))
                .collect(Collectors.toList());

        String name = ctx.IDENTIFIER().getText();

        List<FunctionParameter> params = Collections.emptyList();
        if (ctx.paramList() != null) {
            params = ctx.paramList().IDENTIFIER().stream()
                    .map(t -> new FunctionParameter(lineOf(t), t.getText()))
                    .collect(Collectors.toList());
        }

        List<Statement> body = visitSuiteAsStatements(ctx.suite());

        // Match your FunctionDef constructor:
        // public FunctionDef(int line, String name, List<FunctionParameter> parameters,
        //                    List<Decorator> decorators, List<Statement> body)
        return new FunctionDef(lineOf(ctx), name, params, decorators, body);
    }

    @Override
    public Object visitIfStatement(PythonParser.IfStatementContext ctx) {
        // IF expr COLON suite (ELIF expr COLON suite)* (ELSE COLON suite)?
        Expression cond = toExpression(ctx.expr(0));
        List<Statement> thenBody = visitSuiteAsStatements(ctx.suite(0));

        List<ElifClause> elifs = new ArrayList<>();
        int elifCount = ctx.ELIF().size();
        for (int i = 0; i < elifCount; i++) {
            Expression elifCond = toExpression(ctx.expr(i + 1));
            List<Statement> elifBody = visitSuiteAsStatements(ctx.suite(i + 1));
            elifs.add(new ElifClause(lineOf(ctx), elifCond, elifBody));
        }

        ElseClause elseClause = null;
        if (ctx.ELSE() != null) {
            PythonParser.SuiteContext elseSuite = ctx.suite(ctx.suite().size() - 1);
            List<Statement> elseBody = visitSuiteAsStatements(elseSuite);
            elseClause = new ElseClause(lineOf(ctx), elseBody);
        }

        return new IfStmt(lineOf(ctx), cond, thenBody, elifs, elseClause);
    }

    @Override
    public Object visitForStatement(PythonParser.ForStatementContext ctx) {
        // FOR IDENTIFIER IN expr COLON suite
        Identifier var = new Identifier(lineOf(ctx.IDENTIFIER()), ctx.IDENTIFIER().getText());
        Expression iterable = toExpression(ctx.expr());
        List<Statement> body = visitSuiteAsStatements(ctx.suite());
        return new ForStmt(lineOf(ctx), var, iterable, body);
    }

    @Override
    public Object visitWhileStatement(PythonParser.WhileStatementContext ctx) {
        // WHILE expr COLON suite
        Expression cond = toExpression(ctx.expr());
        List<Statement> body = visitSuiteAsStatements(ctx.suite());
        return new WhileStmt(lineOf(ctx), cond, body);
    }

    /* -------------------------
       Decorator
       ------------------------- */

    @Override
    public Object visitDecorator(PythonParser.DecoratorContext ctx) {
        // AT IDENTIFIER (DOT IDENTIFIER)* LPAREN argList? RPAREN NEWLINE
        List<TerminalNode> ids = ctx.IDENTIFIER();
        Expression target = new Identifier(lineOf(ctx), ids.get(0).getText());
        for (int i = 1; i < ids.size(); i++) {
            target = new AttributeAccess(lineOf(ctx), target, ids.get(i).getText());
        }
        List<Argument> args = ctx.argList() == null ? Collections.emptyList() : toArgumentList(ctx.argList());
        return new Decorator(lineOf(ctx), target, args);
    }

    /* -------------------------
       Expression visitors (labels)
       ------------------------- */

    @Override
    public Object visitOrExpr(PythonParser.OrExprContext ctx) {
        Expression left = toExpression(ctx.expr(0));
        Expression right = toExpression(ctx.expr(1));
        return new BinaryExpr(lineOf(ctx), left, BinaryOperator.OR, right);
    }

    @Override
    public Object visitAndExpr(PythonParser.AndExprContext ctx) {
        Expression left = toExpression(ctx.expr(0));
        Expression right = toExpression(ctx.expr(1));
        return new BinaryExpr(lineOf(ctx), left, BinaryOperator.AND, right);
    }

    @Override
    public Object visitNotExpr(PythonParser.NotExprContext ctx) {
        // Represent 'not x' as (x == False) using BinaryExpr and BooleanLiteral(false).
        Expression inner = toExpression(ctx.expr());
        return new BinaryExpr(lineOf(ctx), inner, BinaryOperator.EQ, new BooleanLiteral(lineOf(ctx), false));
    }

    @Override
    public Object visitComparisonExpr(PythonParser.ComparisonExprContext ctx) {
        Expression left = toExpression(ctx.expr(0));
        Expression right = toExpression(ctx.expr(1));
        String op = ctx.getChild(1).getText();
        BinaryOperator binOp = mapComparisonOperator(op);
        return new BinaryExpr(lineOf(ctx), left, binOp, right);
    }

    @Override
    public Object visitAddExpr(PythonParser.AddExprContext ctx) {
        Expression left = toExpression(ctx.expr(0));
        Expression right = toExpression(ctx.expr(1));
        BinaryOperator op = ctx.PLUS() != null ? BinaryOperator.ADD : BinaryOperator.SUBTRACT;
        return new BinaryExpr(lineOf(ctx), left, op, right);
    }

    @Override
    public Object visitMulExpr(PythonParser.MulExprContext ctx) {
        Expression left = toExpression(ctx.expr(0));
        Expression right = toExpression(ctx.expr(1));
        BinaryOperator op;
        if (ctx.STAR() != null) op = BinaryOperator.MULTIPLY;
        else if (ctx.SLASH() != null) op = BinaryOperator.DIVIDE;
        else op = BinaryOperator.MODULO;
        return new BinaryExpr(lineOf(ctx), left, op, right);
    }

    @Override
    public Object visitUnaryMinus(PythonParser.UnaryMinusContext ctx) {
        Expression inner = toExpression(ctx.expr());
        // Represent unary minus as 0 - inner using IntegerLiteral(0).
        Expression zero = new IntegerLiteral(lineOf(ctx), 0);
        return new BinaryExpr(lineOf(ctx), zero, BinaryOperator.SUBTRACT, inner);
    }

    @Override
    public Object visitAtomExpr(PythonParser.AtomExprContext ctx) {
        return visit(ctx.atom());
    }

    /* -------------------------
       Atom visitors
       ------------------------- */

    @Override
    public Object visitParenExpr(PythonParser.ParenExprContext ctx) {
        return toExpression(ctx.expr());
    }

    @Override
    public Object visitListExpr(PythonParser.ListExprContext ctx) {
        List<Expression> items = ctx.listItems() == null
                ? Collections.emptyList()
                : ctx.listItems().expr().stream().map(this::toExpression).collect(Collectors.toList());
        return new ListExpr(lineOf(ctx), items);
    }

    @Override
    public Object visitDictExpr(PythonParser.DictExprContext ctx) {
        List<DictEntry> entries = Collections.emptyList();
        if (ctx.dictItems() != null) {
            entries = ctx.dictItems().dictItem().stream()
                    .map(di -> {
                        Expression key;
                        if (di.STRING_LITERAL() != null) {
                            key = new StringLiteral(lineOf(di), unquote(di.STRING_LITERAL().getText()));
                        } else {
                            key = new Identifier(lineOf(di), di.IDENTIFIER().getText());
                        }
                        Expression value = toExpression(di.expr());
                        return new DictEntry(lineOf(di), key, value);
                    })
                    .collect(Collectors.toList());
        }
        return new DictExpr(lineOf(ctx), entries);
    }

    @Override
    public Object visitStringLiteral(PythonParser.StringLiteralContext ctx) {
        return new StringLiteral(lineOf(ctx), unquote(ctx.STRING_LITERAL().getText()));
    }

    @Override
    public Object visitIntLiteral(PythonParser.IntLiteralContext ctx) {
        int v = Integer.parseInt(ctx.INTEGER_LITERAL().getText());
        return new IntegerLiteral(lineOf(ctx), v);
    }

    @Override
    public Object visitFloatLiteral(PythonParser.FloatLiteralContext ctx) {
        double v = Double.parseDouble(ctx.FLOAT_LITERAL().getText());
        return new FloatLiteral(lineOf(ctx), v);
    }

    @Override
    public Object visitTrueLiteral(PythonParser.TrueLiteralContext ctx) {
        return new BooleanLiteral(lineOf(ctx), true);
    }

    @Override
    public Object visitFalseLiteral(PythonParser.FalseLiteralContext ctx) {
        return new BooleanLiteral(lineOf(ctx), false);
    }

    @Override
    public Object visitNoneLiteral(PythonParser.NoneLiteralContext ctx) {
        return new NoneLiteral(lineOf(ctx));
    }

    @Override
    public Object visitIdentifierAtom(PythonParser.IdentifierAtomContext ctx) {
        // IDENTIFIER atomTrailer*
        Expression base = new Identifier(lineOf(ctx), ctx.IDENTIFIER().getText());
        for (PythonParser.AtomTrailerContext t : ctx.atomTrailer()) {
            base = applyTrailer(base, t);
        }
        return base;
    }

    /* -------------------------
       AtomTrailer helpers
       ------------------------- */

    // Replace your existing applyTrailer(...) with this implementation
    private Expression applyTrailer(Expression base, compiler.generated.python.PythonParser.AtomTrailerContext t) {

        // atomTrailer alternatives (labeled):
        //   DOT IDENTIFIER                       #attributeAccess
        //   | LBRACK expr RBRACK                 #indexAccess
        //   | LPAREN argList? RPAREN             #callTrailer

        // Attribute access: subclass is PythonParser.AttributeAccessContext
        if (t instanceof compiler.generated.python.PythonParser.AttributeAccessContext) {
            compiler.generated.python.PythonParser.AttributeAccessContext ac =
                    (compiler.generated.python.PythonParser.AttributeAccessContext) t;
            // DOT IDENTIFIER -> IDENTIFIER() exists on the AttributeAccessContext
            String attr = ac.IDENTIFIER().getText();
            return new AttributeAccess(lineOf(ac), base, attr);
        }

        // Index access: subclass is PythonParser.IndexAccessContext
        if (t instanceof compiler.generated.python.PythonParser.IndexAccessContext) {
            compiler.generated.python.PythonParser.IndexAccessContext ic =
                    (compiler.generated.python.PythonParser.IndexAccessContext) t;
            Expression idx = toExpression(ic.expr());
            return new IndexAccess(lineOf(ic), base, idx);
        }

        // Call trailer: subclass is PythonParser.CallTrailerContext
        if (t instanceof compiler.generated.python.PythonParser.CallTrailerContext) {
            compiler.generated.python.PythonParser.CallTrailerContext cc =
                    (compiler.generated.python.PythonParser.CallTrailerContext) t;
            List<Argument> args = cc.argList() == null ? Collections.emptyList() : toArgumentList(cc.argList());
            return new CallExpr(lineOf(cc), base, args);
        }

        throw new IllegalStateException("Unknown atomTrailer alternative at line " + lineOf(t));
    }



    /* -------------------------
       Arguments
       ------------------------- */

    @Override
    public Object visitPositionalArg(PythonParser.PositionalArgContext ctx) {
        Expression e = toExpression(ctx.expr());
        return new PositionalArgument(lineOf(ctx), e);
    }

    @Override
    public Object visitKeywordArg(PythonParser.KeywordArgContext ctx) {
        String name = ctx.IDENTIFIER().getText();
        Expression value = toExpression(ctx.expr());
        return new KeywordArgument(lineOf(ctx), name, value);
    }

    @Override
    public Object visitArgList(PythonParser.ArgListContext ctx) {
        // Return a List<Argument> as Object to match base visitor signature
        return toArgumentList(ctx);
    }

    /* -------------------------
       Suite handling (single-line and indented)
       ------------------------- */

    // Replace your existing visitSuiteAsStatements(...) with this implementation
    private List<Statement> visitSuiteAsStatements(compiler.generated.python.PythonParser.SuiteContext suite) {
        if (suite == null) return Collections.emptyList();

        List<Statement> stmts = new ArrayList<>();

        // Indented suite: NEWLINE INDENT statement+ DEDENT
        if (suite instanceof compiler.generated.python.PythonParser.IndentedSuiteContext) {
            compiler.generated.python.PythonParser.IndentedSuiteContext ind =
                    (compiler.generated.python.PythonParser.IndentedSuiteContext) suite;

            // ind.statement() is available on IndentedSuiteContext
            for (compiler.generated.python.PythonParser.StatementContext s : ind.statement()) {
                stmts.add(toStatement(s));
            }
            return stmts;
        }

        // Single-line suite: simpleLine
        if (suite instanceof compiler.generated.python.PythonParser.SingleSuiteContext) {
            compiler.generated.python.PythonParser.SingleSuiteContext single =
                    (compiler.generated.python.PythonParser.SingleSuiteContext) suite;

            // single.simpleLine() returns the SimpleLineContext
            compiler.generated.python.PythonParser.SimpleLineContext sl = single.simpleLine();
            if (sl != null) {
                List<compiler.generated.python.PythonParser.ExprContext> exprs = sl.expr();
                if (sl.ASSIGN().size() > 0) {
                    List<Expression> targets = exprs.subList(0, exprs.size() - 1).stream()
                            .map(this::toExpression)
                            .collect(Collectors.toList());
                    Expression value = toExpression(exprs.get(exprs.size() - 1));
                    stmts.add(new Assign(lineOf(sl), targets, value));
                } else {
                    stmts.add(new ExprStmt(lineOf(sl), toExpression(exprs.get(0))));
                }
            }
            return stmts;
        }

        // Fallback: try to find statement children (defensive)
        for (int i = 0; i < suite.getChildCount(); i++) {
            org.antlr.v4.runtime.tree.ParseTree child = suite.getChild(i);
            if (child instanceof compiler.generated.python.PythonParser.StatementContext) {
                stmts.add(toStatement((compiler.generated.python.PythonParser.StatementContext) child));
            }
        }
        return stmts;
    }


    /* -------------------------
       Fallbacks
       ------------------------- */

    @Override
    public Object visitChildren(org.antlr.v4.runtime.tree.RuleNode node) {
        return super.visitChildren(node);
    }

    @Override
    protected Object defaultResult() {
        return null;
    }
}
