package compiler.semantic.common;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the symbol table with scope hierarchy.
 */
public class SymbolTable {
    
    private Scope currentScope;
    private final List<Scope> allScopes;
    
    public SymbolTable() {
        this.currentScope = new Scope(Scope.ScopeKind.GLOBAL, null);
        this.allScopes = new ArrayList<>();
        this.allScopes.add(currentScope);
    }
    
    public Scope getCurrentScope() {
        return currentScope;
    }
    
    public void enterScope(Scope.ScopeKind kind) {
        Scope newScope = new Scope(kind, currentScope);
        currentScope = newScope;
        allScopes.add(newScope);
    }
    
    public void exitScope() {
        if (currentScope.getParent() != null) {
            currentScope = currentScope.getParent();
        }
    }
    
    public boolean addSymbol(Symbol symbol) {
        return currentScope.addSymbol(symbol);
    }
    
    public Symbol lookup(String name) {
        return currentScope.lookup(name);
    }
    
    public List<Scope> getAllScopes() {
        return new ArrayList<>(allScopes);
    }
    
    public void print() {
        prettyPrint("");
    }

    public void prettyPrint() {
        prettyPrint("");
    }
    public void prettyPrint(String indent) {
        for (Scope scope : allScopes) {
            System.out.println(indent + "Scope (" + scope.getKind() + ") [depth " + scope.getDepth() + "]");
            for (Symbol symbol : scope.getSymbols().values()) {
                System.out.println(indent + "  " + symbol);
            }
            System.out.println();
        }
    }
}
