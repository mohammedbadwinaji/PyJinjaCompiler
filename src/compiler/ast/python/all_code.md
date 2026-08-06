Python AST Nodes
### ?? File: AbstractAstNode.java 
 
package compiler.ast.python;

import java.util.Objects;

/**
 * Shared implementation for all AST nodes.
 */
public abstract class AbstractAstNode implements AstNode {

    private final String nodeName;
    private final int line;

    protected AbstractAstNode(String nodeName, int line) {

        this.nodeName = Objects.requireNonNull(nodeName);
        this.line = line;
    }

    @Override
    public final String getNodeName() {
        return nodeName;
    }

    @Override
    public final int getLine() {
        return line;
    }

    @Override
    public String prettyPrint(String indent) {

        return indent +
                nodeName +
                " (line " +
                line +
                ")";
    }

    @Override
    public String toString() {
        return prettyPrint("");
    }

} 

### ?? File: AbstractExpression.java 
 
package compiler.ast.python;

/**
 * Base class for all expression nodes.
 */
public abstract class AbstractExpression
        extends AbstractAstNode
        implements Expression {

    protected AbstractExpression(String nodeName, int line) {
        super(nodeName, line);
    }

} 

### ?? File: AbstractStatement.java 
 
package compiler.ast.python;

/**
 * Base class for all statement nodes.
 */
public abstract class AbstractStatement
        extends AbstractAstNode
        implements Statement {

    protected AbstractStatement(String nodeName, int line) {
        super(nodeName, line);
    }

} 

### ?? File: Argument.java 
 
package compiler.ast.python;

/**
 * Base interface for function arguments.
 */
public interface Argument extends AstNode {
} 

### ?? File: Assign.java 
 
package compiler.ast.python;

import java.util.List;
import java.util.Objects;

/**
 * Assignment statement.
 */
public final class Assign extends AbstractStatement {

    private final List<Expression> targets;
    private final Expression value;

    public Assign(
            int line,
            List<Expression> targets,
            Expression value) {

        super("Assign", line);

        this.targets = List.copyOf(
                Objects.requireNonNull(targets));

        this.value = Objects.requireNonNull(value);
    }

    public List<Expression> getTargets() {
        return targets;
    }

    public Expression getValue() {
        return value;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitAssign(this);
    }

    @Override
    public String prettyPrint(String indent) {

        StringBuilder sb = new StringBuilder();

        sb.append(indent)
                .append("Assign [line ")
                .append(getLine())
                .append("]");

        sb.append("\n")
                .append(indent)
                .append("  Targets");

        for (Expression e : targets) {

            sb.append("\n")
                    .append(e.prettyPrint(indent + "    "));
        }

        sb.append("\n")
                .append(indent)
                .append("  Value\n")
                .append(value.prettyPrint(indent + "    "));

        return sb.toString();
    }

} 

### ?? File: AstNode.java 
 
package compiler.ast.python;

/**
 * Base interface for all Python AST nodes.
 */
public interface AstNode extends PrettyPrintable {

    /**
     * Node type name.
     */
    String getNodeName();

    /**
     * Source line.
     */
    int getLine();

    /**
     * Visitor entry.
     */
    <T> T accept(AstVisitor<T> visitor);

} 

### ?? File: AstVisitor.java 
 
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

    T visitWhileStmt(WhileStmt node);

    T visitFunctionDef(FunctionDef node);
} 

### ?? File: AttributeAccess.java 
 
package compiler.ast.python;

import java.util.Objects;

/**
 * obj.attribute
 */
public final class AttributeAccess extends AbstractExpression {

    private final Expression target;

    private final String attribute;

    public AttributeAccess(
            int line,
            Expression target,
            String attribute) {

        super("AttributeAccess", line);

        this.target = Objects.requireNonNull(target);
        this.attribute = Objects.requireNonNull(attribute);
    }

    public Expression getTarget() {
        return target;
    }

    public String getAttribute() {
        return attribute;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitAttributeAccess(this);
    }

} 

### ?? File: BinaryExpr.java 
 
package compiler.ast.python;

import java.util.Objects;

/**
 * Binary expression.
 */
public final class BinaryExpr extends AbstractExpression {

    private final Expression left;

    private final BinaryOperator operator;

    private final Expression right;

    public BinaryExpr(
            int line,
            Expression left,
            BinaryOperator operator,
            Expression right) {

        super("BinaryExpr", line);

        this.left = Objects.requireNonNull(left);
        this.operator = Objects.requireNonNull(operator);
        this.right = Objects.requireNonNull(right);
    }

