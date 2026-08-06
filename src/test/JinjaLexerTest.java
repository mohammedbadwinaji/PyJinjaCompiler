package test;

import compiler.generated.jinja.JinjaLexer;
import compiler.printer.TokenPrinter;
import org.antlr.v4.runtime.*;

import java.io.IOException;

public class JinjaLexerTest implements CompilerTest{

    @Override
    public void test(String file) throws IOException {
        CharStream input = CharStreams.fromFileName(file);
        JinjaLexer lexer = new JinjaLexer(input);

        TokenPrinter.printText(lexer);
    }
}
