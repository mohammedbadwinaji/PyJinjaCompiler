package compiler.semantic.common;

/**
 * Represents a symbol in the symbol table.
 */
public class Symbol {
    
    public enum Kind {
        VARIABLE,
        FUNCTION,
        PARAMETER
    }
    
    private final String name;
    private final Kind kind;
    private final int declaredLine;
    private Type inferredType;
    
    public Symbol(String name, Kind kind, int declaredLine) {
        this.name = name;
        this.kind = kind;
        this.declaredLine = declaredLine;
        this.inferredType = Type.UNKNOWN;
    }
    
    public String getName() {
        return name;
    }
    
    public Kind getKind() {
        return kind;
    }
    
    public int getDeclaredLine() {
        return declaredLine;
    }
    
    public Type getInferredType() {
        return inferredType;
    }
    
    public void setInferredType(Type inferredType) {
        this.inferredType = inferredType;
    }
    
    @Override
    public String toString() {
        return String.format("%s (%s) [line %d] : %s", 
            name, kind, declaredLine, inferredType);
    }
}