    public Expression getLeft() {
        return left;
    }

    public BinaryOperator getOperator() {
        return operator;
    }

    public Expression getRight() {
        return right;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitBinaryExpr(this);
    }

    @Override
    public String prettyPrint(String indent) {

        String child = indent + "  ";

        return indent + "BinaryExpr (" + operator + ") [line " + getLine() + "]\n"
                + left.prettyPrint(child) + "\n"
                + right.prettyPrint(child);
    }

} 

### ?? File: BinaryOperator.java 
 
package compiler.ast.python;

/**
 * Supported binary operators.
 */
public enum BinaryOperator {

    OR,

    AND,

    EQ,
    NE,
    LT,
    LE,
    GT,
    GE,

    ADD,
    SUBTRACT,

    MULTIPLY,
    DIVIDE,
    MODULO

} 

### ?? File: BooleanLiteral.java 
 
package compiler.ast.python;

/**
 * Boolean literal.
 */
public final class BooleanLiteral
        extends LiteralExpression {

    private final boolean value;

    public BooleanLiteral(int line, boolean value) {

        super("BooleanLiteral", line);

        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitBooleanLiteral(this);
    }

    @Override
    public String prettyPrint(String indent) {

        return indent +
                getNodeName() +
                " (" +
                value +
                ") [line " +
                getLine() +
                "]";
    }

} 

### ?? File: CallExpr.java 
 
package compiler.ast.python;

import java.util.List;
import java.util.Objects;

public final class CallExpr extends AbstractExpression {

    private final Expression callee;

    private final List<Argument> arguments;

    public CallExpr(
            int line,
            Expression callee,
            List<Argument> arguments) {

        super("CallExpr", line);

        this.callee = Objects.requireNonNull(callee);
        this.arguments = List.copyOf(arguments);
    }

    public Expression getCallee() {
        return callee;
    }

    public List<Argument> getArguments() {
        return arguments;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitCallExpr(this);
    }

    @Override
    public String prettyPrint(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("CallExpr (line ").append(getLine()).append(")");
        sb.append("\n").append(indent).append("  Callee");
        sb.append("\n").append(callee.prettyPrint(indent + "    "));
        sb.append("\n").append(indent).append("  Arguments");
        if (arguments.isEmpty()) {
            sb.append(" []");
        } else {
            for (Argument arg : arguments) {
                sb.append("\n").append(arg.prettyPrint(indent + "    "));
            }
        }
        return sb.toString();
    }


} 

### ?? File: Decorator.java 
 
package compiler.ast.python;

import java.util.List;
import java.util.Objects;

public final class Decorator extends AbstractAstNode {

    private final Expression target;

    private final List<Argument> arguments;

    public Decorator(
            int line,
            Expression target,
            List<Argument> arguments) {

        super("Decorator", line);

        this.target = Objects.requireNonNull(target);
        this.arguments = List.copyOf(
                Objects.requireNonNull(arguments));
    }

    public Expression getTarget() {
        return target;
    }

    public List<Argument> getArguments() {
        return arguments;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitDecorator(this);
    }

    @Override
    public String prettyPrint(String indent) {

        StringBuilder sb = new StringBuilder();

        sb.append(indent)
                .append("Decorator [line ")
                .append(getLine())
                .append("]");

        sb.append("\n")
                .append(target.prettyPrint(indent + "  "));

        for (Argument arg : arguments) {
            sb.append("\n")
                    .append(arg.prettyPrint(indent + "  "));
        }

        return sb.toString();
    }
} 

### ?? File: DictEntry.java 
 
package compiler.ast.python;

import java.util.Objects;

/**
 * Single dictionary entry.
 */
public final class DictEntry extends AbstractAstNode {

    private final Expression key;

    private final Expression value;

    public DictEntry(
            int line,
            Expression key,
            Expression value) {

        super("DictEntry", line);

        this.key = Objects.requireNonNull(key);
        this.value = Objects.requireNonNull(value);
    }

    public Expression getKey() {
        return key;
    }

    public Expression getValue() {
        return value;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitDictEntry(this);
    }

    @Override
    public String prettyPrint(String indent) {

        return indent + "DictEntry\n"
                + key.prettyPrint(indent + "  ")
                + "\n"
                + value.prettyPrint(indent + "  ");
    }

} 

