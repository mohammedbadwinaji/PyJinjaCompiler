package test;

import compiler.frontend.jinja.AstBuilder;
import compiler.ast.jinja.Template;
import compiler.generated.jinja.JinjaLexer;
import compiler.generated.jinja.JinjaParser;
import org.antlr.v4.runtime.*;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Simple non-JUnit test harness that parses a Jinja file, builds the AST using
 * AstBuilder and prints the parse tree and pretty-printed AST to stdout.
 *
 * Usage:
 *   test.JinjaAstBuilderTest.Test("input/jinja/example.jinja");
 */
public final class JinjaAstBuilderTest implements CompilerTest {

    @Override
    public void test(String file) throws Exception {
        Path path = Paths.get(file);
        CharStream input = CharStreams.fromPath(path);

        // Lexer / parser
        JinjaLexer lexer = new JinjaLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        JinjaParser parser = new JinjaParser(tokens);

        // Optional: attach a simple error listener to surface syntax errors
        lexer.removeErrorListeners();
        parser.removeErrorListeners();
        VerboseErrorListener err = new VerboseErrorListener();
        lexer.addErrorListener(err);
        parser.addErrorListener(err);

        // Parse
        JinjaParser.TemplateContext templateCtx = parser.template();

        // Print parse tree (text form)
        System.out.println("=== Parse tree (toStringTree) ===");
        System.out.println(templateCtx.toStringTree(parser));
        System.out.println("=== End parse tree ===\n");

        // Build AST
        AstBuilder builder = new AstBuilder();
        Template template = builder.build(templateCtx);

        // Print pretty-printed AST
        System.out.println("=== AST prettyPrint ===");
        System.out.println(template.prettyPrint(""));
        System.out.println("=== End AST prettyPrint ===");
    }

    // Simple verbose error listener to surface syntax errors
    private static final class VerboseErrorListener extends BaseErrorListener {
        @Override
        public void syntaxError(Recognizer<?, ?> recognizer,
                                Object offendingSymbol,
                                int line, int charPositionInLine,
                                String msg, RecognitionException e) {
            System.err.printf("SYNTAX ERROR at %d:%d - %s%n", line, charPositionInLine, msg);
        }
    }
}
