import test.AstBuilderTest;
import test.PythonAnalyzerTest;
import test.PythonLexerTest;
import test.PythonParserTest;
public class Main {
    public static void main(String[] args) throws Exception {

        PythonAnalyzerTest.analyze("input/python/duplicateFunction.py");
        PythonAnalyzerTest.analyze("input/python/undefinedVariable.py");
        PythonAnalyzerTest.analyze("input/python/duplicateParameter.py");
        PythonAnalyzerTest.analyze("input/python/wrongArguments.py");
        PythonAnalyzerTest.analyze("input/python/nonIterable.py");
        PythonAnalyzerTest.analyze("input/python/typeMismatch.py");
        PythonAnalyzerTest.analyze("input/python/allErrors.py");
        PythonAnalyzerTest.analyze("input/python/correctProgram.py");
        PythonAnalyzerTest.analyze("input/python/undefinedFunction.py");
        PythonAnalyzerTest.analyze("input/python/callVariable.py");
        PythonAnalyzerTest.analyze("input/python/userFunction.py");
    }
}