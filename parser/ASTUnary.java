package parser;

import lexer.TokenType;
import visitors.ASTVisitor;

//ASTNode for expressions with unary operators (- and not)
public class ASTUnary extends ASTExpression {
    public TokenType unaryType;
    public ASTExpression expression;

    /**
     * @param unaryType unary operator
     * @param expression expression being operated on
     */
    public ASTUnary(TokenType unaryType, ASTExpression expression) {
        this.unaryType = unaryType;
        this.expression = expression;
    }

    @Override
    public void accept(ASTVisitor visitor) throws Exception {
        visitor.visit(this);
    }
}
