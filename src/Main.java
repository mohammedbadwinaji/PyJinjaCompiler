import test.CompilerPipelineTest;
import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        CompilerPipelineTest pipelineTest = new CompilerPipelineTest();

        String pythonFile = "test-data/python/compiler_full_test.py";
        String templatesDirectory = "test-data/jinja";

        List<String> allHtml = pipelineTest.test(pythonFile, templatesDirectory);

        // Display all generated HTML at the end
        System.out.println("\n" + "=".repeat(72));
        System.out.println("ALL GENERATED HTML");
        System.out.println("=".repeat(72));
        for (int i = 0; i < allHtml.size(); i++) {
            System.out.println("\n--- HTML #" + (i + 1) + " ---");
            System.out.println(allHtml.get(i));
        }
        System.out.println("\n" + "=".repeat(72));
        System.out.println("Total: " + allHtml.size() + " HTML document(s) generated");
        System.out.println("=".repeat(72));
    }
}