import compiler.webapp.WebServer;
import test.CompilerPipelineTest;
import test.PythonAstBuilderTest;
import test.Tester;

import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length > 0 && args[0].equals("--web")) {
            // Web server mode
            Path templatesDir = Path.of(args.length > 1 ? args[1] : "test-data/jinja");
            int port = args.length > 2 ? Integer.parseInt(args[2]) : 8080;
            
            System.out.println("========================================");
            System.out.println("PyJinjaCompiler Web Application");
            System.out.println("========================================");
            System.out.println("Templates directory: " + templatesDir);
            System.out.println("Starting server on port " + port + "...");
            
            WebServer server = new WebServer(templatesDir);
            server.start(port);
            
            System.out.println("========================================");
            System.out.println("Server started on:");
            System.out.println("http://localhost:" + port + "/");
            System.out.println("http://localhost:" + port + "/products");
            System.out.println("========================================");
            System.out.println("Press Ctrl+C to stop.");
            
        } else {
            Compiler.compile("test-data/python/test.py", "test-data/jinja");
        }
    }
}