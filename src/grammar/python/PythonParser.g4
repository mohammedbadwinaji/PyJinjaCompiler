parser grammar PythonParser;

options { tokenVocab=PythonLexer; }

tokens { INDENT, DEDENT }

program
    : (statement | NEWLINE)* EOF
    ;

statement
    : RETURN expr (COMMA expr)* NEWLINE?                    #returnStatement
    | expr (ASSIGN expr)* NEWLINE?                           #assignOrExprStatement
    | decorator* NEWLINE? DEF IDENTIFIER LPAREN paramList? RPAREN COLON suite #functionDef
    | IF expr COLON suite (ELIF expr COLON suite)* (ELSE COLON suite)? #ifStatement
    | WHILE expr COLON suite                                        #whileStatement
    | FOR IDENTIFIER IN expr COLON suite                            #forStatement
    ;

decorator
    : AT IDENTIFIER (DOT IDENTIFIER)* LPAREN argList? RPAREN
    ;

paramList
    : IDENTIFIER (COMMA IDENTIFIER)*
    ;

suite
    : simpleLine                                                   #singleSuite
    | NEWLINE INDENT statement+ DEDENT                             #indentedSuite
    ;

simpleLine
    : expr (ASSIGN expr)* NEWLINE?;


expr
    : logicalOr
    ;

logicalOr
    : left=logicalOr op=OR right=logicalAnd              #orExpr
    | logicalAnd                           #logicalAndExpr
    ;

logicalAnd
    : left=logicalAnd op=AND right=comparison           #andExpr
    | comparison                          #logicalAndComparisonExpr
    ;

comparison
    : left=comparison op=(EQ|NE|LT|GT|LE|GE) right=additive  #comparisonOpExpr
    | additive                            #comparisonAdditiveExpr
    ;

additive
    : left=additive op=(PLUS|MINUS) right=multiplicative    #addExpr
    | multiplicative                      #additiveMultiplicativeExpr
    ;

multiplicative
    : left=multiplicative op=(STAR|SLASH|MOD) right=unary   #mulExpr
    | unary                               #multiplicativeUnaryExpr
    ;

unary
    : op=NOT operand=unary                #notExpr
    | op=MINUS operand=unary              #unaryMinus
    | primary                             #primaryExpr
    ;

primary
    : LPAREN expression=expr RPAREN       #parenExpr
    | atom                                 #atomExpr
    ;

listItems
    : expr (COMMA expr)* (COMMA)?
    ;

atom
    : LBRACK listItems? RBRACK              #listExpr
    | LBRACE dictItems? RBRACE             #dictExpr
    | STRING_LITERAL                       #stringLiteral
    | INTEGER_LITERAL                      #intLiteral
    | FLOAT_LITERAL                        #floatLiteral
    | TRUE                                 #trueLiteral
    | FALSE                                #falseLiteral
    | NONE                                 #noneLiteral
    | IDENTIFIER atomTrailer*              #identifierAtom
    ;

atomTrailer
    : DOT IDENTIFIER                       #attributeAccess
    | LBRACK expr RBRACK                   #indexAccess
    | LPAREN argList? RPAREN               #callTrailer
    ;

argList
    : argument (COMMA argument)* (COMMA)?
    ;

argument
    : expr                                 #positionalArg
    | IDENTIFIER ASSIGN expr               #keywordArg
    ;

dictItems
    : dictItem (COMMA dictItem)* (COMMA)?
    ;

dictItem
    : (STRING_LITERAL | IDENTIFIER) COLON expr
    ;
