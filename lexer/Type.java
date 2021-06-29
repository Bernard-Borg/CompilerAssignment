package lexer;

//Class for primitive types
public class Type extends Word {
    public static final Type INTEGER = new Type("int", TokenType.TYPE);
    public static final Type FLOAT = new Type("float", TokenType.TYPE);
    public static final Type BOOL = new Type("bool", TokenType.TYPE);
    public static final Type STRING = new Type("string", TokenType.TYPE);
    public static final Type CHAR = new Type("char", TokenType.TYPE);
    public static final Type AUTO = new Type ("auto", TokenType.TYPE);

    public Type(String word, TokenType tokenType) {
        super(word, tokenType);
    }
}
