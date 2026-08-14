package compiler.webapp;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        Path templatesDir = Path.of(args.length > 0 ? args[0] : "test-data/jinja");
        WebServer server = new WebServer(templatesDir);
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 8080;
        server.start(port);
    }

    public void start(int port) throws IOException {
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        httpServer.createContext("/", this::handle);
        httpServer.createContext("/images", this::serveStaticFile);
        httpServer.setExecutor(null);
        httpServer.start();
        System.out.println("Listening on http://localhost:" + port + "/products");
    }

    private void handle(HttpExchange exchange) {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            System.out.println("[HTTP] " + method + " " + path);
            route(exchange);
        } catch (Exception e) {
            System.err.println("[ERROR] " + e.getMessage());
            e.printStackTrace();
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
        context.put("view_title", "Product Catalog");
        renderResponse(exchange, "products.jinja", context);
    }

    private void newProductForm(HttpExchange exchange) {
        Map<String, Object> context = baseContext();
        context.put("header", "Add Product");
        context.put("product", emptyProduct());
        renderResponse(exchange, "add_product.jinja", context);
    }

    private void editProductForm(HttpExchange exchange, long id) {
        Map<String, Object> product = store.get(id);
        if (product == null) {
            respond(exchange, 404, "Product not found");
            return;
        }
        Map<String, Object> context = baseContext();
        context.put("header", "Update Product");
        context.put("product", product);
        renderResponse(exchange, "update_product.jinja", context);
    }

    private void viewProduct(HttpExchange exchange, long id) {
        Map<String, Object> product = store.get(id);
        if (product == null) {
            respond(exchange, 404, "Product not found");
            return;
        }
        Map<String, Object> context = baseContext();
        context.put("product", product);
        renderResponse(exchange, "product_details.jinja", context);
    }

    private void createProduct(HttpExchange exchange) throws IOException {
        Map<String, String> form = readForm(exchange);
        String imageUrl = form.get("imageUrl");
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            imageUrl = "images/screen.jpg";
        }
        store.add(
                form.getOrDefault("title", ""),
                form.getOrDefault("category", "Electronics"),
                parseDouble(form.get("currentPrice")),
                form.getOrDefault("description", ""),
                imageUrl);
        redirect(exchange, "/products");
    }

    private void updateProduct(HttpExchange exchange, long id) throws IOException {
        Map<String, String> form = readForm(exchange);
        String imageUrl = form.get("imageUrl");
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            imageUrl = "images/screen.jpg";
        }
        boolean updated = store.update(
                id,
                form.getOrDefault("title", ""),
                form.getOrDefault("category", "Electronics"),
                parseDouble(form.get("currentPrice")),
                form.getOrDefault("description", ""),
                imageUrl);
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
        product.put("title", "");
        product.put("category", "Electronics");
        product.put("currentPrice", "");
        product.put("description", "");
        product.put("imageUrl", "images/screen.jpg");
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
            System.out.println("[JINJA] Rendering template: " + template);
            String html = templates.render(template, context);
            byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
            System.out.println("[HTTP] 200 OK");
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to render " + template + ": " + e.getMessage());
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

    private void serveStaticFile(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        System.out.println("[STATIC] " + path);
        
        // Remove leading /images/ to get relative path
        String relativePath = path.substring("/images/".length());
        Path filePath = Paths.get("images").resolve(relativePath).normalize();
        
        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            respond(exchange, 404, "File not found: " + path);
            return;
        }
        
        // Determine content type
        String contentType = "application/octet-stream";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) {
            contentType = "image/jpeg";
        } else if (path.endsWith(".png")) {
            contentType = "image/png";
        } else if (path.endsWith(".gif")) {
            contentType = "image/gif";
        } else if (path.endsWith(".svg")) {
            contentType = "image/svg+xml";
        } else if (path.endsWith(".css")) {
            contentType = "text/css";
        } else if (path.endsWith(".js")) {
            contentType = "application/javascript";
        }
        
        byte[] bytes = Files.readAllBytes(filePath);
        exchange.getResponseHeaders().add("Content-Type", contentType);
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
        System.out.println("[STATIC] 200 OK");
    }
}
