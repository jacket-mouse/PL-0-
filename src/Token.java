public class Token {
    // constant
    public static final int MAXNUMLEN = 14;
    public static final int MAXIDLEN = 10;

    public static final Token EOF = new Token(TokenType.EOF, "EOF");
    public static final Token NULL = new Token(TokenType.SYM_NULL, " ");
    public static final Token UNKNOWN = new Token(TokenType.UNKNOWN, "Lexer Error");
    // keyword
    public static final Token IF = new Token(TokenType.SYM_IF, "if");
    public static final Token THEN = new Token(TokenType.SYM_THEN, "then");
    public static final Token CALL = new Token(TokenType.SYM_CALL, "call");
    public static final Token WHILE = new Token(TokenType.SYM_WHILE, "while");
    public static final Token DO = new Token(TokenType.SYM_DO, "do");
    public static final Token CONST = new Token(TokenType.SYM_CONST, "const");
    public static final Token VAR = new Token(TokenType.SYM_VAR, "var");
    public static final Token PROCEDURE = new Token(TokenType.SYM_PROCEDURE, "procedure");
    public static final Token BEGIN = new Token(TokenType.SYM_BEGIN, "begin");
    public static final Token END = new Token(TokenType.SYM_END, "end");
    public static final Token ODD = new Token(TokenType.SYM_ODD, "odd");
    // symbol
    public static final Token EQU = new Token(TokenType.SYM_EQU, "=");
    public static final Token NEQ = new Token(TokenType.SYM_NEQ, "<>");
    public static final Token LES = new Token(TokenType.SYM_LES, "<");
    public static final Token LEQ = new Token(TokenType.SYM_LEQ, "<=");
    public static final Token GTR = new Token(TokenType.SYM_GTR, ">");
    public static final Token GEQ = new Token(TokenType.SYM_GEQ, ">=");
    public static final Token DOT = new Token(TokenType.SYM_DOT, ".");
    public static final Token PLUS = new Token(TokenType.SYM_PLUS, "+");
    public static final Token MINUS = new Token(TokenType.SYM_MINUS, "-");
    public static final Token TIMES = new Token(TokenType.SYM_TIMES, "*");
    public static final Token SLASH = new Token(TokenType.SYM_SLASH, "/");
    public static final Token LPAREN = new Token(TokenType.SYM_LPAREN, "(");
    public static final Token RPAREN = new Token(TokenType.SYM_RPAREN, ")");
    public static final Token COMMA = new Token(TokenType.SYM_COMMA, ",");
    public static final Token SEMICOLON = new Token(TokenType.SYM_SEMICOLON, ";");
    public static final Token BECOMES = new Token(TokenType.SYM_BECOMES, ":=");

    private final TokenType type;
    private final String text;

    public Token(TokenType type, String text) {
        this.type = type;
        this.text = text;
    }

    public String getText() {
        return this.text;
    }
    public TokenType getType() { return this.type; }

    @Override
    public String toString() {
        return String.format(" {type : %s, text : %s}", this.type, this.text);
    }

}
