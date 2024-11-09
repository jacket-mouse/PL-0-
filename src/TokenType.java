public enum TokenType {
    // Group 0
    EOF,  // end of file
    UNKNOWN,  // for error

    // Group 1
    // lookhead = 1 (LA(1))
    SYM_IF, SYM_THEN, SYM_CALL,
    SYM_CONST, SYM_VAR,
    SYM_PROCEDURE,
    SYM_BEGIN, SYM_END,
    SYM_DO, SYM_WHILE,
    SYM_IDENTIFIER,
    SYM_NUMBER,
    SYM_NULL,
    SYM_ODD,
    SYM_PERIOD,
    SYM_PLUS, SYM_MINUS, SYM_TIMES, SYM_SLASH,
    SYM_LPAREN, SYM_RPAREN,
    SYM_COMMA, SYM_SEMICOLON,
    SYM_BECOMES,
    SYM_DOT,

    // Group 2
    // =, <>, <, <=, >, >=
    SYM_EQU, SYM_NEQ, SYM_LES, SYM_LEQ, SYM_GTR, SYM_GEQ,

}
