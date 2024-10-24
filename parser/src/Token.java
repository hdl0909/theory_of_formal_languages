public class Token {
    TokenType tokenType;
    String tokenValue;

    Token(TokenType tokenType, String tokenValue) {
        this.tokenType = tokenType;
        this.tokenValue = tokenValue;
    }

    public String toString() {
        return "[" + tokenType + ": " + tokenValue + "]";
    }
}

