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
        add("Phone", 300.0, "A smartphone with a large display.");
        add("Tablet", 400.0, "A lightweight tablet, great for reading.");
        add("Laptop", 900.0, "A powerful laptop for development work.");
    }

    public synchronized Map<String, Object> add(String name, double price, String description) {
        long id = nextId.getAndIncrement();
        Map<String, Object> product = new LinkedHashMap<>();
        product.put("id", id);
        product.put("name", name);
        product.put("price", price);
        product.put("description", description);
        products.put(id, product);
        return product;
    }

    public synchronized List<Object> all() {
        return new ArrayList<>(products.values());
    }

    public synchronized Map<String, Object> get(long id) {
        return products.get(id);
    }

    public synchronized boolean update(long id, String name, double price, String description) {
        Map<String, Object> product = products.get(id);
        if (product == null) {
            return false;
        }
        product.put("name", name);
        product.put("price", price);
        product.put("description", description);
        return true;
    }

    public synchronized boolean delete(long id) {
        return products.remove(id) != null;
    }
}
