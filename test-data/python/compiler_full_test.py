# 1. DEFINE HELPER FUNCTIONS FIRST FOR SINGLE-PASS COMPILING
def showProducts(view_name, data_list):
    render_template("products.jinja", products=data_list, view_title=view_name)

def showProductDetails(product_item):
    render_template("product_details.jinja", product=product_item)

def viewAddForm():
    render_template("add_product.jinja", header="Add New Product")

def viewUpdateForm(product_item):
    render_template("update_product.jinja", product=product_item, header="Modify Product")


# 2. STATIC INVENTORY DATA LAYOUTS (With live, direct image URLs)
products_initial = [
    {
        "id": 0,
        "category": "Electronics",
        "title": "Quantum ANC Wireless Headphones",
        "description": "Immersive sound with active noise cancellation.",
        "currentPrice": 149.99,
        "originalPrice": 199.99,
        # Direct link to a real headphones photo
        "image": "https://unsplash.com"
    },
    {
        "id": 1,
        "category": "Electronics",
        "title": "Apex Mechanical Gaming Keyboard",
        "description": "Ultra-responsive RGB backlit keyboard with linear switches.",
        "currentPrice": 89.99,
        "originalPrice": 119.99,
        # Direct link to a real keyboard photo
        "image": "https://unsplash.com"
    }
]

products_after_crud = [
    {
        "id": 1,
        "category": "Electronics",
        "title": "Apex Mechanical Keyboard V2",
        "description": "Ultra-responsive RGB backlit keyboard with linear switches.",
        "currentPrice": 95.00,
        "originalPrice": 119.99,
        "image": "https://unsplash.com"
    },
    {
        "id": 2,
        "category": "Electronics",
        "title": "Smart Watch Series X",
        "description": "Track health metrics.",
        "currentPrice": 199.99,
        "originalPrice": 249.99,
        # Direct link to a real smart watch photo
        "image": "https://unsplash.com"
    }
]


# 3. EXECUTING COMPILER SNAPSHOT RENDERS
showProducts("Initial_Inventory_List", products_initial)

showProductDetails(products_initial[0])
viewAddForm()
viewUpdateForm(products_initial[0])

showProducts("Updated_Inventory_List", products_after_crud)
