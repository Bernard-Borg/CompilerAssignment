package parser;

import lexer.TokenType;
import visitors.ASTVisitor;

public class ASTUnary extends ASTExpression {
    public TokenType unaryType;
    public ASTExpression expression;

    public ASTUnary(TokenType unaryType, ASTExpression expression) {
        this.unaryType = unaryType;
        this.expression = expression;
    }

    @Override
    public void accept(ASTVisitor visitor) throws Exception {
        visitor.visit(this);
    }
}
