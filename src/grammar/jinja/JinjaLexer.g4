lexer grammar JinjaLexer;

// Default mode - outside Jinja tags
HTML_TEXT : (~[{] | '{' ~['{%#])+ ;

JINJA_EXPR_START : '{{' -> pushMode(JINJA_MODE);
JINJA_STMT_START : '{%' -> pushMode(JINJA_MODE);
JINJA_COMMENT_START : '{#' -> pushMode(COMMENT_MODE);

// Jinja mode - inside {{ }} or {% %}
mode JINJA_MODE;
JINJA_EXPR_END : '}}' -> popMode;
JINJA_STMT_END : '%}' -> popMode;

IF      : 'if';
ELIF    : 'elif';
ELSE    : 'else';
ENDIF   : 'endif';
FOR     : 'for';
IN      : 'in';
ENDFOR  : 'endfor';

AND     : 'and';
OR      : 'or';
NOT     : 'not';

TRUE    : 'true';
FALSE   : 'false';
NONE    : 'none';

EQ      : '==';
NE      : '!=';
LT      : '<';
GT      : '>';
LE      : '<=';
GE      : '>=';

PLUS    : '+';
MINUS   : '-';
STAR    : '*';
SLASH   : '/';
MOD     : '%';

DOT     : '.';
LPAREN  : '(';
RPAREN  : ')';
LBRACK  : '[';
RBRACK  : ']';
COMMA   : ',';

STRING  : '"' (~["\\] | '\\' .)* '"' ;
INT     : [0-9]+;
FLOAT   : [0-9]+ '.' [0-9]+;

IDENTIFIER : [_a-zA-Z] [_a-zA-Z0-9]*;

WS : [ \t\r\n]+ -> skip;

// Comment mode - inside {# }
mode COMMENT_MODE;
JINJA_COMMENT_END : '#}' -> popMode;
COMMENT_TEXT : ~[#]+ ;
