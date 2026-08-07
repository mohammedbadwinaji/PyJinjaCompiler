import compiler.semantic.jinja.JinjaSemanticAnalyzer;
import test.*;

public class Main {
    public static void main(String[] args) throws Exception {
//        Tester.testFile("input/python/callVariable.py",new PythonLexerTest());
//        Tester.testFile("input/python/callVariable.py",new PythonParserTest());
//        Tester.testFolder("input/python","py",new PythonAnalyzerTest());
//        Tester.testFolder("input/python","py",new PythonAstBuilderTest());
//        Tester.testFolder("input/jinja",".html",new JinjaLexerTest());
//        Tester.testFolder("input/jinja",".html",new JinjaParserTest());
//        Tester.testFile("input/jinja/complex_template.html",new JinjaAstBuilderTest());

        JinjaSemanticAnalyzerTest test  = new JinjaSemanticAnalyzerTest();
        test.test("input/case01/python.py","input/case01/jinja.html");


    }
}