package compiler.generator;

import compiler.ast.common.*;
import compiler.ast.python.DictEntry;
import compiler.ast.python.DictExpr;
import compiler.ast.python.ListExpr;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Evaluates Expression nodes against a runtime environment
 * (Map<String, Object>), producing plain Java values: Long, Double,
 * String, Boolean, null, List<Object>, or Map<String, Object>.
 *
 * Since compiler.ast.common's expression nodes (Identifier, literals,
 * BinaryExpr, UnaryExpr, AttributeAccess, IndexAccess, CallExpr) are
 * shared by both the Python tree and the Jinja tree, this one evaluator
 * covers both:
 *   - Generator uses it to turn `products = [{"name": ..., ...}, ...]`
 *     into a real Java List/Map.
 *   - Renderer uses it to evaluate `{{ p.price }}` / `{% if p.price > 350 %}`
 *     against that data while walking the Jinja Template.
 *
 * Deliberately restricted: no user-defined function calls, no
 * assignment, no side effects - just the data-literal + arithmetic +
 * comparison subset both grammars actually support.
 */
public final class ExpressionEvaluator {

    public Object evaluate(Expression expr, Map<String, Object> env) {

        if (expr instanceof IntegerLiteral) {
            return ((IntegerLiteral) expr).getValue();

        } else if (expr instanceof FloatLiteral) {
            return ((FloatLiteral) expr).getValue();

        } else if (expr instanceof StringLiteral) {
            return ((StringLiteral) expr).getValue();

        } else if (expr instanceof BooleanLiteral) {
            return ((BooleanLiteral) expr).getValue();

        } else if (expr instanceof NoneLiteral) {
            return null;

        } else if (expr instanceof Identifier) {
            return env.get(((Identifier) expr).getName());

        } else if (expr instanceof ListExpr) {
            List<Object> list = new ArrayList<>();
            for (Expression element : ((ListExpr) expr).getElements()) {
                list.add(evaluate(element, env));
            }
            return list;

        } else if (expr instanceof DictExpr) {
            Map<String, Object> map = new LinkedHashMap<>();
            for (DictEntry entry : ((DictExpr) expr).getEntries()) {
                map.put(keyOf(entry.getKey()), evaluate(entry.getValue(), env));
            }
            return map;

        } else if (expr instanceof UnaryExpr) {
            return evaluateUnary((UnaryExpr) expr, env);

        } else if (expr instanceof BinaryExpr) {
            return evaluateBinary((BinaryExpr) expr, env);

        } else if (expr instanceof AttributeAccess) {
            AttributeAccess access = (AttributeAccess) expr;
            Object target = evaluate(access.getTarget(), env);
            if (target instanceof Map) {
                return ((Map<?, ?>) target).get(access.getAttribute());
            }
            return null;

        } else if (expr instanceof IndexAccess) {
            IndexAccess access = (IndexAccess) expr;
            Object target = evaluate(access.getTarget(), env);
            Object index = evaluate(access.getIndex(), env);
            return evaluateIndex(target, index);

        } else if (expr instanceof CallExpr) {
            return evaluateCall((CallExpr) expr, env);
        }

        return null;
    }

    private String keyOf(Expression key) {
        if (key instanceof StringLiteral) {
            return ((StringLiteral) key).getValue();
        }
        if (key instanceof Identifier) {
            return ((Identifier) key).getName();
        }
        return String.valueOf(key);
    }

    private Object evaluateIndex(Object target, Object index) {

        if (target instanceof List && index instanceof Number) {
            List<?> list = (List<?>) target;
            int i = ((Number) index).intValue();
            return (i >= 0 && i < list.size()) ? list.get(i) : null;
        }

        if (target instanceof Map) {
            return ((Map<?, ?>) target).get(String.valueOf(index));
        }

        if (target instanceof String && index instanceof Number) {
            String s = (String) target;
            int i = ((Number) index).intValue();
            return (i >= 0 && i < s.length()) ? String.valueOf(s.charAt(i)) : null;
        }

        return null;
    }

    private Object evaluateUnary(UnaryExpr expr, Map<String, Object> env) {

        Object value = evaluate(expr.getExpr(), env);

        switch (expr.getOperator()) {
            case NOT:
                return !truthy(value);
            case MINUS:
                if (value instanceof Long) return -((Long) value);
                if (value instanceof Double) return -((Double) value);
                return null;
            case PLUS:
                return value;
            default:
                return null;
        }
    }

