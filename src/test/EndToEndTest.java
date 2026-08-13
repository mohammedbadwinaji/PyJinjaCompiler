package test;

import compiler.ast.jinja.Template;
import compiler.ast.python.Program;

import compiler.generated.python.BasePythonLexer;
import compiler.generated.python.PythonParser;
import compiler.generated.jinja.JinjaLexer;
import compiler.generated.jinja.JinjaParser;

import compiler.printer.ASTPrinter;
import compiler.printer.ParseTreePrinter;
import compiler.printer.TokenPrinter;

import compiler.semantic.common.SemanticError;
import compiler.semantic.common.SymbolTable;
import compiler.semantic.jinja.JinjaSemanticAnalyzer;
import compiler.semantic.python.PythonSemanticAnalyzer;

import compiler.generator.Generator;
import compiler.generator.Renderer;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.tree.ParseTree;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Full pipeline test: runs a Python source file and a Jinja template file
 * through every stage of the compiler, end to end -
 *
 *   Python lexer -> Python parser -> Python AST -> Python semantic
 *   analysis -> Generator -> Jinja lexer -> Jinja parser -> Jinja AST ->
 *   Jinja semantic analysis (checked against the Generator's context) ->
 *   Renderer -> final rendered HTML.
 *
 * Every stage's output is printed with its own labeled section, reusing
 * the project's existing printer utilities (TokenPrinter, ParseTreePrinter,
 * ASTPrinter) rather than duplicating that logic.
 *
 * ================================================================
 * HOW TO USE
 * ================================================================
 * The two file paths right below are the only thing you normally need
 * to touch - point them at whatever .py / .html (Jinja) pair you want
 * to test and re-run:
 *
 *     public static String PYTHON_FILE = "input/python/test01.py";
 *     public static String JINJA_FILE  = "input/jinja/index.html";
 *
 * Run it directly:
 *     java test.EndToEndTest
 *
 * Or override both paths from the command line without editing anything:
 *     java test.EndToEndTest input/python/test02.py input/jinja/other.html
 *
 * Or call it from your own Main.java / another test:
 *     test.EndToEndTest.run("input/python/test01.py", "input/jinja/index.html");
 */
public final class EndToEndTest {

    // ================================================================
    // EDIT THESE TWO PATHS TO TEST A DIFFERENT PYTHON / JINJA FILE PAIR
    // ================================================================
    public static String PYTHON_FILE = "test-data/python/compiler_full_test.py";
    public static String JINJA_FILE  = "test-data/jinja/hello.jinja";
    // ================================================================

    public static void main(String[] args) throws Exception {
        String pythonFile = args.length > 0 ? args[0] : PYTHON_FILE;
        String jinjaFile  = args.length > 1 ? args[1] : JINJA_FILE;
        run(pythonFile, jinjaFile);
    }