### ?? File: DictExpr.java 
 
package compiler.ast.python;

import java.util.List;
import java.util.Objects;

/**
 * Python dictionary literal.
 */
public final class DictExpr extends LiteralExpression {

    private final List<DictEntry> entries;

    public DictExpr(
            int line,
            List<DictEntry> entries) {

        super("DictExpr", line);

        this.entries = List.copyOf(
                Objects.requireNonNull(entries)
        );
    }

    public List<DictEntry> getEntries() {
        return entries;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitDictExpr(this);
    }

    @Override
    public String prettyPrint(String indent) {

        StringBuilder sb = new StringBuilder();

        sb.append(indent)
                .append(getNodeName())
                .append(" [line ")
                .append(getLine())
                .append("]");

        for (DictEntry entry : entries) {

            sb.append("\n")
                    .append(entry.prettyPrint(indent + "  "));
        }

        return sb.toString();
    }

} 

### ?? File: ElifClause.java 
 
package compiler.ast.python;

import java.util.List;
import java.util.Objects;

public final class ElifClause
        extends AbstractAstNode {

    private final Expression condition;

    private final List<Statement> body;

    public ElifClause(
            int line,
            Expression condition,
            List<Statement> body) {

        super("ElifClause", line);

        this.condition =
                Objects.requireNonNull(condition);

        this.body =
                List.copyOf(body);
    }

    public Expression getCondition() {
        return condition;
    }

    public List<Statement> getBody() {
        return body;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitElifClause(this);
    }

} 

### ?? File: Expression.java 
 
package compiler.ast.python;

/**
 * Marker interface for expressions.
 */
public interface Expression extends AstNode {
} 

### ?? File: ExprStmt.java 
 
package compiler.ast.python;

import java.util.Objects;

/**
 * Expression statement.
 *
 * Example:
 *
 * print(x)
 */
public final class ExprStmt extends AbstractStatement {

    private final Expression expression;

    public ExprStmt(
            int line,
            Expression expression) {

        super("ExprStmt", line);

        this.expression =
                Objects.requireNonNull(expression);
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitExprStmt(this);
    }

    @Override
    public String prettyPrint(String indent) {

        StringBuilder sb = new StringBuilder();

        sb.append(indent)
                .append("ExprStmt [line ")
                .append(getLine())
                .append("]");

        sb.append("\n")
                .append(expression.prettyPrint(indent + "  "));

        return sb.toString();
    }
} 

### ?? File: FloatLiteral.java 
 
package compiler.ast.python;

/**
 * Floating-point literal.
 */
public final class FloatLiteral
        extends LiteralExpression {

    private final double value;

    public FloatLiteral(int line, double value) {

        super("FloatLiteral", line);

        this.value = value;
    }

    public double getValue() {
        return value;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitFloatLiteral(this);
    }

    @Override
    public String prettyPrint(String indent) {

        return indent +
                getNodeName() +
                " (" +
                value +
                ") [line " +
                getLine() +
                "]";
    }

} 

### ?? File: ForStmt.java 
 
package compiler.ast.python;

import java.util.List;
import java.util.Objects;

