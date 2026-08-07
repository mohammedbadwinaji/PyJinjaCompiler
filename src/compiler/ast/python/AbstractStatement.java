package compiler.ast.python;

import compiler.ast.common.AbstractAstNode;

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