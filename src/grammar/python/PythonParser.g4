parser grammar PythonParser;

options { tokenVocab=PythonLexer; }

tokens { INDENT, DEDENT }

program
    : statement* EOF
    ;

statement
    : RETURN exprList? NEWLINE                                      #returnStatement
    | exprList (ASSIGN exprList)* NEWLINE                           #assignOrExprStatement
    | decorator* DEF IDENTIFIER LPAREN paramList? RPAREN COLON suite #functionDef
    | IF expr COLON suite (ELIF expr COLON suite)* (ELSE COLON suite)? #ifStatement
    | WHILE expr COLON suite                                        #whileStatement
    | FOR IDENTIFIER IN expr COLON suite                            #forStatement
    ;

decorator
    : AT IDENTIFIER (DOT IDENTIFIER)* LPAREN argList? RPAREN NEWLINE
    ;

paramList
    : IDENTIFIER (COMMA IDENTIFIER)*
    ;

suite
    : simpleLine                                                   #singleSuite
    | NEWLINE INDENT statement+ DEDENT                             #indentedSuite
    ;

simpleLine
    : exprList (ASSIGN exprList)* NEWLINE
    ;

exprList
    : expr (COMMA expr)* (COMMA)?
    ;

expr
    : expr OR expr                         #orExpr
    | expr AND expr                        #andExpr
    | NOT expr                             #notExpr
    | expr (EQ|NE|LT|GT|LE|GE) expr        #comparisonExpr
    | expr (PLUS|MINUS) expr               #addExpr
    | expr (STAR|SLASH|MOD) expr           #mulExpr
    | MINUS expr                           #unaryMinus
    | atom                                 #atomExpr
    ;

atom
    : LPAREN expr RPAREN                   #parenExpr
    | LBRACK exprList? RBRACK              #listExpr
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
