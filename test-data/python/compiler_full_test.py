x = 10
def add(x , y) :
    return x+y

def calculateAge() :
    return 10 + 5

title = "Hello World"
render_template("hello.jinja", info = {"name" : "Mohammed","age": (5 + 5)})


render_template("test.jinja",title="Hello World")