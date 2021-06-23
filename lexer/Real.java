package lexer;

public class Real extends Token {
    public float value;

    public Real(float value) {
        super(TokenType.REAL);
        this.value = value;
    }
}
