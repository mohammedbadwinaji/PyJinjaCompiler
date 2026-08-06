import test.*;

public class Main {
    public static void main(String[] args) throws Exception {

        System.out.println("jinja Lexer Testing");
        Tester.testFile("input/Jinja/attribute_access.html",new JinjaLexerTest());
        System.out.println("jinja Parser Testing");
        Tester.testFile("input/Jinja/attribute_access.html",new JinjaParserTest());
//        System.out.println("Python Lexer Testing");
//        Tester.testFile("input/python/allErrors.py",new PythonLexerTest());
//        System.out.println("Python Parser Testing");
//        Tester.testFile("input/python/allErrors.py",new PythonParserTest());
    }
}