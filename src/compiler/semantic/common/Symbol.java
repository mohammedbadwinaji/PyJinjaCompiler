package compiler.semantic.common;

import java.util.Map;

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
    
    // For LIST types: the element type
    private Type elementType;
    
    // For LIST types with DICTIONARY elements: field types of the dictionary elements
    private Map<String, Type> elementFieldTypes;
    
    // For DICTIONARY types: known field types (key -> value type)
    private Map<String, Type> fieldTypes;
    
    public Symbol(String name, Kind kind, int declaredLine) {
        this.name = name;
        this.kind = kind;
        this.declaredLine = declaredLine;
        this.inferredType = Type.UNKNOWN;
        this.elementType = Type.UNKNOWN;
        this.elementFieldTypes = null;
        this.fieldTypes = null;
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
    
    public Type getElementType() {
        return elementType;
    }
    
    public void setElementType(Type elementType) {
        this.elementType = elementType;
    }
    
    public Map<String, Type> getFieldTypes() {
        return fieldTypes;
    }
    
    public void setFieldTypes(Map<String, Type> fieldTypes) {
        this.fieldTypes = fieldTypes;
    }
    
    public Map<String, Type> getElementFieldTypes() {
        return elementFieldTypes;
    }
    
    public void setElementFieldTypes(Map<String, Type> elementFieldTypes) {
        this.elementFieldTypes = elementFieldTypes;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s (%s) [line %d] : %s", 
            name, kind, declaredLine, inferredType));
        
        if (inferredType == Type.LIST && elementType != Type.UNKNOWN) {
            sb.append(" <").append(elementType).append(">");
            if (elementType == Type.DICTIONARY && elementFieldTypes != null && !elementFieldTypes.isEmpty()) {
                sb.append(" {");
                boolean first = true;
                for (Map.Entry<String, Type> entry : elementFieldTypes.entrySet()) {
                    if (!first) sb.append(", ");
                    sb.append(entry.getKey()).append(": ").append(entry.getValue());
                    first = false;
                }
                sb.append("}");
            }
        }
        
        if (inferredType == Type.DICTIONARY && fieldTypes != null && !fieldTypes.isEmpty()) {
            sb.append(" {");
            boolean first = true;
            for (Map.Entry<String, Type> entry : fieldTypes.entrySet()) {
                if (!first) sb.append(", ");
                sb.append(entry.getKey()).append(": ").append(entry.getValue());
                first = false;
            }
            sb.append("}");
        }
        
        return sb.toString();
    }
}
