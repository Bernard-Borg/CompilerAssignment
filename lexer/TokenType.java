package lexer;

public enum TokenType {
    ADD("'+'"),
    AND("'and'"),
    CHAR("char literal"),
    CLOSECURLYBRACKET("'}'"),
    CLOSEROUNDBRACKET("')'"),
    CMP("'=='"),
    COLON("':'"),
    COMMA("','"),
    DIV("'/'"),
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
    OR("'or'"),
    PRIMITIVE("type specifier"),
    PRINT("'print'"),
    REAL("float literal"),
    RETURN("'return'"),
    SEMICOLON("';'"),
    STRING("string literal"),
    SUB("'-'"),
    TRUE("'true'"),
    WHILE("'while'");

    private final String name;

    TokenType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
