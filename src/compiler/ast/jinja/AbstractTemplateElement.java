package compiler.ast.jinja;

import compiler.ast.common.AbstractAstNode;

/**
 * Base class for all Jinja template element nodes
 * (the Jinja-tree equivalent of AbstractStatement.java on the Python side).
 */
public abstract class AbstractTemplateElement
        extends AbstractAstNode
        implements TemplateElement {

    protected AbstractTemplateElement(String nodeName, int line) {
        super(nodeName, line);
    }

}
