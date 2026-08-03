package test;

import compiler.generated.python.BasePythonLexer;
import compiler.generated.python.PythonLexer;
import org.antlr.v4.runtime.*;

import java.io.IOException;

public class PythonLexerTest {

    public static void Test(String file) throws IOException {
        CharStream input = CharStreams.fromFileName(file);
        BasePythonLexer lexer = new BasePythonLexer(input);

        System.out.println("==================================================");
        System.out.printf("%-25s %-25s%n", "TOKEN", "TEXT");
        System.out.println("==================================================");

        while (true) {
            Token token = lexer.nextToken();
            String tokenName = PythonLexer.VOCABULARY.getSymbolicName(token.getType());

            if (tokenName == null) {
                tokenName = "EOF";
            }

            String text = token.getText()
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");

            System.out.printf("%-25s %-25s%n", tokenName, "\"" + text + "\"");

            if (token.getType() == Token.EOF) {
                break;
            }
        }

        System.out.println("==================================================");
    }
}
