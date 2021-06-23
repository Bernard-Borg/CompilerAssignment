package parser;

import lexer.Token;
import lexer.TokenType;
import lexer.Word;
import visitors.ASTVisitor;

public class ASTLiteral extends ASTExpression {
    public Token token;
    public String type;

    public ASTLiteral(Token token) {
        if (token.tokenType == TokenType.STRING) {
            //Removing the quotation marks for strings
            String lexeme = ((Word) token).lexeme;
            ((Word) token).lexeme = lexeme.substring(1, lexeme.length() - 1);
            type = "string";
        } else if (token.tokenType == TokenType.NUM) {
            type = "int";
        } else if (token.tokenType == TokenType.REAL) {
            type = "float";
        } else if (token.tokenType == TokenType.CHAR) {
            //Removing the apostrophe marks for characters
            String lexeme = ((Word) token).lexeme;
            ((Word) token).lexeme = lexeme.substring(1, lexeme.length() - 1);
            type = "char";
        } else if (token.tokenType == TokenType.TRUE || token.tokenType == TokenType.FALSE) {
            type = "bool";
        }

        this.token = token;
    }

    @Override
    public void accept(ASTVisitor visitor) throws Exception {
        visitor.visit(this);
    }
}
