package compiler.ast.python;

/**
 * Represents an AST node that can print itself in a readable tree format.
 */
public interface PrettyPrintable {

    String prettyPrint(String indent);

}