package test;

import compiler.generated.jinja.*;
import compiler.printer.ParseTreePrinter;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class JinjaParserTest implements CompilerTest {
    @Override
    public  void test(String filePath) throws IOException {
        CharStream input = CharStreams.fromFileName(filePath);

        JinjaLexer lexer = new JinjaLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        JinjaParser parser = new JinjaParser(tokens);


        ParseTree tree = parser.template();

        ParseTreePrinter.printText(tree,parser);
    }

    private static void printTree(ParseTree tree, int level) {
        if (tree == null) return;

        String indent = "  ".repeat(level);
        System.out.println(indent + tree.getClass().getSimpleName() + ": " + tree.getText());

        for (int i = 0; i < tree.getChildCount(); i++) {
            printTree(tree.getChild(i), level + 1);
        }
    }
}
