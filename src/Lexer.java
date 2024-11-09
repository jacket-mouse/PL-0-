
public class Lexer {
    final String input;    // 初始化后无法更改
    char peek;
    int pos;
    int line; // 当前处理的input里的行数
    Boolean flag; // 记录下一次读取字符是不是该行数加一，将加一操作延迟
    private final KeywordTable kwTable = new KeywordTable();

    // 构造函数
    public Lexer(String input) {
        this.input = input;
        this.pos = 0;   // 记录下一个要扫描的位置
        this.peek = input.charAt(peek);
        this.line = 1;
        this.flag = false;
    }

    public void advance() {
        if (flag) line++;
        flag = false;
        pos++;
        if (pos >= input.length()) {
            peek = Character.MIN_VALUE;
        } else {
            peek = input.charAt(pos);
            if (peek == '\n') {
                flag = true;
            }
        }
    }

    // 获取下一个词法单元
    public Token nextToken() {
        if (pos == input.length()) {
            return Token.EOF;
        }
        Token token = null;
        if (Character.isWhitespace(peek)) {
            token = WS();
        } else if (Character.isLetter(peek)) {
            token = ID();
        } else if (Character.isDigit(peek)) {
            token = NUMBER();
        } else if (peek == '.') {
            token = Token.DOT;
            advance();
        } else if (peek == '+') {
            token = Token.PLUS;
            advance();
        } else if (peek == '-') {
            token = Token.MINUS;
            advance();
        } else if (peek == '*') {
            token = Token.TIMES;
            advance();
        } else if (peek == '/') {
            token = Token.SLASH;
            advance();
        } else if (peek == '(') {
            token = Token.LPAREN;
            advance();
        } else if (peek == ')') {
            token = Token.RPAREN;
            advance();
        } else if (peek == ',') {
            token = Token.COMMA;
            advance();
        } else if (peek == ';') {
            token = Token.SEMICOLON;
            advance();
        } else if (peek == '=') {
            token = Token.EQU;
            advance();
        } else if (peek == ':') {
            advance();
            if (peek == '=') {
                token = Token.BECOMES;
                advance();
            }
        } else if (peek == '>') {
            advance();
            if (peek == '=') {
                token = Token.GEQ;
                advance();
            } else {
                token = Token.GTR;
            }
        } else if (peek == '<') {
            advance();
            if (peek == '=') {
                token = Token.LEQ;
                advance();
            } else if (peek == '>') {
                token = Token.NEQ;
                advance();
            } else {
                token = Token.LES;
            }
        }
        return token;
    }

    private Token WS() {
        while (Character.isWhitespace(peek)) {
            advance();
        }
        return Token.NULL;
    }

    private Token ID() {
        StringBuilder sb = new StringBuilder();
        do {
            sb.append(peek);
            advance();
        } while (Character.isLetterOrDigit(peek));
        // ID最大长度限制
        if (sb.length() > Token.MAXIDLEN) return Token.UNKNOWN;
        Token token = kwTable.getKeyword(sb.toString());
        if (token == null) {
            return new Token(TokenType.SYM_IDENTIFIER, sb.toString());
        }
        return token;
    }

    private Token NUMBER() {
        StringBuilder sb = new StringBuilder();
        do {
            sb.append(peek);
            advance();
        } while (Character.isDigit(peek));
        // 数字最大长度限制
        if (sb.length() > Token.MAXNUMLEN) return Token.UNKNOWN;
        return new Token(TokenType.SYM_NUMBER, sb.toString());
    }
}

