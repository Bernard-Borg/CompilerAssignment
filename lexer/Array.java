package lexer;

public class Array extends Type {
    public int size;
    public Type arrayType;

    public Array(int size, Type type) {
        super(type.lexeme + "[]", TokenType.TYPE);
        this.size = size;
        arrayType = type;
    }
}