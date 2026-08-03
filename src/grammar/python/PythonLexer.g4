lexer grammar PythonLexer;

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

UNCLOSED_DOUBLE_STRING
    : '"' (ESC_SEQ | ~["\\\r\n])*
      { throw new compiler.exception.LexicalException("Unterminated string at line " + getLine()); }
    ;

UNCLOSED_SINGLE_STRING
    : '\'' (ESC_SEQ | ~['\\\r\n])*
      { throw new compiler.exception.LexicalException("Unterminated string at line " + getLine()); }
    ;

IDENTIFIER : [_a-zA-Z] [_a-zA-Z0-9]* ;

NEWLINE
    : ('\r'? '\n' | '\r')
      { setText("\n"); }
    ;

COMMENT : '#' ~[\r\n]* -> skip ;

WS
    : [ \t]+
      {
          int next = _input.LA(1);
          if (next != '\n' && next != '\r') {
              skip();
          }
      }
    ;

fragment DIGIT : [0-9];
fragment ESC_SEQ : '\\' . ;

ERROR_CHAR
    : .
      {
          throw new compiler.exception.LexicalException(
              "Illegal character '" + getText() +
              "' at line " + getLine() +
              ", column " + getCharPositionInLine()
          );
      }
    ;
