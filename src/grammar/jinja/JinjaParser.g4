parser grammar JinjaParser;

options { tokenVocab = JinjaLexer; }

// ===============================
// TEMPLATE ROOT
// ===============================
template
    : element* EOF

    ;

// ===============================
// ELEMENTS
// ===============================
element
    : HTML_TEXT              #HtmlElement
    | jinjaExpr              #ExprElement
    | jinjaStmt              #StmtElement
    | jinjaComment           #CommentElement
    ;

// ===============================
// {{ expression }}
// ===============================
jinjaExpr
    : JINJA_EXPR_START expr JINJA_EXPR_END
      #JinjaExpression
    ;

// ===============================
// {% statement %}
//
// ===============================
jinjaStmt
    : JINJA_STMT_START stmt JINJA_STMT_END
      #JinjaStatement
    ;

// ===============================
// {# comment #}
// ===============================
jinjaComment
    : JINJA_COMMENT_START COMMENT_TEXT? JINJA_COMMENT_END
      #Comment
    ;


// ===============================
// STATEMENTS
// ===============================
stmt
    : ifStmt     #IfStatement
    | forStmt    #ForStatement
    ;

// ===============================
// IF / ELIF / ELSE / ENDIF
// ===============================
ifStmt
    : IF expr        #IfOpen
    | ELIF expr      #ElifOpen
    | ELSE           #ElseOpen
    | ENDIF          #IfClose
    ;

// ===============================
// FOR / ENDFOR
// ===============================
forStmt
    : FOR IDENTIFIER IN expr   #ForOpen
    | ENDFOR                   #ForClose
    ;

// ===============================
// EXPRESSIONS (with proper precedence)
// ===============================
expr
    : logicalOr                       #LogicalOrRoot
    ;

logicalOr
    : logicalOr OR logicalAnd         #OrExpr
    | logicalAnd                      #AndRoot
    ;

logicalAnd
    : logicalAnd AND comparison       #AndExpr
    | comparison                      #ComparisonRoot
    ;

comparison
    : comparison (EQ|NE|LT|GT|LE|GE) additive  #CompareExpr
    | additive                        #AdditiveRoot
    ;

additive
    : additive (PLUS|MINUS) multiplicative #AddExpr
    | multiplicative                 #MultiplicativeRoot
    ;

multiplicative
    : multiplicative (STAR|SLASH|MOD) unary #MulExpr
    | unary                          #UnaryRoot
    ;

unary
    : NOT unary                       #NotExpr
    | MINUS unary                     #UnaryMinusExpr
    | atom                           #AtomRoot
    ;

// ===============================
// ATOMS
// ===============================
atom
    : LPAREN expr RPAREN              #ParenAtom
    | IDENTIFIER trailer*              #IdentifierAtom
    | STRING                           #StringAtom
    | INT                              #IntAtom
    | FLOAT                            #FloatAtom
    | TRUE                             #TrueAtom
    | FALSE                            #FalseAtom
    | NONE                             #NoneAtom
    ;

// ===============================
// TRAILERS
// ===============================
trailer
    : DOT IDENTIFIER                   #AttributeTrailer
    | LBRACK expr RBRACK               #IndexTrailer
    | LPAREN argList? RPAREN           #CallTrailer
    ;

// ===============================
// ARGUMENT LIST
// ===============================
argList
    : expr (COMMA expr)* (COMMA)?
    ;
