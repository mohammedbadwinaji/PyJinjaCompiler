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

import compiler.printer.TokenPrinter;
import compiler.printer.ParseTreePrinter;
import compiler.printer.ASTPrinter;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Orchestrates the complete compiler pipeline from Python source to generated HTML.
 * Implements a fail-fast two-pass semantic analysis for Python and processes all Jinja templates.
 */
public class Compiler {

    /**
     * Runs the complete compiler pipeline.
     *
     * @param pythonFilePath Path to the Python source file
     * @param jinjaFolderPath Path to the directory containing Jinja templates
     */
    public static void run(String pythonFilePath, String jinjaFolderPath) {
        printBanner("COMPILER PIPELINE START", "Python: " + pythonFilePath + " | Templates: " + jinjaFolderPath);

        // Validate inputs
        Path pythonPath = Paths.get(pythonFilePath);
        Path jinjaPath = Paths.get(jinjaFolderPath);

        if (!Files.exists(pythonPath)) {
            printErrorBanner("Python source file not found: " + pythonFilePath);
            return;
        }

        if (!Files.exists(jinjaPath)) {
            printErrorBanner("Jinja templates directory not found: " + jinjaFolderPath);
            return;
        }

        try {
            String pythonSource = Files.readString(pythonPath);

            // ============================================================
            // PHASE 1: Python Compilation
            // ============================================================
            printSectionBanner("PHASE 1: PYTHON COMPILATION");

            // Step 1: Python Lexer
            printSubsectionBanner("Python Lexer - Token Stream");
            BasePythonLexer pythonLexer = new BasePythonLexer(CharStreams.fromString(pythonSource));
            TokenPrinter.printText(pythonLexer);

            // Reset lexer for parser
            pythonLexer = new BasePythonLexer(CharStreams.fromString(pythonSource));

            // Step 2: Python Parser
            printSubsectionBanner("Python Parser - Parse Tree (CST)");
            CommonTokenStream pythonTokens = new CommonTokenStream(pythonLexer);
            PythonParser pythonParser = new PythonParser(pythonTokens);
            attachErrorListener(pythonLexer, pythonParser, "Python");

            ParseTree pythonParseTree = pythonParser.program();
            if (pythonParseTree == null || pythonParser.getNumberOfSyntaxErrors() > 0) {
                printErrorBanner("Python parsing failed. Pipeline halted.");
                return;
            }
            ParseTreePrinter.printText(pythonParseTree, pythonParser);

            // Step 3: Python AST Building
            printSubsectionBanner("Python AST Builder - Abstract Syntax Tree");
            Program program = (Program) new compiler.visitor.python.AstBuilder().visit(pythonParseTree);
            if (program == null) {
                printErrorBanner("Python AST building failed. Pipeline halted.");
                return;
            }
            ASTPrinter.printText(program);

            // Step 4: Python Semantic Analysis (Two-Pass)
            printSubsectionBanner("Python Semantic Analysis (Two-Pass)");
            System.out.println("Pass 1: Collecting function signatures...");
            PythonSemanticAnalyzer pythonAnalyzer = new PythonSemanticAnalyzer();
            List<SemanticError> pythonErrors = pythonAnalyzer.analyze(program);
            SymbolTable pythonSymbols = pythonAnalyzer.getSymbolTable();

            System.out.println("Pass 2: Verifying types, scopes, and assignments...");
            printErrors(pythonErrors);

            printSubsectionBanner("Python Symbol Table");
            pythonSymbols.prettyPrint();

            if (!pythonErrors.isEmpty()) {
                printErrorBanner("Python semantic errors detected. Pipeline halted.");
                return;
            }

            // ============================================================
            // PHASE 2: Generator - Discover render_template calls
            // ============================================================
            printSectionBanner("PHASE 2: GENERATOR - DISCOVER RENDER CALLS");
            Generator generator = new Generator();
            List<Generator.RenderCall> renderCalls = generator.generate(program, pythonSymbols);

            if (renderCalls.isEmpty()) {
                System.out.println("No render_template calls found in Python source.");
                printBanner("PIPELINE COMPLETE", "No templates to process");
                return;
            }

            System.out.println("Found " + renderCalls.size() + " render_template call(s).");

            // ============================================================
            // PHASE 3: Process each render call
            // ============================================================
            printSectionBanner("PHASE 3: JINJA TEMPLATE PROCESSING");

            int successCount = 0;
            for (int i = 0; i < renderCalls.size(); i++) {
                Generator.RenderCall renderCall = renderCalls.get(i);
                String templateName = renderCall.getTemplateName();
                
                System.out.println();
                printTemplateBanner("Render Call #" + (i + 1));
                System.out.println("Template: " + templateName);
                System.out.println("Line: " + renderCall.getLine());

                // Resolve template path
                Path templatePath = jinjaPath.resolve(templateName);
                if (!Files.exists(templatePath)) {
                    printErrorBanner("Jinja template not found: " + templatePath + ". Pipeline halted.");
                    return;
                }

                try {
                    String jinjaSource = Files.readString(templatePath);

                    // Jinja Lexer & Parser
                    printSubsectionBanner("Jinja Lexer & Parser");
                    CharStream jinjaInput = CharStreams.fromString(jinjaSource);
                    JinjaLexer jinjaLexer = new JinjaLexer(jinjaInput);
                    CommonTokenStream jinjaTokens = new CommonTokenStream(jinjaLexer);
                    JinjaParser jinjaParser = new JinjaParser(jinjaTokens);
                    attachErrorListener(jinjaLexer, jinjaParser, "Jinja");

                    ParseTree jinjaParseTree = jinjaParser.template();
                    if (jinjaParseTree == null || jinjaParser.getNumberOfSyntaxErrors() > 0) {
                        printErrorBanner("Jinja parsing failed for template: " + templateName + ". Pipeline halted.");
                        return;
                    }
                    ParseTreePrinter.printText(jinjaParseTree, jinjaParser);

                    // Jinja AST Builder
                    printSubsectionBanner("Jinja AST Builder - Abstract Syntax Tree");
                    Template template = (Template) new compiler.visitor.jinja.AstBuilder().visit(jinjaParseTree);
                    if (template == null) {
                        printErrorBanner("Jinja AST building failed for template: " + templateName + ". Pipeline halted.");
                        return;
                    }
                    ASTPrinter.printText(template);

                    // Jinja Semantic Analyzer
                    printSubsectionBanner("Jinja Semantic Analysis");
                    JinjaSemanticAnalyzer jinjaAnalyzer = new JinjaSemanticAnalyzer(renderCall.getContextSymbolTable());
                    List<SemanticError> jinjaErrors = jinjaAnalyzer.analyze(template);
                    printErrors(jinjaErrors);

                    if (!jinjaErrors.isEmpty()) {
                        printErrorBanner("Jinja semantic errors detected for template: " + templateName + ". Pipeline halted.");
                        return;
                    }

                    // Jinja Code Generation
                    printSubsectionBanner("Jinja Code Generation - Generated HTML");
                    Renderer renderer = new Renderer();
                    String html = renderer.render(template, renderCall.getContextValues());
                    System.out.println(html);
                    System.out.println("\nGenerated " + html.length() + " characters of HTML.");

                    // Write HTML to file
                    writeHtmlToFile(templateName, html);

                    successCount++;

                } catch (IOException e) {
                    printErrorBanner("Error reading template file: " + templatePath + " - " + e.getMessage());
                    return;
                }
            }

            printBanner("PIPELINE COMPLETE", "Successfully processed " + successCount + "/" + renderCalls.size() + " template(s)");

        } catch (IOException e) {
            printErrorBanner("Error reading Python source file: " + e.getMessage());
        }
    }

    /**
     * Writes generated HTML to a file in the test-data/generated-html directory.
     */
    private static void writeHtmlToFile(String templateName, String html) {
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

    /* -------------------------
       Banner and Formatting Methods
       ------------------------- */

    private static void printBanner(String title, String subtitle) {
        String line = "=".repeat(80);
        System.out.println("\n" + line);
        System.out.println(title);
        System.out.println(subtitle);
        System.out.println(line);
    }

    private static void printErrorBanner(String message) {
        String line = "=".repeat(80);
        System.out.println("\n" + line);
        System.out.println("ERROR: " + message);
        System.out.println(line);
    }

    private static void printSectionBanner(String title) {
        String line = "=".repeat(80);
        System.out.println("\n" + line);
        System.out.println(title);
        System.out.println(line);
    }

    private static void printSubsectionBanner(String title) {
        String line = "-".repeat(80);
        System.out.println("\n" + line);
        System.out.println(title);
        System.out.println(line);
    }

    private static void printTemplateBanner(String title) {
        String line = "*".repeat(80);
        System.out.println(line);
        System.out.println(title);
        System.out.println(line);
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

    private static void attachErrorListener(Lexer lexer, Parser parser, String label) {
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
