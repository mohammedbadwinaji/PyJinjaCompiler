def showProducts() :
    products = [
        {
            "category": "Electronics",
            "title": "Quantum ANC Wireless Headphones",
            "description": "Immersive over-ear headphones featuring hybrid active noise cancellation, 50-hour battery life, and high-fidelity spatial audio.",
            "currentPrice": 149.99,
            "originalPrice": 199.99,
            "image": "https://unsplash.com"
        },
        {
            "category": "Electronics",
            "title": "Apex Mechanical Gaming Keyboard",
            "description": "Ultra-responsive RGB backlit keyboard with hot-swappable linear switches and an aircraft-grade aluminum top frame.",
            "currentPrice": 89.99,
            "originalPrice": 119.99,
            "image": "https://unsplash.com"
        },
        {
            "category": "Apparel",
            "title": "All-Weather Performance Parka",
            "description": "Windproof and waterproof winter jacket engineered with breathable thermal insulation and an adjustable storm hood.",
            "currentPrice": 175.00,
            "originalPrice": 240.00,
            "image": "https://unsplash.com"
        },
        {
            "category": "Apparel",
            "title": "Classic Leather Chelsea Boots",
            "description": "Handcrafted full-grain leather boots with flexible elastic side panels and durable Goodyear welt construction.",
            "currentPrice": 120.00,
            "originalPrice": 160.00,
            "image": "https://unsplash.com"
        },
        {
            "category": "Home & Kitchen",
            "title": "Barista Pro Espresso Machine",
            "description": "Compact 15-bar pressure espresso maker featuring a built-in commercial steam wand for professional latte art at home.",
            "currentPrice": 299.95,
            "originalPrice": 379.95,
            "image": "https://unsplash.com"
        },
        {
            "category": "Home & Kitchen",
            "title": "Smart Air Purifier HEPA H13",
            "description": "Medical-grade air filtration system that removes 99.97% of airborne particles, compatible with Alexa and Google Assistant.",
            "currentPrice": 79.00,
            "originalPrice": 99.00,
            "image": "https://unsplash.com"
        },
        {
            "category": "Fitness & Outdoors",
            "title": "Ergonomic Hydration Backpack",
            "description": "Lightweight 10L trail running pack equipped with a leak-proof 2-liter water bladder and breathable mesh shoulder straps.",
            "currentPrice": 45.50,
            "originalPrice": 65.00,
            "image": "https://unsplash.com"
        },
        {
            "category": "Fitness & Outdoors",
            "title": "Adjustable Smart Dumbbell Set",
            "description": "Space-saving strength training weights that quickly adjust from 5 to 52.5 lbs with a smooth dial mechanism.",
            "currentPrice": 249.99,
            "originalPrice": 329.99,
            "image": "https://unsplash.com"
        }
    ]
    render_template("products.jinja", products = products)

def addProduct() :
    header = "Add Product"
    render_template("add_product.jinja",header=header,)



def showPersonInfo(name,age,gender) :
    render_template("personInfo.jinja",name=name,age=age,gender=gender)


name = "Yaya"
def updateProduct() :
    category =  "Home & Kitchen"
    title =  "Barista Pro Espresso Machine"
    description =  "Compact 15-bar pressure espresso maker featuring a built-in commercial steam wand for professional latte art at home."
    currentPrice =  299.95
    originalPrice =  379.95
    render_template("personInfo.jinja",title=title,description=description,currentPrice=currentPrice,originalPrice=originalPrice,category)


age = 14
gender = "Male"
showPersonInfo(name,age,gender)


showProducts()



