import compiler.semantic.python.PythonSemanticAnalyzer;
import test.*;

import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
//        new GeneratorTest().test("test-data/python/compiler_full_test.py");
        CompilerPipelineTest test =new CompilerPipelineTest();
        test.test("test-data/python/test01.py","test-data/jinja");
    }
}