public class Main {
    public static void main(String[] args) throws Exception {
//        CompilerPipelineTest test =new CompilerPipelineTest();
//        test.test("test-data/python/test01.py","test-data/jinja");

        Compiler.run("test-data/python/compiler_full_test.py","test-data/jinja");
    }
}