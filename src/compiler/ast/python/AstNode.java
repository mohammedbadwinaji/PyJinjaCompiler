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