


def renderPersonInfo(name , age , gender) :
    render_template("personInfo.jinja",name=name,age=age,gender=gender)


def getName() :
    name = "mohammed"
    return name

def getAge() :
    return 5 + 6 * 10 / 3

def getGender() :
    x = 7
    y = 6
    if ( x > y) :
        return "Male"
    else :
        return "Female"

renderPersonInfo(getName(),getAge(),getGender())




def getStringValue() :
    name = "Ahmad"
    if(name == "Ahmad"):
        return "Name is Ahmad"
    else :
        return  "Name is Not Ahmad"

def getBooleanValue() :
    return (55 < 10) and (5 >= 5) or (6 == 7)

def getIntegerValue():
    return 55 + 10 + 5 * 5

def getFloatValue() :
    return 55.5 + 10.5 + 5.5 * 5.5

def getList():
    return ["hello","Mohammed",{"name": "any name"}]

def getDictionry() :
    return {
        "value1" : "one",
        "value2" : "two"
    }
def renderComplexExpression() :
   list = [
       getStringValue(),
       getBooleanValue(),
       getIntegerValue(),
       getFloatValue(),
       getList(),
       getDictionry()
   ]
   render_template("testArrayAccess.jinja",string=list[0]
                   ,boolean = list[1] , integer=list[2],float=list[3],list=list[4],dictionary=list[5])



renderComplexExpression()


def renderComplexTemplate():
    numbers = [1,2,3,4,5,6]
    names = ["Mohammed","Ahmad","Yaya"]
    products = [
        {
            "price": 15
        },
        {
            "price": 20
        },
        {
            "price" : 30
        }
    ]
    render_template("complex.jinja",numbers=numbers,names=names,products=products)


renderComplexTemplate()


def renderMathCalculation() :
    mathFormulas = [
        {"expr": "5 + 5 * 10", "val": 5 + 5 * 10},  # 55
        {"expr": "6 / 5 - 5 * 1", "val": 6 / 5 - 5 * 1},  # -4
        {"expr": "12 % 5 + 4 * 2", "val": 12 % 5 + 4 * 2},  # 10
        {"expr": "18 * 12 / 9 + 15", "val": 18 * 12 / 9 + 15},  # 39
        {"expr": "20 - 16 / 4 + 7 % 3", "val": 20 - 16 / 4 + 7 % 3},  # 17
        {"expr": "15 * 14 % 8 - 3", "val": 15 * 14 % 8 - 3},  # -1
        {"expr": "16 / 2 * 5 + 13 % 4", "val": 16 / 2 * 5 + 13 % 4},  # 41
        {"expr": "100 * 2 / 10 % 7 + 1", "val": 100 * 2 / 10 % 7 + 1},  # 7
        {"expr": "9 + 9 * 9 / 3 - 5", "val": 9 + 9 * 9 / 3 - 5},  # 31
        {"expr": "25 % 4 * 12 + 8 - 2", "val": 25 % 4 * 12 + 8 - 2},  # 18
        {"expr": "14 + 2 % 15 / 10 * 4", "val": 14 + 2 % 15 / 10 * 4},  # 14
        {"expr": "13 + 7 * 12 - 19 % 5", "val": 13 + 7 * 12 - 19 % 5},  # 93
        {"expr": "45 / 5 * 3 + 12 - 8", "val": 45 / 5 * 3 + 12 - 8},  # 31
        {"expr": "8 - 17 * 2 + 16 / 4", "val": 8 - 17 * 2 + 16 / 4},  # -22
        {"expr": "19 % 2 / 7 + 11 * 3", "val": 19 % 2 / 7 + 11 * 3},  # 33
        {"expr": "2 * 16 - 1 + 14 % 6", "val": 2 * 16 - 1 + 14 % 6},  # 33
        {"expr": "50 / 2 % 11 * 3 - 4", "val": 50 / 2 % 11 * 3 - 4},  # 5.0
        {"expr": "6 * 8 + 12 / 4 - 15", "val": 6 * 8 + 12 / 4 - 15},  # 36.0
        {"expr": "88 % 10 * 4 / 2 + 9", "val": 88 % 10 * 4 / 2 + 9},  # 25.0
        {"expr": "13 + 9 % 7 / 8 * 12", "val": 13 + 9 % 7 / 8 * 12},  # 16.0
        {"expr": "35 / 7 * 5 - 20 + 8", "val": 35 / 7 * 5 - 20 + 8},  # 13.0
        {"expr": "12 * 11 % 15 - 7 + 5", "val": 12 * 11 % 15 - 7 + 5},  # 10
        {"expr": "10 - 8 - 1 + 40 / 5", "val": 10 - 8 - 1 + 40 / 5},  # 9.0
        {"expr": "6 + 9 - 7 % 4 * 2", "val": 6 + 9 - 7 % 4 * 2},  # 9
        {"expr": "16 % 3 * 20 / 4 + 11", "val": 16 % 3 * 20 / 4 + 11},  # 16.0
        {"expr": "15 + 5 + 14 % 3 * 6", "val": 15 + 5 + 14 % 3 * 6},  # 32
        {"expr": "30 / 3 / 2 * 5 - 1", "val": 30 / 3 / 2 * 5 - 1},  # 24.0
        {"expr": "7 * 7 - 49 + 12 % 5", "val": 7 * 7 - 49 + 12 % 5},  # 2
        {"expr": "18 + 4 - 3 * 6 / 2", "val": 18 + 4 - 3 * 6 / 2},  # 13.0
        {"expr": "99 % 10 * 8 + 4 / 2", "val": 99 % 10 * 8 + 4 / 2},  # 74.0
        {"expr": "20 + 10 % 11 * 3 - 5", "val": 20 + 10 % 11 * 3 - 5},  # 45
        {"expr": "14 / 4 * 8 - 12 + 6", "val": 14 / 4 * 8 - 12 + 6},  # 22.0
        {"expr": "16 * 10 / 14 + 2 - 5", "val": 16 * 10 / 14 + 2 - 5},  # 8.428571428571429
        {"expr": "5 + 10 * 15 / 3 % 7", "val": 5 + 10 * 15 / 3 % 7},  # 6.0
        {"expr": "12 / 4 + 5 * 6 - 2", "val": 12 / 4 + 5 * 6 - 2},  # 31.0
        {"expr": "80 % 9 * 8 - 14 / 2", "val": 80 % 9 * 8 - 14 / 2},  # 57.0
        {"expr": "3 + 14 % 19 * 2 - 1", "val": 3 + 14 % 19 * 2 - 1},  # 30
        {"expr": "70 / 7 * 2 - 15 % 4", "val": 70 / 7 * 2 - 15 % 4},  # 17.0
        {"expr": "4 * 4 * 4 / 2 + 8", "val": 4 * 4 * 4 / 2 + 8},  # 40.0
        {"expr": "100 - 50 * 2 / 4 % 3", "val": 100 - 50 * 2 / 4 % 3}  # 99.0
    ]

    render_template("pyMath.jinja",mathFormulas=mathFormulas)


renderMathCalculation()

render_template("jiMath.jinja")