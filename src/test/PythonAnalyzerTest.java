package test;

import compiler.ast.python.Program;
import compiler.frontend.python.AstBuilder;
import compiler.generated.python.BasePythonLexer;
import compiler.generated.python.PythonParser;
import compiler.semantic.python.PythonSemanticAnalyzer;
import compiler.semantic.common.SemanticError;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class PythonAnalyzerTest implements CompilerTest {

    public PythonAnalyzerTest() {
    }

    @Override
    public void test(String fileName) throws IOException {

        String source = Files.readString(Path.of(fileName));

        CharStream input = CharStreams.fromString(source);

        BasePythonLexer lexer = new BasePythonLexer(input);

        CommonTokenStream tokens = new CommonTokenStream(lexer);

        PythonParser parser = new PythonParser(tokens);

        ParseTree tree = parser.program();

        AstBuilder builder = new AstBuilder();

        Program program = (Program) builder.visit(tree);



//        System.out.println("======================================");
//        System.out.println("AST");
//        System.out.println("======================================");
//        System.out.println(program.prettyPrint(""));

        PythonSemanticAnalyzer analyzer = new PythonSemanticAnalyzer();

        List<SemanticError> errors = analyzer.analyze(program);

        System.out.println();
        System.out.println("======================================");
        System.out.println("Semantic Analysis");
        System.out.println("======================================");

        if (errors.isEmpty()) {

            System.out.println("No semantic errors.");

        } else {

            for (SemanticError error : errors) {
                System.out.println(error);
            }

            System.out.println();
            System.out.println(errors.size() + " semantic error(s).");
        }

        System.out.println();
        System.out.println("======================================");
        System.out.println("Symbol Table");
        System.out.println("======================================");

        analyzer.getSymbolTable().prettyPrint();
    }
}