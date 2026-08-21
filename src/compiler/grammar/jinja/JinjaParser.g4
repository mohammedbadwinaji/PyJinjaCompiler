parser compiler.grammar JinjaParser;

options { tokenVocab = JinjaLexer; }

// ===============================
// TEMPLATE ROOT
// ===============================
template
    : body=templateBody EOF
    ;

// ===============================
// TEMPLATE BODY
// ===============================
templateBody
    : element*
    ;

// ===============================
// ELEMENTS
// ===============================
element
    : HTML_TEXT                              #htmlElement
    | expressionOutput                       #expressionElement
    | ifStatement                            #ifElement
    | forStatement                           #forElement
    ;

// ===============================
// {{ expression }}
// ===============================


expressionOutput
    : JINJA_EXPR_START expression=expr JINJA_EXPR_END
    ;

// ===============================
// {% if %} ... {% elif %} ... {% else %} ... {% endif %}
// ===============================
ifStatement
    : JINJA_STMT_START IF condition=expr JINJA_STMT_END
      thenBody=templateBody
      elifClause*
      elseClause?
      JINJA_STMT_START ENDIF JINJA_STMT_END
    ;

elifClause
    : JINJA_STMT_START ELIF condition=expr JINJA_STMT_END
      body=templateBody
    ;

elseClause
    : JINJA_STMT_START ELSE JINJA_STMT_END
      body=templateBody
    ;

// ===============================
// {% for %} ... {% endfor %}
// ===============================
forStatement
    : JINJA_STMT_START FOR variable=IDENTIFIER IN iterable=expr JINJA_STMT_END
      body=templateBody
      JINJA_STMT_START ENDFOR JINJA_STMT_END
    ;

// ===============================
// EXPRESSIONS (helper rules for operator precedence)
// ===============================
expr
    : logicalOr
    ;

logicalOr
    : left=logicalOr op=OR right=logicalAnd              #orExpr
    | logicalAnd                           #logicalAndExpr
    ;

logicalAnd
    : left=logicalAnd op=AND right=comparison           #andExpr
    | comparison                          #comparisonExpr
    ;

comparison
    : left=comparison op=(EQ|NE|LT|GT|LE|GE) right=additive  #comparisonOpExpr
    | additive                            #additiveExpr
    ;

additive
    : left=additive op=(PLUS|MINUS) right=multiplicative    #addExpr
    | multiplicative                      #multiplicativeExpr
    ;

multiplicative
    : left=multiplicative op=(STAR|SLASH|MOD) right=unary   #mulExpr
    | unary                               #unaryExpr
    ;

unary
    : op=NOT operand=unary                #notExpr
    | op=MINUS operand=unary              #unaryMinus
    | primary                             #primaryExpr
    ;

// ===============================
// PRIMARY EXPRESSIONS
// ===============================
primary
    : LPAREN expression=expr RPAREN       #parenExpr
    | literal                             #literalExpr
    | identifier=IDENTIFIER trailers+=trailer*  #identifierExpr
    ;

// ===============================
// LITERALS
// ===============================
literal
    : STRING                              #stringLiteral
    | INT                                 #intLiteral
    | FLOAT                               #floatLiteral
    | TRUE                                #trueLiteral
    | FALSE                               #falseLiteral
    | NONE                                #noneLiteral
    ;

// ===============================
// TRAILERS (mirroring Python's atomTrailer)
// ===============================
trailer
    : DOT attribute=IDENTIFIER             #attributeAccess
    | LBRACK index=expr RBRACK             #indexAccess
    | LPAREN arguments=argList? RPAREN     #callTrailer
    ;

// ===============================
// ARGUMENT LIST
// ===============================
argList
    : expr (COMMA expr)* (COMMA)?
    ;
