package parser;

import visitors.ASTVisitor;

//ASTNode for print statements
public class ASTPrint extends ASTStatement {
    public ASTExpression expression;

    /**
     * @param expression expression to print
     */
    public ASTPrint(ASTExpression expression) {
        this.expression = expression;
    }

    @Override
    public void accept(ASTVisitor visitor) throws Exception {
        visitor.visit(this);
    }
}
