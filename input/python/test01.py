@app.route("/")
def index():
    products = [
        {"name": "Phone", "price": 300},
        {"name": "Tablet", "price": 400}
    ]
    total = 0
    for p in products:
        if p.price > 350:
            save(p)
        elif p.price == 300:
            save(p)
        else:
            log(p)

    summary = render_template(
        "index.jinja",
        title=title,
        products=products
    )
    return summary