    private Object evaluateBinary(BinaryExpr expr, Map<String, Object> env) {

        BinaryOperator op = expr.getOperator();

        // AND / OR short-circuit like Python: return the operand value,
        // not a coerced boolean.
        if (op == BinaryOperator.AND) {
            Object left = evaluate(expr.getLeft(), env);
            return truthy(left) ? evaluate(expr.getRight(), env) : left;
        }
        if (op == BinaryOperator.OR) {
            Object left = evaluate(expr.getLeft(), env);
            return truthy(left) ? left : evaluate(expr.getRight(), env);
        }

        Object left = evaluate(expr.getLeft(), env);
        Object right = evaluate(expr.getRight(), env);

        switch (op) {
            case EQ: return equalsValue(left, right);
            case NE: return !equalsValue(left, right);
            case LT: return compare(left, right) < 0;
            case LE: return compare(left, right) <= 0;
            case GT: return compare(left, right) > 0;
            case GE: return compare(left, right) >= 0;
            default: break;
        }

        if (op == BinaryOperator.ADD && left instanceof String && right instanceof String) {
            return (String) left + (String) right;
        }

        if (isNumeric(left) && isNumeric(right)) {
            boolean useDouble = (left instanceof Double) || (right instanceof Double);
            double l = ((Number) left).doubleValue();
            double r = ((Number) right).doubleValue();

            Double result;
            switch (op) {
                case ADD: result = l + r; break;
                case SUBTRACT: result = l - r; break;
                case MULTIPLY: result = l * r; break;
                case DIVIDE: result = r == 0 ? null : l / r; break;
                case MODULO: result = r == 0 ? null : l % r; break;
                default: return null;
            }

            if (result == null) return null;
            return useDouble ? result : (Object) result.longValue();
        }

        return null;
    }

    private Object evaluateCall(CallExpr call, Map<String, Object> env) {

        if (!(call.getCallee() instanceof Identifier)) {
            return null;
        }

        String name = ((Identifier) call.getCallee()).getName();

        List<Object> args = new ArrayList<>();
        for (Argument arg : call.getArguments()) {
            if (arg instanceof PositionalArgument) {
                args.add(evaluate(((PositionalArgument) arg).getValue(), env));
            }
        }

        switch (name) {
            case "len":
                if (args.isEmpty()) return null;
                Object a0 = args.get(0);
                if (a0 instanceof List) return (long) ((List<?>) a0).size();
                if (a0 instanceof Map) return (long) ((Map<?, ?>) a0).size();
                if (a0 instanceof String) return (long) ((String) a0).length();
                return null;

            case "str":
                return args.isEmpty() ? "" : toDisplayString(args.get(0));

            case "int":
                if (args.isEmpty()) return 0L;
                if (args.get(0) instanceof Number) return ((Number) args.get(0)).longValue();
                try { return Long.parseLong(String.valueOf(args.get(0))); } catch (NumberFormatException e) { return null; }

            case "float":
                if (args.isEmpty()) return 0.0;
                if (args.get(0) instanceof Number) return ((Number) args.get(0)).doubleValue();
                try { return Double.parseDouble(String.valueOf(args.get(0))); } catch (NumberFormatException e) { return null; }

            case "bool":
                return args.isEmpty() ? Boolean.FALSE : truthy(args.get(0));

            case "range":
                return evaluateRange(args);

            default:
                // No user-defined functions - not something this evaluator executes.
                return null;
        }
    }

    private List<Object> evaluateRange(List<Object> args) {
        long start = 0, stop, step = 1;
        if (args.size() == 1) {
            stop = ((Number) args.get(0)).longValue();
        } else if (args.size() >= 2) {
            start = ((Number) args.get(0)).longValue();
            stop = ((Number) args.get(1)).longValue();
            if (args.size() >= 3) step = ((Number) args.get(2)).longValue();
        } else {
            return new ArrayList<>();
        }
        List<Object> result = new ArrayList<>();
        if (step > 0) {
            for (long i = start; i < stop; i += step) result.add(i);
        } else if (step < 0) {
            for (long i = start; i > stop; i += step) result.add(i);
        }
        return result;
    }

    private boolean isNumeric(Object o) {
        return o instanceof Long || o instanceof Double;
    }

    private boolean equalsValue(Object left, Object right) {
        if (isNumeric(left) && isNumeric(right)) {
            return ((Number) left).doubleValue() == ((Number) right).doubleValue();
        }
        if (left == null) return right == null;
        return left.equals(right);
    }

    @SuppressWarnings("unchecked")
    private int compare(Object left, Object right) {
        if (isNumeric(left) && isNumeric(right)) {
            return Double.compare(((Number) left).doubleValue(), ((Number) right).doubleValue());
        }
        if (left instanceof String && right instanceof String) {
            return ((String) left).compareTo((String) right);
        }
        if (left instanceof Comparable && right != null && left.getClass().isInstance(right)) {
            return ((Comparable<Object>) left).compareTo(right);
        }
        return 0; // Not meaningfully comparable - treat as equal rather than throw.
    }

    public boolean truthy(Object o) {
        if (o == null) return false;
        if (o instanceof Boolean) return (Boolean) o;
        if (o instanceof Number) return ((Number) o).doubleValue() != 0;
        if (o instanceof String) return !((String) o).isEmpty();
        if (o instanceof List) return !((List<?>) o).isEmpty();
        if (o instanceof Map) return !((Map<?, ?>) o).isEmpty();
        return true;
    }

    /** Python-flavored display string, since the data originated from Python literals. */
    public String toDisplayString(Object value) {
        if (value == null) return "None";
        if (value instanceof Boolean) return ((Boolean) value) ? "True" : "False";
        if (value instanceof Double) {
            double d = (Double) value;
            if (d == Math.floor(d) && !Double.isInfinite(d)) {
                return String.valueOf((long) d) + ".0";
            }
            return String.valueOf(d);
        }
        return String.valueOf(value);
    }
}
