package lexer;

//Class for identifiers, string literals and keywords
public class Word extends Token {
    public static final Word OR = new Word("or", TokenType.OR);
    public static final Word AND = new Word("and", TokenType.AND);
    public static final Word TRUE = new Word("true", TokenType.TRUE);
    public static final Word FALSE = new Word("false", TokenType.FALSE);
    public static final Word LET = new Word("let", TokenType.LET);
    public static final Word PRINT = new Word("print", TokenType.PRINT);
    public static final Word RETURN = new Word("return", TokenType.RETURN);
    public static final Word IF = new Word("if", TokenType.IF);
    public static final Word ELSE = new Word("else", TokenType.ELSE);
    public static final Word FOR = new Word("for", TokenType.FOR);
    public static final Word WHILE = new Word("while", TokenType.WHILE);
    public static final Word NOT = new Word("not", TokenType.NOT);

    public String lexeme = "";

    public Word(String word, TokenType tokenType) {
        super(tokenType);
        lexeme = word;
    }
}
