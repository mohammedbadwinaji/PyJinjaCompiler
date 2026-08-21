lexer compiler.grammar PythonLexer;

DEF         : 'def';
RETURN      : 'return';

IF          : 'if';
ELIF        : 'elif';
ELSE        : 'else';

FOR         : 'for';
IN          : 'in';
WHILE       : 'while';

TRUE        : 'True';
FALSE       : 'False';
NONE        : 'None';

AND         : 'and';
OR          : 'or';
NOT         : 'not';

EQ          : '==';
NE          : '!=';
LE          : '<=';
GE          : '>=';

ASSIGN      : '=';

LT          : '<';
GT          : '>';

PLUS        : '+';
MINUS       : '-';
STAR        : '*';
SLASH       : '/';
MOD         : '%';

LPAREN      : '(' ;
RPAREN      : ')' ;

LBRACK      : '[' ;
RBRACK      : ']' ;

LBRACE      : '{' ;
RBRACE      : '}' ;

COLON       : ':';
COMMA       : ',';
DOT         : '.';
AT          : '@';

FLOAT_LITERAL : DIGIT+ '.' DIGIT+ ;
INTEGER_LITERAL : DIGIT+ ;

STRING_LITERAL
    : '"' (ESC_SEQ | ~["\\\r\n])* '"'
    | '\'' (ESC_SEQ | ~['\\\r\n])* '\''
    ;

IDENTIFIER : [_a-zA-Z] [_a-zA-Z0-9]* ;

NEWLINE
    : '\r'? '\n' | '\r'
    ;

COMMENT : '#' ~[\r\n]* -> skip ;

WS
    : [ \t]+ -> skip
    ;

fragment DIGIT : [0-9];
fragment ESC_SEQ : '\\' . ;
