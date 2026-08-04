package compiler.semantic.python;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a lexical scope in the symbol table.
 */
public class Scope {
    
    public enum ScopeKind {
        GLOBAL,
        FUNCTION,
        BLOCK
    }
    
    private final ScopeKind kind;
    private final Scope parent;
    private final Map<String, Symbol> symbols;
    private final int depth;
    
    public Scope(ScopeKind kind, Scope parent) {
        this.kind = kind;
        this.parent = parent;
        this.symbols = new HashMap<>();
        this.depth = parent == null ? 0 : parent.depth + 1;
    }
    
    public ScopeKind getKind() {
        return kind;
    }
    
    public Scope getParent() {
        return parent;
    }
    
    public int getDepth() {
        return depth;
    }
    
    /**
     * Add a symbol to this scope.
     * Returns true if added, false if already exists in this scope (shadowing not allowed in same scope).
     */
    public boolean addSymbol(Symbol symbol) {
        if (symbols.containsKey(symbol.getName())) {
            return false;
        }
        symbols.put(symbol.getName(), symbol);
        return true;
    }
    
    /**
     * Look up a symbol in this scope only.
     */
    public Symbol lookupLocal(String name) {
        return symbols.get(name);
    }
    
    /**
     * Look up a symbol in this scope and all parent scopes.
     */
    public Symbol lookup(String name) {
        Symbol symbol = symbols.get(name);
        if (symbol != null) {
            return symbol;
        }
        if (parent != null) {
            return parent.lookup(name);
        }
        return null;
    }
    
    /**
     * Check if a symbol exists in any parent scope (for shadowing detection).
     */
    public boolean existsInParent(String name) {
        if (parent == null) {
            return false;
        }
        if (parent.lookupLocal(name) != null) {
            return true;
        }
        return parent.existsInParent(name);
    }
    
    public Map<String, Symbol> getSymbols() {
        return new HashMap<>(symbols);
    }
}