    public static void run(String pythonFile, String jinjaFile) throws Exception {

        banner("PIPELINE TEST", "Python: " + pythonFile + "   |   Jinja: " + jinjaFile);

        String pythonSource = Files.readString(Path.of(pythonFile));
        String jinjaSource = Files.readString(Path.of(jinjaFile));

        /* ============================================================
           STEP 1 - Python Lexer
           ============================================================ */
        section(1, "PYTHON LEXER", "Token stream produced from " + pythonFile);
        TokenPrinter.printText(newPythonLexer(pythonSource));

        /* ============================================================
           STEP 2 - Python Parser
           ============================================================ */
        section(2, "PYTHON PARSER", "Parse tree produced by PythonParser.program()");

        BasePythonLexer pythonLexer = newPythonLexer(pythonSource);
        CommonTokenStream pythonTokens = new CommonTokenStream(pythonLexer);
        PythonParser pythonParser = new PythonParser(pythonTokens);
        attachErrorListener(pythonLexer, pythonParser, "Python");

        ParseTree pythonParseTree = pythonParser.program();
        ParseTreePrinter.printText(pythonParseTree, pythonParser);

        /* ============================================================
           STEP 3 - Python AST
           ============================================================ */
        section(3, "PYTHON AST", "Built by compiler.frontend.python.AstBuilder");

        Program program = (Program) new compiler.visitor.python.AstBuilder().visit(pythonParseTree);
        ASTPrinter.printText(program);

        /* ============================================================
           STEP 4 - Python Semantic Analysis
           ============================================================ */
        section(4, "PYTHON SEMANTIC ANALYSIS", "PythonSemanticAnalyzer + SymbolTable");

        PythonSemanticAnalyzer pythonAnalyzer = new PythonSemanticAnalyzer();
        List<SemanticError> pythonErrors = pythonAnalyzer.analyze(program);
        SymbolTable pythonSymbols = pythonAnalyzer.getSymbolTable();

        printErrors("Python", pythonErrors);
        System.out.println("--- Python symbol table ---");
        pythonSymbols.prettyPrint();

        /* ============================================================
           STEP 5 - Generator (Python data -> Jinja context)
           ============================================================ */
        section(5, "GENERATOR", "Resolving render_template(...) and extracting its context");

        Generator generator = new Generator();
        List<Generator.RenderCall> renderCalls = generator.generate(program, pythonSymbols);

        Generator.RenderCall renderCall;
        if (renderCalls.isEmpty()) {
            System.out.println("No render_template(...) call found in " + pythonFile + ".");
            System.out.println("Falling back to an empty context - only names the template");
            System.out.println("doesn't reference (or Python module-level constants) will render correctly.");
            renderCall = new Generator.RenderCall(jinjaFile, -1, new LinkedHashMap<>(), new SymbolTable());
        } else {
            if (renderCalls.size() > 1) {
                System.out.println(renderCalls.size() + " render_template(...) calls found - using the first one.");
            }
            renderCall = renderCalls.get(0);
            System.out.println("render_template(\"" + renderCall.getTemplateName() + "\", ...) found at line " + renderCall.getLine());
        }

        System.out.println("\n--- Context values (what the template will actually see) ---");
        if (renderCall.getContextValues().isEmpty()) {
            System.out.println("(empty)");
        } else {
            for (Map.Entry<String, Object> entry : renderCall.getContextValues().entrySet()) {
                System.out.println("  " + entry.getKey() + " = " + describe(entry.getValue()));
            }
        }

        System.out.println("\n--- Context symbol table (handed to JinjaSemanticAnalyzer) ---");
        renderCall.getContextSymbolTable().prettyPrint();

        /* ============================================================
           STEP 6 - Jinja Lexer
           ============================================================ */
        section(6, "JINJA LEXER", "Token stream produced from " + jinjaFile);
        TokenPrinter.printText(new JinjaLexer(CharStreams.fromString(jinjaSource)));

        /* ============================================================
           STEP 7 - Jinja Parser
           ============================================================ */
        section(7, "JINJA PARSER", "Parse tree produced by JinjaParser.template()");

        CharStream jinjaInput = CharStreams.fromString(jinjaSource);
        JinjaLexer jinjaLexer = new JinjaLexer(jinjaInput);
        CommonTokenStream jinjaTokens = new CommonTokenStream(jinjaLexer);
        JinjaParser jinjaParser = new JinjaParser(jinjaTokens);
        attachErrorListener(jinjaLexer, jinjaParser, "Jinja");

        ParseTree jinjaParseTree = jinjaParser.template();
        ParseTreePrinter.printText(jinjaParseTree, jinjaParser);

        /* ============================================================
           STEP 8 - Jinja AST
           ============================================================ */
        section(8, "JINJA AST", "Built by compiler.frontend.jinja.AstBuilder");

        Template template = (Template) new compiler.visitor.jinja.AstBuilder().visit(jinjaParseTree);
        ASTPrinter.printText(template);

        /* ============================================================
           STEP 9 - Jinja Semantic Analysis
           ============================================================ */
        section(9, "JINJA SEMANTIC ANALYSIS", "Checked against the Generator's context");

        JinjaSemanticAnalyzer jinjaAnalyzer = new JinjaSemanticAnalyzer(renderCall.getContextSymbolTable());
        List<SemanticError> jinjaErrors = jinjaAnalyzer.analyze(template);
        printErrors("Jinja", jinjaErrors);

        /* ============================================================
           STEP 10 - Render (code generation)
           ============================================================ */
        section(10, "RENDER", "Final HTML produced by Renderer.render(template, context)");

        if (!jinjaErrors.isEmpty()) {
            System.out.println("(Rendering anyway despite the semantic errors above, so you can");
            System.out.println(" see the output - fix the template/context before trusting it.)\n");
        }

        Renderer renderer = new Renderer();
        String html = renderer.render(template, renderCall.getContextValues());
        System.out.println(html);

        banner("PIPELINE COMPLETE", pythonFile + " + " + jinjaFile + " -> " + html.length() + " characters of HTML");
    }

    /* -------------------------
       Helpers
       ------------------------- */

    private static BasePythonLexer newPythonLexer(String source) {
        return new BasePythonLexer(CharStreams.fromString(source));
    }

    private static void printErrors(String label, List<SemanticError> errors) {
        if (errors.isEmpty()) {
            System.out.println(label + ": no semantic errors.");
        } else {
            System.out.println(label + ": " + errors.size() + " semantic error(s):");
            for (SemanticError error : errors) {
                System.out.println("  " + error);
            }
        }
        System.out.println();
    }

    private static String describe(Object value) {
        if (value == null) return "None";
        if (value instanceof List) return "List of " + ((List<?>) value).size() + " item(s) -> " + value;
        if (value instanceof Map) return "Dict with keys " + ((Map<?, ?>) value).keySet();
        return String.valueOf(value) + "  (" + value.getClass().getSimpleName() + ")";
    }

    private static void attachErrorListener(org.antlr.v4.runtime.Lexer lexer, org.antlr.v4.runtime.Parser parser, String label) {
        lexer.removeErrorListeners();
        parser.removeErrorListeners();
        BaseErrorListener listener = new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                     int line, int charPositionInLine, String msg, RecognitionException e) {
                System.err.printf("[%s SYNTAX ERROR] %d:%d - %s%n", label, line, charPositionInLine, msg);
            }
        };
        lexer.addErrorListener(listener);
        parser.addErrorListener(listener);
    }

    private static void banner(String title, String subtitle) {
        String line = "=".repeat(72);
        System.out.println("\n" + line);
        System.out.println(title);
        System.out.println(subtitle);
        System.out.println(line);
    }

    private static void section(int step, String title, String subtitle) {
        String line = "-".repeat(72);
        System.out.println("\n" + line);
        System.out.println("STEP " + step + ": " + title);
        System.out.println(subtitle);
        System.out.println(line);
    }
}
