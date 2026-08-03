parser grammar PythonParser;

options {
    tokenVocab = PythonLexer;
}

//====================================================
// Program
//====================================================

program
    : programElement* EOF
      #Program
    ;

programElement
    : functionDefinition NEWLINE*
      #FunctionElement

    | statement NEWLINE*
      #StatementElement

    | NEWLINE
      #BlankLine
    ;

//====================================================
// Statements
//====================================================

statement
    : assignmentStatement
      #AssignmentStmt

    | returnStatement
      #ReturnStmt

    | expressionStatement
      #ExpressionStmt

    | ifStatement
      #IfStmt

    | whileStatement
      #WhileStmt

    | forStatement
      #ForStmt

    | passStatement
      #PassStmt
    ;

block
    : NEWLINE
      INDENT
      statement+
      DEDENT
      #Block
    ;

//====================================================
// Function Definition
//====================================================

functionDefinition
    : routeDecorator*
      DEF
      IDENTIFIER
      LPAREN
      parameterList?
      RPAREN
      COLON
      block
      #FunctionDefinition
    ;

routeDecorator
    : AT
      IDENTIFIER
      DOT
      IDENTIFIER
      LPAREN
      STRING_LITERAL
      RPAREN
      NEWLINE
      #RouteDecorator
    ;

parameterList
    : parameter
      (COMMA parameter)*
      COMMA?
      #ParameterList
    ;

parameter
    : IDENTIFIER
      #Parameter
    ;

//====================================================
// Assignment
//====================================================

assignmentStatement
    : assignmentTarget
      ASSIGN
      expression
      #AssignmentStatement
    ;

assignmentTarget
    : IDENTIFIER
      #VariableAssignment
    ;

//====================================================
// Return
//====================================================

returnStatement
    : RETURN expression?
      #ReturnStatement
    ;

//====================================================
// Expression Statement
//====================================================

expressionStatement
    : expression
      #ExpressionStatement
    ;

//====================================================
// Pass
//====================================================

passStatement
    : IDENTIFIER
      #PassStatement
    ;

//====================================================
// If Statement
//====================================================

ifStatement
    : IF
      expression
      COLON
      block
      elifClause*
      elseClause?
      #IfStatement
    ;

elifClause
    : ELIF
      expression
      COLON
      block
      #ElifClause
    ;

elseClause
    : ELSE
      COLON
      block
      #ElseClause
    ;

//====================================================
// While Statement
//====================================================

whileStatement
    : WHILE
      expression
      COLON
      block
      #WhileStatement
    ;

//====================================================
// For Statement
//====================================================

forStatement
    : FOR
      IDENTIFIER
      IN
      expression
      COLON
      block
      #ForStatement
    ;

//====================================================
// Expression
//====================================================

expression
    : logicalOrExpression
      #Expression
    ;

//====================================================
// Logical OR
//====================================================

logicalOrExpression
    : logicalOrExpression
      OR
      logicalAndExpression
      #OrExpression

    | logicalAndExpression
      #LogicalAndRoot
    ;

//====================================================
// Logical AND
//====================================================

logicalAndExpression
    : logicalAndExpression
      AND
      equalityExpression
      #AndExpression

    | equalityExpression
      #EqualityRoot
    ;

//====================================================
// Equality
//====================================================

equalityExpression
    : equalityExpression
      EQ
      comparisonExpression
      #EqualExpression

    | equalityExpression
      NE
      comparisonExpression
      #NotEqualExpression

    | comparisonExpression
      #ComparisonRoot
    ;

//====================================================
// Comparison
//====================================================

comparisonExpression
    : comparisonExpression
      LT
      additiveExpression
      #LessThanExpression

    | comparisonExpression
      LE
      additiveExpression
      #LessEqualExpression

    | comparisonExpression
      GT
      additiveExpression
      #GreaterThanExpression

    | comparisonExpression
      GE
      additiveExpression
      #GreaterEqualExpression

    | additiveExpression
      #AdditiveRoot
    ;

