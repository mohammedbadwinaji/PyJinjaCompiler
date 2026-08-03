package test;

import compiler.generated.python.BasePythonLexer;
import compiler.generated.python.PythonParser;
import compiler.printer.TreeVisualizer;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;

public class PythonParserTest {

    public static void Test(String file) throws Exception {
        CharStream input = CharStreams.fromFileName(file);

        BasePythonLexer lexer = new BasePythonLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        PythonParser parser = new PythonParser(tokens);

        ParseTree tree = parser.program();
        TreeVisualizer.printTextTree(tree,parser);
//        System.out.println(tree.toStringTree(parser));
    }
}
