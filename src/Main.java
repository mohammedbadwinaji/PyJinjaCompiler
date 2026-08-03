import test.PythonLexerTest;
import test.PythonParserTest;
public class Main {
    public static void main(String[] args) throws Exception {
        PythonLexerTest.Test("input/python/test01.py");
        PythonParserTest.Test("input/python/test01.py");
    }
}
