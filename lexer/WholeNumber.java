package lexer;

//Class for whole numbers (integers)
public class WholeNumber extends Token {
    public int value;

    public WholeNumber(int value) {
        super(TokenType.NUM);
        this.value = value;
    }
}
