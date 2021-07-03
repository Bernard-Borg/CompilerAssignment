package parser;

import lexer.Token;
import visitors.ASTVisitor;

//Binary operator class is used for expressions with an operator (*, -, /, +, and, or, etc)
public class ASTBinaryOperator extends ASTExpression {
    public ASTExpression expression1;
    public ASTExpression expression2;
    public Token operator;

    /**
     * @param expression1 first expression of the binary operator
     * @param expression2 second expression of the binary operator
     * @param operator contains the token for the operator
     */
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
