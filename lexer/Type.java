package lexer;

public class Type extends Word {
    public static final Type INTEGER = new Type("int", TokenType.PRIMITIVE);
    public static final Type FLOAT = new Type("float", TokenType.PRIMITIVE);
    public static final Type BOOL = new Type("bool", TokenType.PRIMITIVE);
    public static final Type STRING = new Type("string", TokenType.PRIMITIVE);
    public static final Type CHAR = new Type("char", TokenType.PRIMITIVE);

    public Type(String word, TokenType tokenType) {
        super(word, tokenType);
    }
}
