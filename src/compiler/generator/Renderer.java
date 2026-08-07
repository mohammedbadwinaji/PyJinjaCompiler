package compiler.generator;

import compiler.ast.jinja.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Renderer: the actual "code generation" step (requirement 5). Walks a
 * Jinja Template AST together with a data context (typically
 * Generator.RenderCall.getContextValues()) and produces the final
 * rendered HTML as a String - proving the Python-extracted data and the
 * Jinja tree work together end to end.
 *
 * Expression evaluation ({{ ... }}, {% if ... %}, {% for ... %}'s
 * iterable) is delegated to ExpressionEvaluator, the same evaluator
 * Generator uses to pull data out of the Python tree in the first
 * place.
 */
public final class Renderer {

    private final ExpressionEvaluator evaluator = new ExpressionEvaluator();

    public String render(Template template, Map<String, Object> context) {
        StringBuilder out = new StringBuilder();
        renderElements(template.getElements(), context, out);
        return out.toString();
    }

    private void renderElements(List<TemplateElement> elements, Map<String, Object> env, StringBuilder out) {
        for (TemplateElement element : elements) {
            renderElement(element, env, out);
        }
    }

    private void renderElement(TemplateElement element, Map<String, Object> env, StringBuilder out) {

        if (element instanceof HtmlText) {
            out.append(((HtmlText) element).getText());

        } else if (element instanceof ExpressionOutput) {
            Object value = evaluator.evaluate(((ExpressionOutput) element).getExpression(), env);
            out.append(evaluator.toDisplayString(value));

        } else if (element instanceof JinjaIfStmt) {
            renderIf((JinjaIfStmt) element, env, out);

        } else if (element instanceof JinjaForStmt) {
            renderFor((JinjaForStmt) element, env, out);
        }
    }

    private void renderIf(JinjaIfStmt node, Map<String, Object> env, StringBuilder out) {

        if (evaluator.truthy(evaluator.evaluate(node.getCondition(), env))) {
            renderElements(node.getThenBody(), env, out);
            return;
        }

        for (JinjaElifClause elif : node.getElifClauses()) {
            if (evaluator.truthy(evaluator.evaluate(elif.getCondition(), env))) {
                renderElements(elif.getBody(), env, out);
                return;
            }
        }

        if (node.getElseClause() != null) {
            renderElements(node.getElseClause().getBody(), env, out);
        }
    }

    private void renderFor(JinjaForStmt node, Map<String, Object> env, StringBuilder out) {

        Object iterableValue = evaluator.evaluate(node.getIterable(), env);
        List<Object> items = toIterable(iterableValue);
        String varName = node.getVariable().getName();

        for (Object item : items) {
            Map<String, Object> loopEnv = new HashMap<>(env);
            loopEnv.put(varName, item);
            renderElements(node.getBody(), loopEnv, out);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Object> toIterable(Object value) {

        if (value instanceof List) {
            return (List<Object>) value;
        }

        if (value instanceof Map) {
            // Python dict iteration yields keys.
            return new ArrayList<>(((Map<Object, Object>) value).keySet());
        }

        if (value instanceof String) {
            List<Object> chars = new ArrayList<>();
            for (char c : ((String) value).toCharArray()) {
                chars.add(String.valueOf(c));
            }
            return chars;
        }

        return List.of(); // non-iterable - NON_ITERABLE_IN_FOR should already have flagged this
    }
}
