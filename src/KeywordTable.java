import java.util.HashMap;
import java.util.Map;

public class KeywordTable {
    private final Map<String, Token> keywords = new HashMap<>();

    public KeywordTable() {
        this.reserve(Token.IF);
        this.reserve(Token.THEN);
        this.reserve(Token.CALL);
        this.reserve(Token.WHILE);
        this.reserve(Token.DO);
        this.reserve(Token.CONST);
        this.reserve(Token.VAR);
        this.reserve(Token.PROCEDURE);
        this.reserve(Token.BEGIN);
        this.reserve(Token.END);
        this.reserve(Token.ODD);
    }

    public Token getKeyword(String text) {
        return keywords.get(text);
    }

    private void reserve(Token token) {
        keywords.put(token.getText(), token);
    }

}