public final class ForStmt
        extends AbstractStatement {

    private final String variable;

    private final Expression iterable;

    private final List<Statement> body;

    public ForStmt(
            int line,
            String variable,
            Expression iterable,
            List<Statement> body) {

        super("ForStmt", line);

        this.variable =
                Objects.requireNonNull(variable);

        this.iterable =
                Objects.requireNonNull(iterable);

        this.body =
                List.copyOf(body);
    }

    public String getVariable() {
        return variable;
    }

    public Expression getIterable() {
        return iterable;
    }

    public List<Statement> getBody() {
        return body;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {

        return visitor.visitForStmt(this);
    }

    @Override
    public String prettyPrint(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("ForStmt (line ").append(getLine()).append(")");
        sb.append("\n").append(indent).append("  Variable: ").append(variable);
        sb.append("\n").append(indent).append("  Iterable");
        sb.append("\n").append(iterable.prettyPrint(indent + "    "));
        sb.append("\n").append(indent).append("  Body");
        for (Statement stmt : body) {
            sb.append("\n").append(stmt.prettyPrint(indent + "    "));
        }
        return sb.toString();
    }

} 

### ?? File: FunctionDef.java 
 
package compiler.ast.python;

import java.util.List;
import java.util.Objects;

/**
 * Python function definition.
 */
public final class FunctionDef extends AbstractStatement {

    private final String name;

    private final List<FunctionParameter> parameters;

    private final List<Decorator> decorators;

    private final List<Statement> body;

    public FunctionDef(
            int line,
            String name,
            List<FunctionParameter> parameters,
            List<Decorator> decorators,
            List<Statement> body) {

        super("FunctionDef", line);

        this.name = Objects.requireNonNull(name);
        this.parameters = List.copyOf(
                Objects.requireNonNull(parameters));
        this.decorators = List.copyOf(
                Objects.requireNonNull(decorators));
        this.body = List.copyOf(
                Objects.requireNonNull(body));
    }

    public String getName() {
        return name;
    }

    public List<FunctionParameter> getParameters() {
        return parameters;
    }

    public List<Decorator> getDecorators() {
        return decorators;
    }

    public List<Statement> getBody() {
        return body;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitFunctionDef(this);
    }

    @Override
    public String prettyPrint(String indent) {

        StringBuilder sb = new StringBuilder();

        sb.append(indent)
                .append("FunctionDef(")
                .append(name)
                .append(") [line ")
                .append(getLine())
                .append("]");

        if (!decorators.isEmpty()) {

            sb.append("\n")
                    .append(indent)
                    .append("  Decorators");

            for (Decorator decorator : decorators) {
                sb.append("\n")
                        .append(decorator.prettyPrint(indent + "    "));
            }
        }

        if (!parameters.isEmpty()) {

            sb.append("\n")
                    .append(indent)
                    .append("  Parameters");

            for (FunctionParameter parameter : parameters) {
                sb.append("\n")
                        .append(parameter.prettyPrint(indent + "    "));
            }
        }

        sb.append("\n")
                .append(indent)
                .append("  Body");

        for (Statement stmt : body) {
            sb.append("\n")
                    .append(stmt.prettyPrint(indent + "    "));
        }

        return sb.toString();
    }
} 

### ?? File: FunctionParameter.java 
 
package compiler.ast.python;

import java.util.Objects;

/**
 * Represents a function parameter.
 */
public final class FunctionParameter extends AbstractAstNode {

    private final String name;

    public FunctionParameter(int line, String name) {

        super("FunctionParameter", line);

        this.name = Objects.requireNonNull(name);
    }

    public String getName() {
        return name;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitFunctionParameter(this);
    }

    @Override
    public String prettyPrint(String indent) {

        return indent +
                "Parameter(" +
                name +
                ") [line " +
                getLine() +
                "]";
    }
} 

### ?? File: Identifier.java 
 
package compiler.ast.python;

import java.util.Objects;

/**
 * Identifier expression.
 */
public final class Identifier extends AbstractExpression {

    private final String name;

    public Identifier(int line, String name) {

        super("Identifier", line);

        this.name = Objects.requireNonNull(name);
    }

    public String getName() {
        return name;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitIdentifier(this);
    }

    @Override
    public String prettyPrint(String indent) {

        return indent +
                getNodeName() +
                " (" +
                name +
                ") [line " +
                getLine() +
                "]";
    }

} 

### ?? File: IfStmt.java 
 
package compiler.ast.python;

import java.util.List;
import java.util.Objects;

public final class IfStmt extends AbstractStatement {

    private final Expression condition;

    private final List<Statement> thenBody;

    private final List<ElifClause> elifClauses;

    private final List<Statement> elseBody;

    public IfStmt(
            int line,
            Expression condition,
            List<Statement> thenBody,
            List<ElifClause> elifClauses,
            List<Statement> elseBody) {

        super("IfStmt", line);

        this.condition = Objects.requireNonNull(condition);
        this.thenBody = List.copyOf(
                Objects.requireNonNull(thenBody));
        this.elifClauses = List.copyOf(
                Objects.requireNonNull(elifClauses));
        this.elseBody = List.copyOf(
                Objects.requireNonNull(elseBody));
    }

    public Expression getCondition() {
        return condition;
    }

    public List<Statement> getThenBody() {
        return thenBody;
    }

    public List<ElifClause> getElifClauses() {
        return elifClauses;
    }

    public List<Statement> getElseBody() {
        return elseBody;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitIfStmt(this);
    }
} 

### ?? File: IndexAccess.java 
 
package compiler.ast.python;

import java.util.Objects;

/**
 * target[index]
 */
public final class IndexAccess extends AbstractExpression {

    private final Expression target;

    private final Expression index;

    public IndexAccess(
            int line,
            Expression target,
            Expression index) {

        super("IndexAccess", line);

        this.target = Objects.requireNonNull(target);
        this.index = Objects.requireNonNull(index);
    }

    public Expression getTarget() {
        return target;
    }

    public Expression getIndex() {
        return index;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitIndexAccess(this);
    }

} 

### ?? File: IntegerLiteral.java 
 
package compiler.ast.python;

/**
 * Integer literal.
 */
public final class IntegerLiteral
        extends LiteralExpression {

    private final long value;

    public IntegerLiteral(int line, long value) {

        super("IntegerLiteral", line);

        this.value = value;
    }

    public long getValue() {
        return value;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitIntegerLiteral(this);
    }

    @Override
    public String prettyPrint(String indent) {

        return indent +
                getNodeName() +
                " (" +
                value +
                ") [line " +
                getLine() +
                "]";
    }

} 

### ?? File: KeywordArgument.java 
 
package compiler.ast.python;

import java.util.Objects;

public final class KeywordArgument extends AbstractAstNode implements Argument {

    private final String name;

    private final Expression value;

    public KeywordArgument(
            int line,
            String name,
            Expression value) {

        super("KeywordArgument", line);

        this.name = Objects.requireNonNull(name);
        this.value = Objects.requireNonNull(value);
    }

    public String getName() {
        return name;
    }

    public Expression getValue() {
        return value;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitKeywordArgument(this);
    }

    @Override
    public String prettyPrint(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("KeywordArgument (").append(name).append(") [line ").append(getLine()).append("]");
        sb.append("\n").append(value.prettyPrint(indent + "  "));
        return sb.toString();
    }

} 

### ?? File: ListExpr.java 
 
package compiler.ast.python;

import java.util.List;
import java.util.Objects;

/**
 * Python list literal.
 */
public final class ListExpr extends LiteralExpression {

    private final List<Expression> elements;

    public ListExpr(
            int line,
            List<Expression> elements) {

        super("ListExpr", line);

        this.elements = List.copyOf(
                Objects.requireNonNull(elements)
        );
    }

    public List<Expression> getElements() {
        return elements;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitListExpr(this);
    }

    @Override
    public String prettyPrint(String indent) {

        StringBuilder sb = new StringBuilder();

        sb.append(indent)
                .append(getNodeName())
                .append(" [line ")
                .append(getLine())
                .append("]");

        for (Expression expression : elements) {

            sb.append("\n")
                    .append(expression.prettyPrint(indent + "  "));
        }

        return sb.toString();
    }

} 

### ?? File: LiteralExpression.java 
 
package compiler.ast.python;

/**
 * Base class for all literal expressions.
 *
 * Examples:
 * - Integer
 * - Float
 * - String
 * - Boolean
 * - None
 * - List
 * - Dictionary
 */
public abstract class LiteralExpression
        extends AbstractExpression {

    protected LiteralExpression(
            String nodeName,
            int line) {

        super(nodeName, line);
    }

} 

### ?? File: NoneLiteral.java 
 
package compiler.ast.python;

/**
 * Python None literal.
 */
public final class NoneLiteral
        extends LiteralExpression {

    public NoneLiteral(int line) {
        super("NoneLiteral", line);
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitNoneLiteral(this);
    }

    @Override
    public String prettyPrint(String indent) {

        return indent +
                getNodeName() +
                " [line " +
                getLine() +
                "]";
    }

} 

### ?? File: PositionalArgument.java 
 
package compiler.ast.python;

import java.util.Objects;

public final class PositionalArgument extends AbstractAstNode implements Argument {

    private final Expression value;

    public PositionalArgument(int line, Expression value) {

        super("PositionalArgument", line);

        this.value = Objects.requireNonNull(value);
    }

    public Expression getValue() {
        return value;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitPositionalArgument(this);
    }

    @Override
    public String prettyPrint(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("PositionalArgument (line ").append(getLine()).append(")");
        sb.append("\n").append(value.prettyPrint(indent + "  "));
        return sb.toString();
    }


} 

### ?? File: PrettyPrintable.java 
 
package compiler.ast.python;

/**
 * Represents an AST node that can print itself in a readable tree format.
 */
public interface PrettyPrintable {

    String prettyPrint(String indent);

} 

### ?? File: Program.java 
 
package compiler.ast.python;

import java.util.List;
import java.util.Objects;

/**
 * Root of the Python AST.
 */
public final class Program extends AbstractAstNode {

    private final List<Statement> statements;

    public Program(
            int line,
            List<Statement> statements) {

        super("Program", line);

        this.statements = List.copyOf(
                Objects.requireNonNull(statements)
        );
    }

    public List<Statement> getStatements() {
        return statements;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitProgram(this);
    }

    @Override
    public String prettyPrint(String indent) {

        StringBuilder builder = new StringBuilder();

        builder.append(super.prettyPrint(indent));

        for (Statement stmt : statements) {

            builder.append(System.lineSeparator())
                    .append(stmt.prettyPrint(indent + "  "));
        }

        return builder.toString();
    }

} 

### ?? File: ReturnStmt.java 
 
package compiler.ast.python;

import java.util.Optional;

/**
 * return statement.
 */
public final class ReturnStmt
        extends AbstractStatement {

    private final Expression value;

    public ReturnStmt(
            int line,
            Expression value) {

        super("ReturnStmt", line);

        this.value = value;
    }

    public Optional<Expression> getValue() {

        return Optional.ofNullable(value);
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {

        return visitor.visitReturnStmt(this);
    }

    @Override
    public String prettyPrint(String indent) {
        if (value == null) {
            return indent + "ReturnStmt [line " + getLine() + "]";
        }
        return indent + "ReturnStmt [line " + getLine() + "]\n" + value.prettyPrint(indent + "  ");
    }

} 

### ?? File: Statement.java 
 
package compiler.ast.python;

/**
 * Marker interface for statements.
 */
public interface Statement extends AstNode {
} 

### ?? File: StringLiteral.java 
 
package compiler.ast.python;

import java.util.Objects;

/**
 * String literal.
 */
public final class StringLiteral
        extends LiteralExpression {

    private final String value;

    public StringLiteral(int line, String value) {

        super("StringLiteral", line);

        this.value = Objects.requireNonNull(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitStringLiteral(this);
    }

    @Override
    public String prettyPrint(String indent) {

        return indent +
                getNodeName() +
                " (\"" +
                value +
                "\") [line " +
                getLine() +
                "]";
    }

} 

### ?? File: UnaryExpr.java 
 
package compiler.ast.python;


public class UnaryExpr extends AbstractExpression  {

    private  final UnaryOperator operator;
    private final Expression expr;
    public UnaryExpr(
            int line,
            UnaryOperator operator,
            Expression expr
    ){

        super("UnaryExpr", line);
        this.operator = operator;
        this.expr = expr;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return null;
    }
}
 

### ?? File: UnaryOperator.java 
 
package compiler.ast.python;
public enum UnaryOperator {
    PLUS,
    MINUS,
    NOT
} 

### ?? File: WhileStmt.java 
 
package compiler.ast.python;

import java.util.List;
import java.util.Objects;

public final class WhileStmt
        extends AbstractStatement {

    private final Expression condition;

    private final List<Statement> body;

    public WhileStmt(
            int line,
            Expression condition,
            List<Statement> body) {

        super("WhileStmt", line);

        this.condition = Objects.requireNonNull(condition);
        this.body = List.copyOf(body);
    }

    public Expression getCondition() {
        return condition;
    }

    public List<Statement> getBody() {
        return body;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitWhileStmt(this);
    }

    @Override
    public String prettyPrint(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("WhileStmt (line ").append(getLine()).append(")");
        sb.append("\n").append(indent).append("  Condition");
        sb.append("\n").append(condition.prettyPrint(indent + "    "));
        sb.append("\n").append(indent).append("  Body");
        for (Statement stmt : body) {
            sb.append("\n").append(stmt.prettyPrint(indent + "    "));
        }
        return sb.toString();
    }
}
 
______________________________________
Jinja Lexer And Parser Grammar


### ?? File: JinjaLexer.java
lexer grammar JinjaLexer;

// Default mode - outside Jinja tags
HTML_TEXT : (~[{] | '{' ~['{%#])+ ;

JINJA_EXPR_START : '{{' -> pushMode(JINJA_MODE);
JINJA_STMT_START : '{%' -> pushMode(JINJA_MODE);
JINJA_COMMENT_START : '{#' -> pushMode(COMMENT_MODE);

// Jinja mode - inside {{ }} or {% %}
mode JINJA_MODE;
JINJA_EXPR_END : '}}' -> popMode;
JINJA_STMT_END : '%}' -> popMode;

IF      : 'if';
ELIF    : 'elif';
ELSE    : 'else';
ENDIF   : 'endif';
FOR     : 'for';
IN      : 'in';
ENDFOR  : 'endfor';

AND     : 'and';
OR      : 'or';
NOT     : 'not';

TRUE    : 'true';
FALSE   : 'false';
NONE    : 'none';

EQ      : '==';
NE      : '!=';
LT      : '<';
GT      : '>';
LE      : '<=';
GE      : '>=';

PLUS    : '+';
MINUS   : '-';
STAR    : '*';
SLASH   : '/';
MOD     : '%';

DOT     : '.';
LPAREN  : '(';
RPAREN  : ')';
LBRACK  : '[';
RBRACK  : ']';
COMMA   : ',';

STRING  : '"' (~["\\] | '\\' .)* '"' ;
INT     : [0-9]+;
FLOAT   : [0-9]+ '.' [0-9]+;

IDENTIFIER : [_a-zA-Z] [_a-zA-Z0-9]*;

WS : [ \t\r\n]+ -> skip;

// Comment mode - inside {# }
mode COMMENT_MODE;
JINJA_COMMENT_END : '#}' -> popMode;
COMMENT_TEXT : ~[#]+ ;



### ?? File: JinjaParser.java

parser grammar JinjaParser;

options { tokenVocab = JinjaLexer; }

// ===============================
// TEMPLATE ROOT
// ===============================
template
: element* EOF

    ;

// ===============================
// ELEMENTS
// ===============================
element
: HTML_TEXT              #HtmlElement
| jinjaExpr              #ExprElement
| jinjaStmt              #StmtElement
| jinjaComment           #CommentElement
;

// ===============================
// {{ expression }}
// ===============================
jinjaExpr
: JINJA_EXPR_START expr JINJA_EXPR_END
#JinjaExpression
;

// ===============================
// {% statement %}
//
// ===============================
jinjaStmt
: JINJA_STMT_START stmt JINJA_STMT_END
#JinjaStatement
;

// ===============================
// {# comment #}
// ===============================
jinjaComment
: JINJA_COMMENT_START COMMENT_TEXT? JINJA_COMMENT_END
#Comment
;


// ===============================
// STATEMENTS
// ===============================
stmt
: ifStmt     #IfStatement
| forStmt    #ForStatement
;

// ===============================
// IF / ELIF / ELSE / ENDIF
// ===============================
ifStmt
: IF expr        #IfOpen
| ELIF expr      #ElifOpen
| ELSE           #ElseOpen
| ENDIF          #IfClose
;

// ===============================
// FOR / ENDFOR
// ===============================
forStmt
: FOR IDENTIFIER IN expr   #ForOpen
| ENDFOR                   #ForClose
;

// ===============================
// EXPRESSIONS (with proper precedence)
// ===============================
expr
: logicalOr                       #LogicalOrRoot
;

logicalOr
: logicalOr OR logicalAnd         #OrExpr
| logicalAnd                      #AndRoot
;

logicalAnd
: logicalAnd AND comparison       #AndExpr
| comparison                      #ComparisonRoot
;

comparison
: comparison (EQ|NE|LT|GT|LE|GE) additive  #CompareExpr
| additive                        #AdditiveRoot
;

additive
: additive (PLUS|MINUS) multiplicative #AddExpr
| multiplicative                 #MultiplicativeRoot
;

multiplicative
: multiplicative (STAR|SLASH|MOD) unary #MulExpr
| unary                          #UnaryRoot
;

unary
: NOT unary                       #NotExpr
| MINUS unary                     #UnaryMinusExpr
| atom                           #AtomRoot
;

// ===============================
// ATOMS
// ===============================
atom
: LPAREN expr RPAREN              #ParenAtom
| IDENTIFIER trailer*              #IdentifierAtom
| STRING                           #StringAtom
| INT                              #IntAtom
| FLOAT                            #FloatAtom
| TRUE                             #TrueAtom
| FALSE                            #FalseAtom
| NONE                             #NoneAtom
;

// ===============================
// TRAILERS
// ===============================
trailer
: DOT IDENTIFIER                   #AttributeTrailer
| LBRACK expr RBRACK               #IndexTrailer
| LPAREN argList? RPAREN           #CallTrailer
;

// ===============================
// ARGUMENT LIST
// ===============================
argList
: expr (COMMA expr)* (COMMA)?
;
