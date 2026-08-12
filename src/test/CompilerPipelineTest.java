package test;

import compiler.ast.jinja.Template;
import compiler.ast.python.Program;

import compiler.generated.python.BasePythonLexer;
import compiler.generated.python.PythonParser;
import compiler.generated.jinja.JinjaLexer;
import compiler.generated.jinja.JinjaParser;

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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * End-to-end integration test that executes the complete compiler pipeline
 * starting from a Python source file and ending with generated HTML.
 *
 * The test automatically discovers Jinja template names from render_template()
 * calls in the Python source, so only the Python file and templates directory
 * need to be specified.
 */
public class CompilerPipelineTest {

    /**
     * Run the complete compiler pipeline.
     *
     * @param pythonFile Path to the Python source file
     * @param templatesDirectory Path to the directory containing Jinja templates
     * @return List of all generated HTML strings
     * @throws IOException If file operations fail
     */
    public List<String> test(String pythonFile, String templatesDirectory) throws IOException {
        banner("COMPILER PIPELINE TEST", "Python: " + pythonFile + "   |   Templates: " + templatesDirectory);

        // Validate Python file exists
        Path pythonPath = Path.of(pythonFile);
        if (!Files.exists(pythonPath)) {
            System.err.println("Python source file not found: " + pythonFile);
            return new ArrayList<>();
        }

        // Validate templates directory exists
        Path templatesPath = Path.of(templatesDirectory);
        if (!Files.exists(templatesPath)) {
            System.err.println("Templates directory not found: " + templatesDirectory);
            return new ArrayList<>();
        }

        String pythonSource = Files.readString(pythonPath);

        // List to collect all generated HTML
        List<String> allGeneratedHtml = new ArrayList<>();

        /* ============================================================
           PHASE 1: Python Compilation
           ============================================================ */
        section("PYTHON COMPILATION");

        // Step 1: Python Lexer
        BasePythonLexer pythonLexer = new BasePythonLexer(CharStreams.fromString(pythonSource));

        // Step 2: Python Parser
        CommonTokenStream pythonTokens = new CommonTokenStream(pythonLexer);
        PythonParser pythonParser = new PythonParser(pythonTokens);
        attachErrorListener(pythonLexer, pythonParser, "Python");

        ParseTree pythonParseTree = pythonParser.program();
        if (pythonParseTree == null || pythonParser.getNumberOfSyntaxErrors() > 0) {
            System.err.println("Python parsing failed.");
            return null;
        }

        // Step 3: Python AST Builder
        Program program = (Program) new compiler.frontend.python.AstBuilder().visit(pythonParseTree);
        if (program == null) {
            System.err.println("Python AST building failed.");
            return null;
        }

        // Step 4: Python Semantic Analysis
        PythonSemanticAnalyzer pythonAnalyzer = new PythonSemanticAnalyzer();
        List<SemanticError> pythonErrors = pythonAnalyzer.analyze(program);
        SymbolTable pythonSymbols = pythonAnalyzer.getSymbolTable();

        printSectionHeader("Python Semantic Analysis");
        printErrors(pythonErrors);

        printSectionHeader("Python Symbol Table");
        pythonSymbols.prettyPrint();

        if (!pythonErrors.isEmpty()) {
            System.err.println("Stopping pipeline due to Python semantic errors.");
            return allGeneratedHtml;
        }

        /* ============================================================
           PHASE 2: Generator - Discover render_template calls
           ============================================================ */
        section("GENERATOR PHASE");

        Generator generator = new Generator();
        List<Generator.RenderCall> renderCalls = generator.generate(program, pythonSymbols);

        if (renderCalls.isEmpty()) {
            System.out.println("No render_template calls found.");
            return allGeneratedHtml;
        }

        System.out.println("Found " + renderCalls.size() + " render_template call(s).");

        /* ============================================================
           PHASE 3: Process each render call
           ============================================================ */
        for (int i = 0; i < renderCalls.size(); i++) {
            Generator.RenderCall renderCall = renderCalls.get(i);
            String html = processRenderCall(renderCall, i + 1, templatesPath);
            if (html != null) {
                allGeneratedHtml.add(html);
            }
        }

        banner("PIPELINE COMPLETE", "Processed " + renderCalls.size() + " render call(s)");
        return allGeneratedHtml;
    }

