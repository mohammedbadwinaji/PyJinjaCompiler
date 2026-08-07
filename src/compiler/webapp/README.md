# Running the demo web app

## Where these files go

```
compiler/webapp/ProductStore.java
compiler/webapp/TemplateEngine.java
compiler/webapp/WebServer.java
templates/list.html
templates/detail.html
templates/form.html
```

Drop the three `.java` files into your existing source tree under
`compiler/webapp/`. Keep the `templates/` folder wherever you'll run the
program from (or pass its path as a command-line argument - see below).

## Build

Compile alongside the rest of your project, on the same classpath you
already use for the ANTLR runtime and your other `compiler.*` classes:

```
javac -cp path/to/antlr-4.13.2-complete.jar -d out $(find compiler -name "*.java")
```

## Run

```
java -cp "out;path/to/antlr-4.13.2-complete.jar" compiler.webapp.WebServer templates
```

(use `:` instead of `;` between classpath entries on macOS/Linux)

If you omit the `templates` argument it defaults to a `templates`
folder in the current working directory.

Then open **http://localhost:8080/products** in a browser.

## What it demonstrates

Every page - list, add, edit, view details, delete-and-redirect - is
produced by `TemplateEngine`, which runs your own
`JinjaLexer -> JinjaParser -> compiler.frontend.jinja.AstBuilder ->
compiler.generator.Renderer` chain against the real `.html` files in
`templates/`. `ProductStore` is just an in-memory stand-in for what
would otherwise be a database, so the whole thing runs with zero
external dependencies beyond the ANTLR runtime you already need.

`form.html` is the one template that exercises `{% if %}/{% else %}`
for real (switching between "Add Product" / "Edit Product" and between
the two form-submission URLs based on `mode`) - worth pointing to in
your report as the {% if %} feature actually doing something, not just
existing.

## Note on requirement 4 (semantic analysis) in this flow

This server calls `Renderer.render(...)` directly rather than running
`JinjaSemanticAnalyzer` first, since these three templates are trusted,
hand-written files, not user-submitted code - semantic *analysis* is
what your compiler does to Python/Jinja source before trusting it
enough to render, which the Generator + JinjaSemanticAnalyzer pipeline
in PIPELINE_USAGE.md already demonstrates. If your instructor wants to
see that check happen inline here too, TemplateEngine.parse(...) is
the right place to also run JinjaSemanticAnalyzer against a
SymbolTable built from the context keys before rendering - a small
addition if you want the web app to double as a live demo of requirement 4.
