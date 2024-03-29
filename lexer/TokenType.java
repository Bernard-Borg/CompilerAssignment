package lexer;

//Class containing all the types of tokens
public enum TokenType {
    ADD("'+'"),
    AND("'and'"),
    CHAR("char literal"),
    CLOSECURLYBRACKET("'}'"),
    CLOSEROUNDBRACKET("')'"),
    CLOSESQUAREBRACKET("']'"),
    CMP("'=='"),
    COLON("':'"),
    COMMA("','"),
    COMPLEXTYPE("struct type"),
    DIV("'/'"),
    DOT("'.'"),
    ELSE("'else'"),
    EQ("'='"),
    FALSE("'false'"),
    FOR("'for'"),
    GT("'>'"),
    GTE("'>='"),
    IDENTIFIER("identifier"),
    IF("'if'"),
    LET("'let'"),
    LT("'<'"),
    LTE("'<='"),
    MUL("'*'"),
    NE("'!='"),
    NOT("'not'"),
    NUM("integer literal"),
    OPENCURLYBRACKET("'{'"),
    OPENROUNDBRACKET("'('"),
    OPENSQUAREBRACKET("'['"),
    OR("'or'"),
    PRINT("'print'"),
    REAL("float literal"),
    RETURN("'return'"),
    SEMICOLON("';'"),
    STRING("string literal"),
    STRUCT("'tlstruct'"),
    SUB("'-'"),
    TRUE("'true'"),
    TYPE("type specifier"),
    WHILE("'while'");

    //Name is used for error messages
    private final String name;

    TokenType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
