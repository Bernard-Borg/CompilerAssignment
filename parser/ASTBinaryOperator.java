package parser;

import lexer.Token;
import visitors.ASTVisitor;

public class ASTBinaryOperator extends ASTExpression {
    public ASTExpression expression1;
    public ASTExpression expression2;
    public Token operator;

    public ASTBinaryOperator(ASTExpression expression1, ASTExpression expression2, Token operator) {
        this.expression1 = expression1;
        this.expression2 = expression2;
        this.operator = operator;
    }

    @Override
    public void accept(ASTVisitor visitor) throws Exception {
        visitor.visit(this);
    }
}
