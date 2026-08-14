import test.CompilerPipelineTest;

public class Main {
    public static void main(String[] args) throws Exception {
//        CompilerPipelineTest test =new CompilerPipelineTest();
//        test.test("test-data/python/semanticErrors.py","test-data/jinja");

        Compiler.run("test-data/python/semanticErrors.py","test-data/jinja");
    }
}