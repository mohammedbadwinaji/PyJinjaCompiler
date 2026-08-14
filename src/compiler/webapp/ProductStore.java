package compiler.webapp;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory product catalog backing the demo CRUD web app. Each product
 * is a Map<String, Object> (id/name/price/description) - the exact shape
 * ExpressionEvaluator/Renderer already expect, since it's the same shape
 * Generator produces from a Python `products = [{...}, ...]` literal.
 */
public final class ProductStore {

    private final Map<Long, Map<String, Object>> products = new LinkedHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    public ProductStore() {
        add("Phone", "Electronics", 300.0, "A smartphone with a large display.", "images/phone.jpg");
        add("Tablet", "Electronics", 400.0, "A lightweight tablet, great for reading.", "images/tablet.jpg");
        add("Laptop", "Electronics", 900.0, "A powerful laptop for development work.", "images/laptop.jpg");
    }

    public synchronized Map<String, Object> add(String title, String category, double currentPrice, String description, String imageUrl) {
        long id = nextId.getAndIncrement();
        Map<String, Object> product = new LinkedHashMap<>();
        product.put("id", id);
        product.put("title", title);
        product.put("category", category);
        product.put("currentPrice", currentPrice);
        product.put("description", description);
        product.put("imageUrl", imageUrl);
        products.put(id, product);
        return product;
    }

    public synchronized List<Object> all() {
        return new ArrayList<>(products.values());
    }

    public synchronized Map<String, Object> get(long id) {
        return products.get(id);
    }

    public synchronized boolean update(long id, String title, String category, double currentPrice, String description, String imageUrl) {
        Map<String, Object> product = products.get(id);
        if (product == null) {
            return false;
        }
        product.put("title", title);
        product.put("category", category);
        product.put("currentPrice", currentPrice);
        product.put("description", description);
        product.put("imageUrl", imageUrl);
        return true;
    }

    public synchronized boolean delete(long id) {
        return products.remove(id) != null;
    }
}
