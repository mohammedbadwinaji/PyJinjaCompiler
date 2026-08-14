# Duplicate Function
def duplicateFunction(x , y):
    return x + y

def duplicateFunction (x , y) :
    return y + y

# Duplicate Argument Name
def duplicateParameterName(x,x):
    return x + x

# Invalid Operation# Type Mismatch
typeMismatch = not 10
invalidOperation = 5 and True
invalidOperation = 6 / True
typeMismatch = 10 + "Ahmad"

# Undefined Function
num = undefinedFunction()

# Undefined Variable
num = undefinedVariable

# Not Callable
notcallable = 10
notcallable()

# Wrong Argument Count
def wrongArgumentCount(x , y) :
    return  x * y

wrongArgumentCount(10)

# Non Iterable Type Used In For
nonIterable = 5
for i in nonIterable :
    i = 5


# def renderJinjaTemplate() :
#     nonIterable = 15.0
#     notCallable = "Mohammed"
#     render_template("semanticErrors.jinja",nonIterable=nonIterable,notCallable=notCallable)
# renderJinjaTemplate()
