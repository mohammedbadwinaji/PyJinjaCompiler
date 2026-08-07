package compiler.ast.common;

/**
 * Base interface for all AST nodes.
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
