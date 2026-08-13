package test;

import compiler.ast.python.Program;
import compiler.generated.python.BasePythonLexer;
import compiler.generated.python.PythonParser;
import compiler.generator.Generator;
import compiler.semantic.common.SymbolTable;
import compiler.semantic.python.PythonSemanticAnalyzer;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Tests for Generator branch handling and multiple render_template calls.
 * Can be run standalone or polymorphically via the CompilerTest interface.
 */
public class GeneratorTest implements CompilerTest {

    @Override
    public void test(String filePath) throws IOException {
        System.out.println("=== Generator Test: " + filePath + " ===\n");

        String source = Files.readString(Path.of(filePath));
        List<Generator.RenderCall> calls = generate(source);

        System.out.println("Found " + calls.size() + " render_template call(s).\n");
        for (int i = 0; i < calls.size(); i++) {
            Generator.RenderCall call = calls.get(i);
            System.out.println("Call #" + (i + 1));
            System.out.println("  Template : " + call.getTemplateName());
            System.out.println("  Line     : " + call.getLine());
            System.out.println("  Context  : " + call.getContextValues());
            System.out.println();
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Generator Tests ===\n");

        testMultipleRenderCalls();
        testBranchIsolation();
        testMultipleCallsInBranches();
        testFunctionIsolation();

        System.out.println("\n=== All Generator Tests Complete ===");
    }

    private static void testMultipleRenderCalls() {
        System.out.println("Test 1: Multiple render_template Calls");
        String source =
                "title = \"A\"\n" +
                        "render_template(\"a.jinja\", title=title)\n" +
                        "title = \"B\"\n" +
                        "render_template(\"b.jinja\", title=title)";

        List<Generator.RenderCall> calls = generate(source);

        if (calls.size() == 2) {
            System.out.println("  PASS: Found 2 render calls");

            Object title1 = calls.get(0).getContextValues().get("title");
            if ("A".equals(title1)) {
                System.out.println("  PASS: First call has title=A");
            } else {
                System.out.println("  FAIL: First call title is " + title1 + ", expected A");
            }

            Object title2 = calls.get(1).getContextValues().get("title");
            if ("B".equals(title2)) {
                System.out.println("  PASS: Second call has title=B");
            } else {
                System.out.println("  FAIL: Second call title is " + title2 + ", expected B");
            }
        } else {
            System.out.println("  FAIL: Expected 2 render calls, found " + calls.size());
        }
        System.out.println();
    }

    private static void testBranchIsolation() {
        System.out.println("Test 2: Branch Environment Isolation");
        String source =
                "if condition:\n" +
                        "    products = [{\"name\": \"Phone\"}]\n" +
                        "else:\n" +
                        "    title = \"x\"\n" +
                        "render_template(\"x.jinja\", products=products)";

        List<Generator.RenderCall> calls = generate(source);

        if (calls.size() == 1) {
            System.out.println("  PASS: Found 1 render call");

            Map<String, Object> context = calls.get(0).getContextValues();
            if (!context.containsKey("title")) {
                System.out.println("  PASS: Branch isolation - title not leaked from else branch");
            } else {
                System.out.println("  FAIL: title leaked from else branch: " + context.get("title"));
            }
        } else {
            System.out.println("  FAIL: Expected 1 render call, found " + calls.size());
        }
        System.out.println();
    }

    private static void testMultipleCallsInBranches() {
        System.out.println("Test 3: Multiple Calls in Different Branches");
        String source =
                "if condition:\n" +
                        "    render_template(\"a.jinja\", title=\"A\")\n" +
                        "else:\n" +
                        "    render_template(\"b.jinja\", title=\"B\")";

        List<Generator.RenderCall> calls = generate(source);

        if (calls.size() == 2) {
            System.out.println("  PASS: Found 2 render calls (one per branch)");

            String template1 = calls.get(0).getTemplateName();
            String template2 = calls.get(1).getTemplateName();

            if (("a.jinja".equals(template1) || "b.jinja".equals(template1)) &&
                    ("a.jinja".equals(template2) || "b.jinja".equals(template2)) &&
                    !template1.equals(template2)) {
                System.out.println("  PASS: Both branches preserved their render calls");
            } else {
                System.out.println("  FAIL: Templates: " + template1 + ", " + template2);
            }
        } else {
            System.out.println("  FAIL: Expected 2 render calls, found " + calls.size());
        }
        System.out.println();
    }

    private static void testFunctionIsolation() {
        System.out.println("Test 4: Function Environment Isolation");
        String source =
                "def one():\n" +
                        "    title = \"A\"\n" +
                        "    render_template(\"a.jinja\", title=title)\n" +
                        "def two():\n" +
                        "    product = {\"name\": \"Phone\"}\n" +
                        "    render_template(\"b.jinja\", product=product)";

        List<Generator.RenderCall> calls = generate(source);

        if (calls.size() == 2) {
            System.out.println("  PASS: Found 2 render calls");

            Map<String, Object> context1 = calls.get(0).getContextValues();
            Map<String, Object> context2 = calls.get(1).getContextValues();

            if (context1.containsKey("title") && !context1.containsKey("product")) {
                System.out.println("  PASS: First call has title, not product");
            } else {
                System.out.println("  FAIL: First call context: " + context1.keySet());
            }

            if (context2.containsKey("product") && !context2.containsKey("title")) {
                System.out.println("  PASS: Second call has product, not title");
            } else {
                System.out.println("  FAIL: Second call context: " + context2.keySet());
            }
        } else {
            System.out.println("  FAIL: Expected 2 render calls, found " + calls.size());
        }
        System.out.println();
    }

    private static List<Generator.RenderCall> generate(String source) {
        BasePythonLexer lexer = new BasePythonLexer(CharStreams.fromString(source));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        PythonParser parser = new PythonParser(tokens);
        ParseTree tree = parser.program();

        Program program = (Program) new compiler.visitor.python.AstBuilder().visit(tree);
        PythonSemanticAnalyzer analyzer = new PythonSemanticAnalyzer();
        analyzer.analyze(program);
        SymbolTable symbols = analyzer.getSymbolTable();

        Generator generator = new Generator();
        return generator.generate(program, symbols);
    }
}