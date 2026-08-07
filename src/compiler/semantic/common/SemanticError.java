package compiler.semantic.common;

/**
 * Represents a semantic error found during analysis.
 */
public class SemanticError {
    
    public enum ErrorType {
        DUPLICATE_FUNCTION_DEFINITION,
        UNDEFINED_VARIABLE,
        UNDEFINED_FUNCTION,
        NOT_CALLABLE,
        DUPLICATE_PARAMETER,
        WRONG_ARGUMENT_COUNT,
        NON_ITERABLE_IN_FOR,
        TYPE_MISMATCH
    }
    
    private final int line;
    private final ErrorType errorType;
    private final String message;
    
    public SemanticError(int line, ErrorType errorType, String message) {
        this.line = line;
        this.errorType = errorType;
        this.message = message;
    }
    
    public int getLine() {
        return line;
    }
    
    public ErrorType getErrorType() {
        return errorType;
    }
    
    public String getMessage() {
        return message;
    }
    
    @Override
    public String toString() {
        return String.format("Line %d: %s", line, message);
    }
}
