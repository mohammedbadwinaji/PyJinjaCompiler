package compiler.ast.python;

import compiler.ast.common.AstVisitor;
import compiler.ast.common.AbstractAstNode;
import compiler.ast.common.Expression;

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