    /**
     * Process a single render call through Jinja compilation and rendering.
     *
     * @return Generated HTML string, or null if rendering failed
     */
    private String processRenderCall(Generator.RenderCall renderCall, int callNumber, Path templatesDirectory) {
        String templateName = renderCall.getTemplateName();
        System.out.println("\n" + "=".repeat(72));
        System.out.println("Render Call #" + callNumber);
        System.out.println("Template: " + templateName);
        System.out.println("Line: " + renderCall.getLine());
        System.out.println("=".repeat(72));

        // Resolve template path
        Path templatePath = templatesDirectory.resolve(templateName);
        if (!Files.exists(templatePath)) {
            System.err.println("Jinja template not found: " + templatePath);
            return null;
        }

        try {
            String jinjaSource = Files.readString(templatePath);

            /* ============================================================
               Jinja Compilation
               ============================================================ */
            section("JINJA COMPILATION");

            // Jinja Lexer
            CharStream jinjaInput = CharStreams.fromString(jinjaSource);
            JinjaLexer jinjaLexer = new JinjaLexer(jinjaInput);

            // Jinja Parser
            CommonTokenStream jinjaTokens = new CommonTokenStream(jinjaLexer);
            JinjaParser jinjaParser = new JinjaParser(jinjaTokens);
            attachErrorListener(jinjaLexer, jinjaParser, "Jinja");

            ParseTree jinjaParseTree = jinjaParser.template();
            if (jinjaParseTree == null || jinjaParser.getNumberOfSyntaxErrors() > 0) {
                System.err.println("Jinja parsing failed for template: " + templateName);
                return null;
            }

            // Jinja AST Builder
            Template template = (Template) new compiler.frontend.jinja.AstBuilder().visit(jinjaParseTree);
            if (template == null) {
                System.err.println("Jinja AST building failed for template: " + templateName);
                return null;
            }

            /* ============================================================
               Jinja Semantic Analysis
               ============================================================ */
            printSectionHeader("Jinja Semantic Analysis");

            JinjaSemanticAnalyzer jinjaAnalyzer = new JinjaSemanticAnalyzer(renderCall.getContextSymbolTable());
            List<SemanticError> jinjaErrors = jinjaAnalyzer.analyze(template);
            printErrors(jinjaErrors);

            if (!jinjaErrors.isEmpty()) {
                System.err.println("Stopping rendering due to Jinja semantic errors.");
                return null;
            }

            /* ============================================================
               Rendering Phase
               ============================================================ */
            printSectionHeader("Generated HTML");

            Renderer renderer = new Renderer();
            String html = renderer.render(template, renderCall.getContextValues());
            System.out.println(html);

            System.out.println("\nGenerated " + html.length() + " characters of HTML.");

            // Write HTML to file
            writeHtmlToFile(templateName, html);

            return html;

        } catch (IOException e) {
            System.err.println("Error reading template file: " + templatePath);
            e.printStackTrace();
            return null;
        }
    }

    /* -------------------------
       Helper Methods
       ------------------------- */

    private void writeHtmlToFile(String templateName, String html) {
        try {
            // Create output directory if it doesn't exist
            Path outputDir = Path.of("test-data/generated-html");
            if (!Files.exists(outputDir)) {
                Files.createDirectories(outputDir);
            }

            // Convert template name to HTML filename (e.g., products.jinja -> products.html)
            String htmlFileName = templateName.replace(".jinja", ".html");
            Path outputPath = outputDir.resolve(htmlFileName);

            // Write HTML to file
            Files.writeString(outputPath, html);
            System.out.println("HTML written to: " + outputPath);

        } catch (IOException e) {
            System.err.println("Error writing HTML to file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void banner(String title, String subtitle) {
        String line = "=".repeat(72);
        System.out.println("\n" + line);
        System.out.println(title);
        System.out.println(subtitle);
        System.out.println(line);
    }

    private static void section(String title) {
        String line = "-".repeat(72);
        System.out.println("\n" + line);
        System.out.println(title);
        System.out.println(line);
    }

    private static void printSectionHeader(String title) {
        System.out.println("\n" + "=".repeat(72));
        System.out.println(title);
        System.out.println("=".repeat(72));
    }

    private static void printErrors(List<SemanticError> errors) {
        if (errors.isEmpty()) {
            System.out.println("No semantic errors.");
        } else {
            System.out.println(errors.size() + " semantic error(s):");
            for (SemanticError error : errors) {
                System.out.println("  " + error);
            }
        }
        System.out.println();
    }

    private static void attachErrorListener(org.antlr.v4.runtime.Lexer lexer, 
                                            org.antlr.v4.runtime.Parser parser, 
                                            String label) {
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
}
