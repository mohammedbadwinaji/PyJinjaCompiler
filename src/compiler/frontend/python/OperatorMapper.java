package compiler.frontend.python;

import compiler.ast.python.BinaryOperator;
import compiler.generated.python.BasePythonLexer;
import org.antlr.v4.runtime.Token;

public final class OperatorMapper {

    private OperatorMapper() {
    }

    public static BinaryOperator map(Token token) {

        return switch (token.getType()) {

            case BasePythonLexer.OR -> BinaryOperator.OR;
            case BasePythonLexer.AND -> BinaryOperator.AND;

            case BasePythonLexer.EQ -> BinaryOperator.EQ;
            case BasePythonLexer.NE -> BinaryOperator.NE;
            case BasePythonLexer.LT -> BinaryOperator.LT;
            case BasePythonLexer.LE -> BinaryOperator.LE;
            case BasePythonLexer.GT -> BinaryOperator.GT;
            case BasePythonLexer.GE -> BinaryOperator.GE;

            case BasePythonLexer.PLUS -> BinaryOperator.ADD;
            case BasePythonLexer.MINUS -> BinaryOperator.SUBTRACT;

            case BasePythonLexer.STAR -> BinaryOperator.MULTIPLY;
            case BasePythonLexer.SLASH -> BinaryOperator.DIVIDE;
            case BasePythonLexer.MOD -> BinaryOperator.MODULO;

            default ->
                    throw new IllegalArgumentException(
                            "Unsupported operator: " + token.getText());
        };
    }

}