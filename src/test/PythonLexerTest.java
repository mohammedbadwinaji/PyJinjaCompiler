package test;

import compiler.generated.python.BasePythonLexer;
import compiler.generated.python.PythonLexer;
import compiler.printer.TokenPrinter;
import org.antlr.v4.runtime.*;

import java.io.IOException;

public class PythonLexerTest implements CompilerTest {

    @Override
    public  void test(String file) throws IOException {
        CharStream input = CharStreams.fromFileName(file);
        BasePythonLexer lexer = new BasePythonLexer(input);

        TokenPrinter.showGuiList(lexer);
    }
}
