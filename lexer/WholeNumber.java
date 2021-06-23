package lexer;

public class WholeNumber extends Token {
    public int value;

    public WholeNumber(int value) {
        super(TokenType.NUM);
        this.value = value;
    }
}
