# ==========================================
# test_literals_and_collections.py
# Validates literals, lists, and dictionaries
# ==========================================

# 1. Literals (Natively)
10
10.5
"Double Quoted"
'Single Quoted'
True
False
None

# 2. Lists
# Empty list
empty_list = []

# Populated list with mixed literals
populated_list = [1, 2.5, "Three", True, None]

# Nested lists
nested_list = [[1, 2], [3, [4, 5]], []]

# Lists containing expressions
expression_list = [1 + 2, 5 == 5, not True, x * y]

# 3. Dictionaries
# Empty dictionary
empty_dict = {}

# Dictionaries with string keys
string_key_dict = {
    "name": "John",
    "age": 30,
    "is_active": True
}

# Dictionaries with identifier keys
identifier_key_dict = {
    name: "Jane",
    age: 25,
    status: False
}

# Nested dictionaries
nested_dict = {
    "user": {
        "id": 1,
        "preferences": {
            theme: "dark",
            "notifications": True
        }
    },
    metadata: {
        created_at: "2023-10-01"
    }
}

# Dictionaries with expression values
expr_dict = {
    "total": 10 + 20,
    is_valid: 5 == 5 or False,
    "items": [1, 2, 3]
}