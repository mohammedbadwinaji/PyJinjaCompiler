package compiler.webapp;

import compiler.ast.jinja.Template;
import compiler.visitor.jinja.AstBuilder;
import compiler.generated.jinja.JinjaLexer;
import compiler.generated.jinja.JinjaParser;
import compiler.generator.Renderer;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads a .html template from disk, runs it through the project's own
 * JinjaLexer -> JinjaParser -> compiler.frontend.jinja.AstBuilder chain
 * to get a Template AST (parsed once, then cached), and renders it with
 * Renderer. This is the same pipeline PIPELINE_USAGE.md walks through for
 * the Python-driven case - here the context comes straight from
 * ProductStore instead of from Generator, since a live web request isn't
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
}
