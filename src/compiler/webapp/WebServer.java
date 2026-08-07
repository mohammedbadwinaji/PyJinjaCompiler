package compiler.webapp;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Requirement 6: integrated web interfaces (list / add / view details /
 * edit / delete) with smooth navigation between them.
 *
 * Deliberately dependency-free - no Flask, no servlet container, just
 * the JDK's built-in HttpServer - so it runs with nothing more than the
 * project's existing ANTLR runtime on the classpath. The actual HTML for
 * every page comes from your own compiler.frontend.jinja.AstBuilder +
 * compiler.generator.Renderer, via TemplateEngine - this app exists to
 * exercise that pipeline against real requests, not to reimplement Flask.
 *
 * Routes:
 *   GET  /products              -> list.jinja
 *   GET  /products/new          -> form.jinja (mode = "new")
 *   POST /products               (create, then redirect to /products)
 *   GET  /products/{id}         -> detail.jinja
 *   GET  /products/{id}/edit    -> form.jinja (mode = "edit")
 *   POST /products/{id}          (update, then redirect to /products/{id})
 *   POST /products/{id}/delete   (delete, then redirect to /products)
 */
public final class WebServer {

    private static final String NAV_HTML =
            "<nav><a href=\"/products\">Products</a> | " +
            "<a href=\"/products/new\">Add Product</a></nav><hr>";

    private static final Pattern PRODUCT_PATH = Pattern.compile("^/products/(\\d+)$");
    private static final Pattern PRODUCT_EDIT_PATH = Pattern.compile("^/products/(\\d+)/edit$");
    private static final Pattern PRODUCT_DELETE_PATH = Pattern.compile("^/products/(\\d+)/delete$");

    private final ProductStore store = new ProductStore();
    private final TemplateEngine templates;

    public WebServer(Path templatesDir) {
        this.templates = new TemplateEngine(templatesDir);
    }

    public static void main(String[] args) throws IOException {
        Path templatesDir = Path.of(args.length > 0 ? args[0] : "templates");
        WebServer server = new WebServer(templatesDir);
        server.start(8080);
    }

    public void start(int port) throws IOException {
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        httpServer.createContext("/", this::handle);
        httpServer.setExecutor(null);
        httpServer.start();
        System.out.println("Listening on http://localhost:" + port + "/products");
    }

    private void handle(HttpExchange exchange) {
        try {
            route(exchange);
        } catch (Exception e) {
            respond(exchange, 500, "Internal error: " + e.getMessage());
        }
    }

    private void route(HttpExchange exchange) throws IOException {

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        if (method.equals("GET") && path.equals("/products")) {
            listProducts(exchange);

        } else if (method.equals("GET") && path.equals("/products/new")) {
            newProductForm(exchange);

        } else if (method.equals("POST") && path.equals("/products")) {
            createProduct(exchange);

        } else if (method.equals("GET") && PRODUCT_EDIT_PATH.matcher(path).matches()) {
            editProductForm(exchange, idFrom(PRODUCT_EDIT_PATH, path));

        } else if (method.equals("GET") && PRODUCT_PATH.matcher(path).matches()) {
            viewProduct(exchange, idFrom(PRODUCT_PATH, path));

        } else if (method.equals("POST") && PRODUCT_PATH.matcher(path).matches()) {
            updateProduct(exchange, idFrom(PRODUCT_PATH, path));

        } else if (method.equals("POST") && PRODUCT_DELETE_PATH.matcher(path).matches()) {
            deleteProduct(exchange, idFrom(PRODUCT_DELETE_PATH, path));

        } else if (method.equals("GET") && path.equals("/")) {
            redirect(exchange, "/products");

        } else {
            respond(exchange, 404, "Not found: " + method + " " + path);
        }
    }

    private long idFrom(Pattern pattern, String path) {
        Matcher matcher = pattern.matcher(path);
        matcher.matches();
        return Long.parseLong(matcher.group(1));
    }

    /* -------------------------
       Handlers
       ------------------------- */

    private void listProducts(HttpExchange exchange) {
        Map<String, Object> context = baseContext();
        context.put("products", store.all());
        renderResponse(exchange, "list.jinja", context);
    }

    private void newProductForm(HttpExchange exchange) {
        Map<String, Object> context = baseContext();
        context.put("mode", "new");
        context.put("product", emptyProduct());
        renderResponse(exchange, "form.jinja", context);
    }

    private void editProductForm(HttpExchange exchange, long id) {
        Map<String, Object> product = store.get(id);
        if (product == null) {
            respond(exchange, 404, "Product not found");
            return;
        }
        Map<String, Object> context = baseContext();
        context.put("mode", "edit");
        context.put("product", product);
        renderResponse(exchange, "form.jinja", context);
    }

    private void viewProduct(HttpExchange exchange, long id) {
        Map<String, Object> product = store.get(id);
        if (product == null) {
            respond(exchange, 404, "Product not found");
            return;
        }
        Map<String, Object> context = baseContext();
        context.put("product", product);
        renderResponse(exchange, "detail.jinja", context);
    }

    private void createProduct(HttpExchange exchange) throws IOException {
        Map<String, String> form = readForm(exchange);
        store.add(
                form.getOrDefault("name", ""),
                parseDouble(form.get("price")),
                form.getOrDefault("description", ""));
        redirect(exchange, "/products");
    }

    private void updateProduct(HttpExchange exchange, long id) throws IOException {
        Map<String, String> form = readForm(exchange);
        boolean updated = store.update(
                id,
                form.getOrDefault("name", ""),
                parseDouble(form.get("price")),
                form.getOrDefault("description", ""));
        redirect(exchange, updated ? "/products/" + id : "/products");
    }

    private void deleteProduct(HttpExchange exchange, long id) {
        store.delete(id);
        redirect(exchange, "/products");
    }

    /* -------------------------
       Helpers
       ------------------------- */

    private Map<String, Object> baseContext() {
        Map<String, Object> context = new HashMap<>();
        context.put("nav", NAV_HTML);
        return context;
    }

    private Map<String, Object> emptyProduct() {
        Map<String, Object> product = new LinkedHashMap<>();
        product.put("id", "");
        product.put("name", "");
        product.put("price", "");
        product.put("description", "");
        return product;
    }

    private double parseDouble(String s) {
        try {
            return s == null || s.isEmpty() ? 0.0 : Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private void renderResponse(HttpExchange exchange, String template, Map<String, Object> context) {
        try {
            String html = templates.render(template, context);
            byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        } catch (IOException e) {
            respond(exchange, 500, "Failed to render " + template + ": " + e.getMessage());
        }
    }

    private void redirect(HttpExchange exchange, String location) {
        try {
            exchange.getResponseHeaders().add("Location", location);
            exchange.sendResponseHeaders(302, -1);
        } catch (IOException e) {
            respond(exchange, 500, "Redirect failed: " + e.getMessage());
        } finally {
            exchange.close();
        }
    }

    private void respond(HttpExchange exchange, int status, String message) {
        try {
            byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        } catch (IOException ignored) {
            // Best-effort error response - nothing more to do if this fails too.
        }
    }

    private Map<String, String> readForm(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> result = new HashMap<>();
        for (String pair : body.split("&")) {
            if (pair.isEmpty()) continue;
            int eq = pair.indexOf('=');
            String key = eq >= 0 ? pair.substring(0, eq) : pair;
            String value = eq >= 0 ? pair.substring(eq + 1) : "";
            result.put(
                    URLDecoder.decode(key, StandardCharsets.UTF_8),
                    URLDecoder.decode(value, StandardCharsets.UTF_8));
        }
        return result;
    }
}
