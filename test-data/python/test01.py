


def renderPersonInfo(name , age , gender) :
    render_template("personInfo.jinja",name=name,age=age,gender=gender)


def getName() :
    name = "mohammed"
    return name

def getAge() :
    return (5 + 6 * 10 / 3)

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
