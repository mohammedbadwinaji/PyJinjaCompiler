package test;

import compiler.ast.jinja.Template;
import compiler.ast.python.Program;
import compiler.frontend.python.AstBuilder;
import compiler.generated.jinja.JinjaLexer;
import compiler.generated.jinja.JinjaParser;
import compiler.generated.python.BasePythonLexer;
import compiler.generated.python.PythonLexer;
import compiler.generated.python.PythonParser;
import compiler.semantic.common.SemanticError;
import compiler.semantic.common.SymbolTable;
import compiler.semantic.jinja.JinjaSemanticAnalyzer;
import compiler.semantic.python.PythonSemanticAnalyzer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

public class JinjaSemanticAnalyzerTest {
    public void test(String pythonFile, String jinjaFile) throws IOException {
        String pythonSource = Files.readString(Path.of(pythonFile));

        CharStream pythonInput = CharStreams.fromString(pythonSource);

        BasePythonLexer pythonLexer = new BasePythonLexer(pythonInput);

        CommonTokenStream pythonTokens = new CommonTokenStream(pythonLexer);

        PythonParser pythonParser = new PythonParser(pythonTokens);

        ParseTree pythonParseTree = pythonParser.program();

        compiler.frontend.python.AstBuilder pythonAstBuilder = new compiler.frontend.python.AstBuilder();

        Program program = (Program) pythonAstBuilder.visit(pythonParseTree);

        PythonSemanticAnalyzer pythonAnalyzer = new PythonSemanticAnalyzer();
        pythonAnalyzer.analyze(program);
        SymbolTable symbolTable = pythonAnalyzer.getSymbolTable();


        String jinjaSource = Files.readString(Path.of(jinjaFile));

        CharStream jinjaInput = CharStreams.fromString(jinjaSource);

        JinjaLexer jinjaLexer = new JinjaLexer(jinjaInput);

        CommonTokenStream jinjaTokens = new CommonTokenStream(jinjaLexer);

        JinjaParser jinjaParser = new JinjaParser(jinjaTokens);

        ParseTree jinjaParseTree = jinjaParser.template();

        compiler.frontend.jinja.AstBuilder jinjaAstBuilder = new compiler.frontend.jinja.AstBuilder();

        Template template = (Template) jinjaAstBuilder.visit(jinjaParseTree);

        JinjaSemanticAnalyzer analyzer =
                new JinjaSemanticAnalyzer(symbolTable);

        List<SemanticError> errors =
                analyzer.analyze(template);
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
