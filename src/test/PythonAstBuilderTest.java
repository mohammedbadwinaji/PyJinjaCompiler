package test;

import compiler.visitor.python.AstBuilder;
import compiler.ast.python.Program;
import compiler.generated.python.BasePythonLexer;
import compiler.generated.python.PythonParser;
import org.antlr.v4.runtime.*;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Simple non-JUnit test harness that parses a Python file, builds the AST using
 * AstBuilder and prints the parse tree and pretty-printed AST to stdout.
 *
 * Usage:
 *   test.AstBuilderTest.Test("input/python/exercise_supported_syntax_fixed.py");
 */
public final class PythonAstBuilderTest implements CompilerTest {

    @Override
    public void test(String file) throws Exception {
        Path path = Paths.get(file);
        CharStream input = CharStreams.fromPath(path);

        // Lexer / parser
        BasePythonLexer lexer = new BasePythonLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        PythonParser parser = new PythonParser(tokens);

        // Optional: attach a simple error listener to surface syntax errors
        lexer.removeErrorListeners();
        parser.removeErrorListeners();
        VerboseErrorListener err = new VerboseErrorListener();
        lexer.addErrorListener(err);
        parser.addErrorListener(err);

        // Parse
        PythonParser.ProgramContext programCtx = parser.program();

        // Print parse tree (text form)
        System.out.println("=== Parse tree (toStringTree) ===");
        System.out.println(programCtx.toStringTree(parser));
        System.out.println("=== End parse tree ===\n");

        // Build AST
        AstBuilder builder = new AstBuilder();
        Program program = builder.build(programCtx);

        // Print pretty-printed AST
        System.out.println("=== AST prettyPrint ===");
        System.out.println(program.prettyPrint(""));
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
