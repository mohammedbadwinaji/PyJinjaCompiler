package compiler.webapp;

import compiler.ast.jinja.Template;
import compiler.visitor.jinja.AstBuilder;
import compiler.generated.jinja.JinjaLexer;
import compiler.generated.jinja.JinjaParser;
import compiler.generator.Renderer;
import compiler.semantic.common.SemanticError;
import compiler.semantic.common.Symbol;
import compiler.semantic.common.SymbolTable;
import compiler.semantic.common.Type;
import compiler.semantic.jinja.JinjaSemanticAnalyzer;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads a .html template from disk, runs it through the project's own
 * JinjaLexer -> JinjaParser -> compiler.frontend.jinja.AstBuilder chain
 * to get a Template AST (parsed once, then cached), runs semantic analysis,
 * and renders it with Renderer. This is the same pipeline PIPELINE_USAGE.md 
 * walks through for the Python-driven case - here the context comes straight 
 * from ProductStore instead of from Generator, since a live web request isn't
 * re-parsing a Python source file per click.
 */
public final class TemplateEngine {

    private final Path templatesDir;
    private final Map<String, Template> cache = new ConcurrentHashMap<>();
    private final Renderer renderer = new Renderer();

    public TemplateEngine(Path templatesDir) {
        this.templatesDir = templatesDir;
    }

    public String render(String templateName, Map<String, Object> context) {
        Template template = cache.computeIfAbsent(templateName, this::parse);
        
        // Run semantic analysis before rendering
        SymbolTable symbolTable = buildSymbolTable(context);
        JinjaSemanticAnalyzer analyzer = new JinjaSemanticAnalyzer(symbolTable);
        List<SemanticError> errors = analyzer.analyze(template);
        
        if (!errors.isEmpty()) {
            throw new RuntimeException("Semantic errors in template " + templateName + ": " + errors);
        }
        
        return renderer.render(template, context);
    }

    private Template parse(String templateName) {
        try {
            String source = Files.readString(templatesDir.resolve(templateName));
            CharStream input = CharStreams.fromString(source);
            JinjaLexer lexer = new JinjaLexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            JinjaParser parser = new JinjaParser(tokens);
            ParseTree tree = parser.template();
            return (Template) new AstBuilder().visit(tree);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load template: " + templateName, e);
        }
    }
    
    private SymbolTable buildSymbolTable(Map<String, Object> context) {
        SymbolTable symbolTable = new SymbolTable();
        for (String key : context.keySet()) {
            Symbol symbol = new Symbol(key, Symbol.Kind.VARIABLE, 0);
            symbol.setInferredType(determineType(context.get(key)));
            symbolTable.addSymbol(symbol);
        }
        return symbolTable;
    }
    
    private Type determineType(Object value) {
        if (value == null) return Type.UNKNOWN;
        if (value instanceof String) return Type.STRING;
        if (value instanceof Number) {
            if (value instanceof Integer || value instanceof Long) return Type.INTEGER;
            return Type.FLOAT;
        }
        if (value instanceof Boolean) return Type.BOOLEAN;
        if (value instanceof List) return Type.LIST;
        if (value instanceof Map) return Type.DICTIONARY;
        return Type.UNKNOWN;
    }
}
