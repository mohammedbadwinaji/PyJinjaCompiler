# exercise_supported_syntax_fixed.py
@app.route("/")
def index():
    products = [
        {"name": "Phone", "price": 300},
        {"name": "Tablet", "price": 400}
    ]
    total = 0
    i = 0
    while i < len(products):
        p = products[i]
        if p.price > 350:
            save(p)
        elif p.price == 300:
            save(p)
        else:
            log(p)
        i = i + 1

    summary = render_template(
        "index.jinja",
        title=title,
        products=products
    )
    return summary

def simple_exprs():
    a = 1
    b = 2
    # replaced '//' with '/' to match grammar
    c = a + b * (a - b) / 1 % 3
    flag = True
    nothing = None
    s = ("hello")
    lst = [1, 2, 3]
    d = {"x": 10, y: 20}
    print(c)
    return c

def trailers_and_calls(x, y):
    obj = container.item
    val = container[0]
    res = service.call(x, y)
    nested = outer(inner(1), key = val)
    chained = obj.method()[0].field
    return res

def if_single_line():
    # moved return into indented suite (single-line suite doesn't accept RETURN)
    return 1

def assign_chain():
    a = b = c = 0
    a = 5
    b = a + 2
    c = b * 3

def for_and_index():
    items = [ {"id":1}, {"id":2}, {"id":3} ]
    for it in items:
        process(it.id)
    return items[0]["id"]

def call_examples():
    r1 = fn(1, 2, key = 3)
    r2 = fn_only_kw(key1 = 10, key2 = 20)
    return r1, r2

top_list = [1, 2, 3]
top_dict = {"a": 1, b: 2}
